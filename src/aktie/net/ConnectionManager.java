package aktie.net;

import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.CommunityMember;
import aktie.data.CommunityMyMember;
import aktie.data.HH2Session;
import aktie.data.IdentityData;
import aktie.data.RequestFile;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.user.IdentityManager;
import aktie.user.PushInterface;
import aktie.user.RequestFileHandler;
import aktie.utils.HasFileCreator;
import aktie.utils.MembershipValidator;
import aktie.utils.SymDecoder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bouncycastle.crypto.params.KeyParameter;
import org.jboss.logging.Logger;

public class ConnectionManager implements GetSendData, DestinationListener, PushInterface, Runnable
{

    Logger log = Logger.getLogger ( "aktie" );

    private Map<String, DestinationThread> destinations;
    private Index index;
    private RequestFileHandler fileHandler;
    private IdentityManager identManager;
    private SymDecoder symdec;
    private MembershipValidator memvalid;
    private boolean stop;
    private HasFileCreator hfc;

    public static int MAX_CONNECTIONS = 10;
    public static long MIN_TIME_BETWEEN_CONNECTIONS = 5L * 60L * 1000L;
    public static long MAX_TIME_WITH_NO_REQUESTS = 30L * 60L * 1000L;

    public ConnectionManager ( HH2Session s, Index i, RequestFileHandler r, IdentityManager id )
    {
        identManager = id;
        index = i;
        destinations = new HashMap<String, DestinationThread>();
        fileHandler = r;
        symdec = new SymDecoder();
        memvalid = new MembershipValidator ( index );
        hfc = new HasFileCreator ( s, index );
        Thread t = new Thread ( this );
        t.start();
    }

    public List<DestinationThread> getDestList()
    {
        List<DestinationThread> r = new LinkedList<DestinationThread>();

        synchronized ( destinations )
        {
            r.addAll ( destinations.values() );
        }

        return r;
    }

    public void addDestination ( DestinationThread d )
    {
        synchronized ( destinations )
        {
            destinations.put ( d.getDest().getPublicDestinationInfo(), d );
        }

    }

    public void push ( CObj fromid, CObj o )
    {
        String dest = fromid.getString ( CObj.DEST );
        DestinationThread dt = null;

        if ( dest != null )
        {
            synchronized ( destinations )
            {
                dt = destinations.get ( dest );
            }

        }

        if ( dt != null )
        {
            dt.send ( o );
        }

    }

    @Override
    public List<String> getConnectedIds ( CObj fromid )
    {
        String dest = fromid.getString ( CObj.DEST );
        List<String> conids = null;

        synchronized ( destinations )
        {
            conids = destinations.get ( dest ).getConnectedIds();
        }

        if ( conids == null )
        {
            conids = new LinkedList<String>();
        }

        return conids;
    }

    @Override
    public void push ( CObj fromid, String to, CObj o )
    {
        String dest = fromid.getString ( CObj.DEST );
        DestinationThread dt = null;

        synchronized ( destinations )
        {
            dt = destinations.get ( dest );
        }

        if ( dt != null )
        {
            dt.send ( to, o );
        }

    }

    public void closeDestinationConnections ( CObj id )
    {
        synchronized ( destinations )
        {
            DestinationThread dt = destinations.get ( id.getString ( CObj.DEST ) );
            dt.closeConnections();
        }

    }

    public void closeAllConnections()
    {
        synchronized ( destinations )
        {
            for ( DestinationThread dt : destinations.values() )
            {
                dt.closeConnections();
            }

        }

    }

    private Map<String, CObj> getMyIdMap()
    {
        List<CObj> myidlst = Index.list ( index.getMyIdentities() );
        Map<String, CObj> myidmap = new HashMap<String, CObj>();

        for ( CObj co : myidlst )
        {
            myidmap.put ( co.getId(), co );
        }

        return myidmap;
    }

    private List<DestinationThread> findMyDestinationsForCommunity ( Map<String, CObj> myidmap, String comid )
    {
        List<CObj> mysubslist = Index.list ( index.getMySubscriptions ( comid ) );
        //Find my destinations for my subscriptions to this community
        List<DestinationThread> dlst = new LinkedList<DestinationThread>();

        synchronized ( destinations )
        {
            for ( CObj c : mysubslist )
            {
                CObj mid = myidmap.get ( c.getString ( CObj.CREATOR ) );

                if ( mid != null )
                {
                    String mydest = mid.getString ( CObj.DEST );

                    if ( mydest != null )
                    {
                        DestinationThread dt = destinations.get ( mydest );
                        dlst.add ( dt );
                    }

                }

            }

        }

        return dlst;
    }

    private List<DestinationThread> findAllMyDestinationsForCommunity ( Map<String, CObj> myidmap, String comid )
    {
        List<DestinationThread> dlst = new LinkedList<DestinationThread>();
        CObj com = index.getCommunity ( comid );

        if ( com != null )
        {
            if ( CObj.SCOPE_PUBLIC.equals ( com.getString ( CObj.SCOPE ) ) )
            {
                synchronized ( destinations )
                {
                    dlst.addAll ( destinations.values() );
                }

            }

            else
            {
                List<CObj> mymemlist = Index.list ( index.getMyMemberships ( comid ) );

                //Find my destinations for my subscriptions to this community
                synchronized ( destinations )
                {
                    for ( CObj c : mymemlist )
                    {
                        CObj mid = myidmap.get ( c.getPrivate ( CObj.MEMBERID ) );

                        if ( mid != null )
                        {
                            String mydest = mid.getString ( CObj.DEST );

                            if ( mydest != null )
                            {
                                DestinationThread dt = destinations.get ( mydest );
                                dlst.add ( dt );
                            }

                        }

                    }

                    if ( "true".equals ( com.getPrivate ( CObj.MINE ) ) )
                    {
                        String creator = com.getString ( CObj.CREATOR );
                        CObj mid = myidmap.get ( creator );

                        if ( mid != null )
                        {
                            String mydest = mid.getString ( CObj.DEST );

                            if ( mydest != null )
                            {
                                DestinationThread dt = destinations.get ( mydest );

                                if ( !dlst.contains ( dt ) )
                                {
                                    dlst.add ( dt );
                                }

                            }

                        }

                    }

                }

            }

        }

        return dlst;
    }

    private void attemptOneConnection ( DestinationThread dt, List<String> idlst, Map<String, CObj> myids )
    {
        IdentityData idat = null;
        int checks = 100;
        long curtime = ( new Date() ).getTime();
        long soonest = curtime - MIN_TIME_BETWEEN_CONNECTIONS;
        //Remove my ids from the list
        Iterator<String> i = idlst.iterator();

        while ( i.hasNext() )
        {
            String id = i.next();

            if ( myids.get ( id ) != null )
            {
                log.info ( "remove id that is mine from connect list." );
                i.remove();
            }

        }

        while ( checks > 0 && idat == null && idlst.size() > 0 )
        {
            String pid = idlst.get ( Utils.Random.nextInt ( idlst.size() ) );
            IdentityData tid = identManager.getIdentity ( pid );

            if ( tid != null )
            {
                if ( tid.getLastConnectionAttempt() < soonest )
                {
                    idat = tid;
                }

            }

            checks--;
        }

        log.info ( "selected node for new connection: " + idat );

        if ( idat != null )
        {
            CObj tid = index.getIdentity ( idat.getId() );

            if ( tid != null )
            {
                identManager.connectionAttempted ( idat.getId() );
                log.info ( "attempt new connection." );
                dt.connect ( tid.getString ( CObj.DEST ) );
            }

        }

    }

    /*
        hlst is a list of CObj's with CREATOR set to identies we
        could connect to
    */
    private void attemptDestinationConnection ( CObjList hlst, DestinationThread dt, Map<String, CObj> myids )
    {
        //See how many of these nodes we're connected to
        List<String> idlst = new LinkedList<String>();
        int connected = 0;

        for ( int c = 0; c < hlst.size(); c++ )
        {
            try
            {
                CObj co = hlst.get ( c );
                String cr = null;

                if ( CObj.MEMBERSHIP.equals ( co.getType() ) )
                {
                    cr = co.getPrivate ( CObj.MEMBERID );
                }

                else if ( CObj.IDENTITY.equals ( co.getType() ) )
                {
                    cr = co.getId();
                }

                else
                {
                    cr = co.getString ( CObj.CREATOR );
                }

                log.info ( "attempt connection add node: " + cr );
                idlst.add ( cr );

                if ( dt.isConnected ( cr ) )
                {
                    log.info ( "   alrady connected." );
                    connected++;
                }

            }

            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

        hlst.close();
        log.info ( " existing connections valid for request: " + connected );

        if ( connected == 0 )
        {
            attemptOneConnection ( dt, idlst, myids );
        }

    }

    private void checkConnections()
    {
        Map<String, CObj> mymap = getMyIdMap();
        //Find the current file requests we have
        List<RequestFile> rl = fileHandler.listRequestFilesNE ( RequestFile.COMPLETE, 5 );

        for ( RequestFile rf : rl )
        {
            //Get the DestinationThread that matches the requesting id.
            DestinationThread dt = null;

            synchronized ( destinations )
            {
                dt = destinations.get ( rf.getRequestId() );
            }

            if ( dt != null )
            {
                if ( dt.numberConnection() < MAX_CONNECTIONS )
                {
                    CObjList hlst = index.getHasFiles ( rf.getCommunityId(),
                                                        rf.getWholeDigest(), rf.getFragmentDigest() );
                    attemptDestinationConnection ( hlst, dt, mymap );
                }

            }

        }

        //--- only members ---
        //Check hasfile requests (always initiate connections for highest prioty no
        //mater what.  If user is only working in one community don't waste connections
        //for other subscriptions.
        List<CommunityMember> reqfilelist = identManager.nextHasFileUpdate ( 5 );

        for ( CommunityMember cm : reqfilelist )
        {
            List<DestinationThread> destlist = findMyDestinationsForCommunity ( mymap, cm.getCommunityId() );

            for ( DestinationThread dt : destlist )
            {
                if ( dt.numberConnection() < MAX_CONNECTIONS )
                {
                    CObjList hlst = index.getSubscriptions ( cm.getCommunityId() );
                    //See how many of these nodes we're connected to
                    attemptDestinationConnection ( hlst, dt, mymap );
                }

            }

        }

        //Check post requests
        List<CommunityMember> reqpostlist = identManager.nextHasPostUpdate ( 5 );

        for ( CommunityMember cm : reqpostlist )
        {
            List<DestinationThread> destlist = findMyDestinationsForCommunity ( mymap, cm.getCommunityId() );

            for ( DestinationThread dt : destlist )
            {
                if ( dt.numberConnection() < MAX_CONNECTIONS )
                {
                    CObjList hlst = index.getSubscriptions ( cm.getCommunityId() );
                    //See how many of these nodes we're connected to
                    attemptDestinationConnection ( hlst, dt, mymap );
                }

            }

        }

        //Check subscription requests
        List<CommunityMember> reqsublist = identManager.nextHasSubscriptionUpdate ( 5 );
        log.info ( "subscription updates: " + reqsublist.size() );

        for ( CommunityMember cm : reqsublist )
        {
            List<DestinationThread> destlist = findAllMyDestinationsForCommunity ( mymap, cm.getCommunityId() );
            log.info ( "subscription update destinations: " + destlist.size() );

            for ( DestinationThread dt : destlist )
            {
                if ( dt.numberConnection() < MAX_CONNECTIONS )
                {
                    CObj com = index.getCommunity ( cm.getCommunityId() );

                    if ( CObj.SCOPE_PRIVATE.equals ( com.getString ( CObj.SCOPE ) ) )
                    {
                        CObjList hlst = index.getMemberships ( cm.getCommunityId() );
                        hlst.add ( com ); //attempt to connect to the community creator too
                        log.info ( "subscription update nodes to try: " + hlst.size() );
                        //See how many of these nodes we're connected to
                        attemptDestinationConnection ( hlst, dt, mymap );
                    }

                    else
                    {
                        CObjList hlst = index.getIdentities();
                        attemptDestinationConnection ( hlst, dt, mymap );
                    }

                }

            }

        }

        //Connect to the most reliable
        List<IdentityData> mrel = identManager.listMostReliable ( 100 );
        List<DestinationThread> dlst = getDestList();

        //Remove any node we're allready connected to from any destination
        for ( DestinationThread dt : dlst )
        {
            Iterator<IdentityData> i = mrel.iterator();

            while ( i.hasNext() )
            {
                IdentityData id = i.next();

                if ( dt.isConnected ( id.getId() ) )
                {
                    i.remove();
                }

            }

        }

        List<String> ids = new LinkedList<String>();

        for ( IdentityData id : mrel )
        {
            ids.add ( id.getId() );
        }

        for ( DestinationThread dt : dlst )
        {
            if ( dt.numberConnection() < MAX_CONNECTIONS )
            {
                attemptOneConnection ( dt, ids, mymap );
            }

        }

    }

    private void closeUselessConnections()
    {
        List<ConnectionThread> dlst = new LinkedList<ConnectionThread>();

        synchronized ( destinations )
        {
            for ( DestinationThread dt : destinations.values() )
            {
                dlst.addAll ( dt.getConnectionThreads() );
            }

        }

        for ( ConnectionThread ct : dlst )
        {
            long curtime = System.currentTimeMillis();
            long cuttime = curtime - MAX_TIME_WITH_NO_REQUESTS;

            if ( ct.getLastMyRequest() < cuttime )
            {
                ct.stop();
            }

        }

    }

    @Override
    public Object next ( String localdest, String remotedest )
    {
        Object r = null;
        //Find all subscriptions the remotedest has
        CObjList clst = index.getMemberSubscriptions ( remotedest );
        List<String> comlist = new LinkedList<String>();

        for ( int c = 0; c < clst.size(); c++ )
        {
            try
            {
                CObj co = clst.get ( c );
                String comid = co.getString ( CObj.COMMUNITYID );

                if ( comid != null )
                {
                    comlist.add ( comid );
                }

            }

            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

        clst.close();
        //Remove subscriptions that this localdest does not have
        List<String> ncomlst = new LinkedList<String>();
        clst = index.getMemberSubscriptions ( localdest );

        for ( int c = 0; c < clst.size(); c++ )
        {
            try
            {
                CObj co = clst.get ( c );
                String comid = co.getString ( CObj.COMMUNITYID );

                if ( comid != null && comlist.contains ( comid ) )
                {
                    ncomlst.add ( comid );
                }

            }

            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

        clst.close();
        comlist = ncomlst;
        log.info ( "common subscriptions for next request: " + comlist.size() );
        //Check if we want a file
        int doit = Utils.Random.nextInt ( 100 );

        if ( doit < 50 ) //50% chance
        {
            List<RequestFile> rflst = fileHandler.findFileListFrags ( localdest, 10L * 60L * 1000L );
            Iterator<RequestFile> it = rflst.iterator();

            while ( it.hasNext() && r == null )
            {
                RequestFile rf = it.next();
                CObj nhf = index.getIdentHasFile ( rf.getCommunityId(), remotedest,
                                                   rf.getWholeDigest(), rf.getFragmentDigest() );

                if ( nhf != null && "true".equals ( nhf.getString ( CObj.STILLHASFILE ) ) )
                {
                    if ( fileHandler.claimFileListClaim ( rf ) )
                    {
                        CObj cr = new CObj();
                        cr.setType ( CObj.CON_REQ_FRAGLIST );
                        cr.pushString ( CObj.COMMUNITYID, rf.getCommunityId() );
                        cr.pushString ( CObj.FILEDIGEST, rf.getWholeDigest() );
                        cr.pushString ( CObj.FRAGDIGEST, rf.getFragmentDigest() );
                        r = cr;
                    }

                }

            }

            //If there are no real fragments to request yet, then go ahead and request
            //fragment lists for ones we don't have yet.
            if ( r == null )
            {
                //First find the highest priority file we're tryiing to get
                //from the communities this node is subscribed to.
                List<RequestFile> rlst = fileHandler.findFileToGetFrags ( localdest );
                it = rlst.iterator();

                while ( it.hasNext() && r == null )
                {
                    //See if the remote dest has the file.
                    RequestFile rf = it.next();
                    CObj nhf = index.getIdentHasFile ( rf.getCommunityId(), remotedest,
                                                       rf.getWholeDigest(), rf.getFragmentDigest() );

                    if ( nhf != null && "true".equals ( nhf.getString ( CObj.STILLHASFILE ) ) )
                    {
                        //Find the fragments that haven't been requested yet.
                        CObjList cl = index.getFragmentsToRequest ( rf.getCommunityId(),
                                      rf.getWholeDigest(), rf.getFragmentDigest() );

                        if ( cl.size() == 0 )
                        {
                            //If there are no fragments that have not be requested yet,
                            //then let's reset the ones that in the req status, and not
                            //complete, in case we just failed to get it back after
                            //requesting.
                            cl.close();
                            cl = index.getFragmentsToReset ( rf.getCommunityId(),
                                                             rf.getWholeDigest(), rf.getFragmentDigest() );

                            for ( int c = 0; c < cl.size(); c++ )
                            {
                                try
                                {
                                    //Set to false, so that we'll request again.
                                    //Ones already complete won't be reset.
                                    CObj co = cl.get ( c );
                                    co.pushPrivate ( CObj.COMPLETE, "false" );
                                    index.index ( co );
                                }

                                catch ( IOException e )
                                {
                                    e.printStackTrace();
                                }

                            }

                            cl.close();
                            //Get the new list of fragments to request after resetting
                            cl = index.getFragmentsToRequest ( rf.getCommunityId(),
                                                               rf.getWholeDigest(), rf.getFragmentDigest() );

                            if ( cl.size() == 0 )
                            {
                                //We're done, but the RequestFile wasn't updated properly.  do it now.
                                if ( fileHandler.claimFileComplete ( rf ) )
                                {
                                    //Generate a HasFileRecord
                                    CObj hf = new CObj();
                                    hf.setType ( CObj.HASFILE );
                                    hf.pushString ( CObj.CREATOR, rf.getRequestId() );
                                    hf.pushString ( CObj.COMMUNITYID, rf.getCommunityId() );
                                    hf.pushString ( CObj.NAME, ( new File ( rf.getLocalFile() ) ).getName() );
                                    hf.pushText ( CObj.NAME, hf.getString ( CObj.NAME ) );
                                    hf.pushNumber ( CObj.FRAGSIZE, rf.getFragSize() );
                                    hf.pushNumber ( CObj.FILESIZE, rf.getFileSize() );
                                    hf.pushNumber ( CObj.FRAGNUMBER, rf.getFragsTotal() );
                                    hf.pushString ( CObj.STILLHASFILE, "true" );
                                    hf.pushString ( CObj.FILEDIGEST, rf.getWholeDigest() );
                                    hf.pushString ( CObj.FRAGDIGEST, rf.getFragmentDigest() );
                                    hf.pushPrivate ( CObj.LOCALFILE, rf.getLocalFile() );
                                    hfc.createHasFile ( hf );
                                    hfc.updateFileInfo ( hf );
                                }

                            }

                        }

                        if ( cl.size() > 0 )
                        {
                            try
                            {
                                CObj co = cl.get ( Utils.Random.nextInt ( cl.size() ) );
                                co.pushPrivate ( CObj.COMPLETE, "req" );
                                index.index ( co );
                                CObj sr = new CObj();
                                sr.setType ( CObj.CON_REQ_FRAG );
                                sr.pushString ( CObj.COMMUNITYID, co.getString ( CObj.COMMUNITYID ) );
                                sr.pushString ( CObj.FILEDIGEST, co.getString ( CObj.FILEDIGEST ) );
                                sr.pushString ( CObj.FRAGDIGEST, co.getString ( CObj.FRAGDIGEST ) );
                                sr.pushString ( CObj.FRAGDIG, co.getString ( CObj.FRAGDIG ) );
                                r = sr;
                            }

                            catch ( IOException e )
                            {
                                e.printStackTrace();
                            }

                        }

                        cl.close();
                    }

                }

            }

        }

        //Check if we want to get a has file
        if ( r == null )
        {
            doit = Utils.Random.nextInt ( 100 );

            if ( doit < 50 ) //25% chance
            {
                CommunityMember cm = identManager.claimHasFileUpdate ( comlist );

                if ( cm != null )
                {
                    log.info ( "send has file update request" );
                    CObj cr = new CObj();
                    cr.setType ( CObj.CON_REQ_HASFILE );
                    cr.pushString ( CObj.COMMUNITYID, cm.getCommunityId() );
                    cr.pushString ( CObj.CREATOR, cm.getMemberId() );
                    cr.pushNumber ( CObj.FIRSTNUM, cm.getLastFileNumber() + 1 );
                    cr.pushNumber ( CObj.LASTNUM, Long.MAX_VALUE );
                    r = cr;
                }

            }

        }

        //Check if we want to get posts
        if ( r == null )
        {
            doit = Utils.Random.nextInt ( 100 );

            if ( doit < 50 ) //12.5%
            {
                CommunityMember cm = identManager.claimPostUpdate ( comlist );

                if ( cm != null )
                {
                    CObj cr = new CObj();
                    cr.setType ( CObj.CON_REQ_POSTS );
                    cr.pushString ( CObj.COMMUNITYID, cm.getCommunityId() );
                    cr.pushString ( CObj.CREATOR, cm.getMemberId() );
                    cr.pushNumber ( CObj.FIRSTNUM, cm.getLastPostNumber() + 1 );
                    cr.pushNumber ( CObj.LASTNUM, Long.MAX_VALUE );
                    r = cr;
                }

            }

        }

        //Check if we want to get subscriptions
        if ( r == null )
        {
            doit = Utils.Random.nextInt ( 100 );

            if ( doit < 50 ) //6.25%
            {

                List<String> allmems = new LinkedList<String>();
                //See which communities the remote identity is a member
                //does not have to be subscribed
                CObjList memlst = index.getIdentityMemberships ( remotedest );

                for ( int c = 0; c < memlst.size(); c++ )
                {
                    try
                    {
                        CObj m = memlst.get ( c );
                        allmems.add ( m.getPrivate ( CObj.COMMUNITYID ) );
                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                memlst.close();
                //Get all the private communities created by this node.
                memlst = index.getIdentityPrivateCommunities ( remotedest );

                for ( int c = 0; c < memlst.size(); c++ )
                {
                    try
                    {
                        CObj m = memlst.get ( c );
                        allmems.add ( m.getDig() );
                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                memlst.close();

                //Only keep communities we are membership of
                List<String> mymems = new LinkedList<String>();
                memlst = index.getIdentityMemberships ( localdest );

                for ( int c = 0; c < memlst.size(); c++ )
                {
                    try
                    {
                        CObj m = memlst.get ( c );
                        String comid = m.getPrivate ( CObj.COMMUNITYID );

                        if ( comid != null && allmems.contains ( comid ) )
                        {
                            mymems.add ( comid );
                        }

                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                memlst.close();
                //Get all the private communities created by this node.
                memlst = index.getIdentityPrivateCommunities ( localdest );

                for ( int c = 0; c < memlst.size(); c++ )
                {
                    try
                    {
                        CObj m = memlst.get ( c );
                        String comid = m.getDig();

                        if ( comid != null && allmems.contains ( comid ) )
                        {
                            mymems.add ( m.getDig() );
                        }

                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                memlst.close();
                allmems = mymems;
                //Get all the public communities.  Everyone is a member of public communties.
                //Does not matter if not subscribed
                memlst = index.getPublicCommunities();

                for ( int c = 0; c < memlst.size(); c++ )
                {
                    try
                    {
                        CObj m = memlst.get ( c );
                        allmems.add ( m.getDig() );
                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                memlst.close();
                log.info ( "memberships in common for requesting subscription update: " + allmems.size() );
                CommunityMember cm = identManager.claimSubUpdate ( allmems );

                if ( cm != null )
                {
                    log.info ( "send subscription update request" );
                    CObj cr = new CObj();
                    cr.setType ( CObj.CON_REQ_SUBS );
                    cr.pushString ( CObj.COMMUNITYID, cm.getCommunityId() );
                    r = cr;
                }

            }

        }

        //Check if we want to get memberships
        if ( r == null )
        {
            doit = Utils.Random.nextInt ( 100 );

            if ( doit < 50 ) //3.125%
            {
                IdentityData id = identManager.claimMemberUpdate();

                if ( id != null )
                {
                    CObj cr = new CObj();
                    cr.setType ( CObj.CON_REQ_MEMBERSHIPS );
                    cr.pushString ( CObj.CREATOR, id.getId() );
                    cr.pushNumber ( CObj.FIRSTNUM, id.getLastMembershipNumber() + 1 );
                    cr.pushNumber ( CObj.LASTNUM, Long.MAX_VALUE );
                    r = cr;
                }

            }

        }

        //Check if we want to get communities
        if ( r == null )
        {
            doit = Utils.Random.nextInt ( 100 );

            if ( doit < 50 ) //1.5625%
            {
                IdentityData id = identManager.claimCommunityUpdate();

                if ( id != null )
                {
                    CObj cr = new CObj();
                    cr.setType ( CObj.CON_REQ_COMMUNITIES );
                    cr.pushString ( CObj.CREATOR, id.getId() );
                    cr.pushNumber ( CObj.FIRSTNUM, id.getLastCommunityNumber() + 1 );
                    cr.pushNumber ( CObj.LASTNUM, Long.MAX_VALUE );
                    r = cr;
                }

            }

        }

        //Check if we want to get identities
        if ( r == null ) //1.5625%
        {
            IdentityData id = identManager.claimIdentityUpdate ( remotedest );

            if ( id != null )
            {
                CObj cr = new CObj();
                cr.setType ( CObj.CON_REQ_IDENTITIES );
                r = cr;
            }

        }

        return r;
    }

    private void decodeMemberships()
    {
        //Find my memberships
        boolean newdecoded = false;
        List<CommunityMyMember> mycoms = identManager.getMyMemberships();

        for ( CommunityMyMember c : mycoms )
        {
            long lastdecode = c.getLastDecode();
            long newtime = System.currentTimeMillis();
            //Find all membership records we've received after this time.
            KeyParameter kp = new KeyParameter ( c.getKey() );
            CObjList unlst = index.getUnDecodedMemberships ( lastdecode );
            boolean decoded = false;

            for ( int cnt = 0; cnt < unlst.size() && !decoded; cnt++ )
            {
                try
                {
                    CObj um = unlst.get ( cnt );

                    if ( symdec.decode ( um, kp ) )
                    {
                        decoded = true;
                        newdecoded = true;
                        um.pushPrivate ( CObj.DECODED, "true" );
                        index.index ( um );
                    }

                }

                catch ( IOException e )
                {
                    e.printStackTrace();
                }

            }

            unlst.close();

            //See if we've validated our own membership yet.
            //it could be we got a new membership, but we didn't decode
            //any.  we still want to attempt to validate it.
            if ( !newdecoded )
            {
                CObjList mymem = index.getInvalidMembership ( c.getCommunityId(), c.getMemberId() );
                newdecoded = mymem.size() > 0;
                mymem.close();
            }

            c.setLastDecode ( newtime );
            identManager.saveMyMembership ( c ); //if we get one after we start we'll try again
        }

        if ( newdecoded )
        {
            //Search for all decoded, invalid memberships, and check if valid
            //keep checking until no more validated.
            int lastdec = 0;
            CObjList invliddeclist = index.getInvalidMemberships();

            while ( invliddeclist.size() != lastdec )
            {
                lastdec = invliddeclist.size();

                for ( int c = 0; c < invliddeclist.size(); c++ )
                {
                    try
                    {
                        CObj m = invliddeclist.get ( c );
                        String creator = m.getString ( CObj.CREATOR );
                        String comid = m.getPrivate ( CObj.COMMUNITYID );
                        String memid = m.getPrivate ( CObj.MEMBERID );
                        Long auth = m.getPrivateNumber ( CObj.AUTHORITY );

                        if ( creator != null && comid != null && auth != null )
                        {
                            CObj com = memvalid.canGrantMemebership ( comid, creator, auth );
                            CObj member = index.getIdentity ( memid );

                            if ( com != null )
                            {
                                m.pushPrivate ( CObj.VALIDMEMBER, "true" );
                                m.pushPrivate ( CObj.NAME, com.getPrivate ( CObj.NAME ) );
                                m.pushPrivate ( CObj.DESCRIPTION, com.getPrivate ( CObj.DESCRIPTION ) );

                                if ( "true".equals ( member.getPrivate ( CObj.MINE ) ) )
                                {
                                    m.pushPrivate ( CObj.MINE, "true" );
                                    com.pushPrivate ( memid, "true" );
                                    index.index ( com );
                                }

                                index.index ( m );
                            }

                        }

                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                invliddeclist.close();
                invliddeclist = index.getInvalidMemberships();
            }

            invliddeclist.close();
        }

    }

    public synchronized void stop()
    {
        stop = true;

        synchronized ( destinations )
        {
            for ( DestinationThread dt : destinations.values() )
            {
                dt.stop();
            }

        }

        notifyAll();
    }

    private synchronized void delay()
    {
        try
        {
            wait ( 1L * 1000L ); //one minute
        }

        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }

    }

    @Override
    public void run()
    {
        while ( !stop )
        {
            delay();

            if ( !stop )
            {
                checkConnections();
                decodeMemberships();
                closeUselessConnections();
            }

        }

    }

}

