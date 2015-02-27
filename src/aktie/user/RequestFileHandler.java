package aktie.user;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.logging.Logger;

import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.data.RequestFile;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.utils.FUtils;

public class RequestFileHandler
{
    Logger log = Logger.getLogger ( "aktie" );

    private HH2Session session;
    private Index index;
    private File downloadDir;
    private NewFileProcessor nfp;


    public RequestFileHandler ( HH2Session s, String downdir, NewFileProcessor n, Index i )
    {
        index = i;
        nfp = n;
        session = s;
        downloadDir = new File ( downdir );

        if ( !downloadDir.exists() )
        {
            downloadDir.mkdirs();
        }

    }

    public void cancelDownload ( RequestFile f )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();

            RequestFile rf = ( RequestFile ) s.get ( RequestFile.class, f.getId() );
            s.delete ( rf );

            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

    }

    public void setPriority ( RequestFile f, int pri )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            RequestFile rf = ( RequestFile ) s.get ( RequestFile.class, f.getId() );
            rf.setPriority ( pri );
            s.merge ( rf );
            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

    }

    @SuppressWarnings ( "unchecked" )
    public void deleteOldRequests ( long oldest )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE x.requestedOn < :old AND "
                                      + " ( x.state = :st OR  x.state = :sts ) " );
            q.setParameter ( "old", System.currentTimeMillis() - oldest );
            q.setParameter ( "st", RequestFile.REQUEST_FRAG_LIST );
            q.setParameter ( "sts", RequestFile.REQUEST_FRAG_LIST_SNT );
            List<RequestFile> l = q.list();

            for ( RequestFile rf : l )
            {
                s.delete ( rf );
            }

            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

    }

    @SuppressWarnings ( "unchecked" )
    public List<RequestFile> findFileToGetFrags ( String myid )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE "
                                      + "x.state = :st AND x.requestId = :rid ORDER BY "
                                      + "x.priority DESC, x.lastRequest ASC" );
            q.setParameter ( "st", RequestFile.REQUEST_FRAG );
            q.setParameter ( "rid", myid );
            q.setMaxResults ( 100 );
            List<RequestFile> l = q.list();//LOCKED HERE
            s.close();
            return l;
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return null;
    }

    public boolean claimFileListClaim ( RequestFile rf )
    {
        boolean claimed = false;
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            RequestFile r = ( RequestFile ) s.get ( RequestFile.class, rf.getId() );

            if ( RequestFile.REQUEST_FRAG_LIST == r.getState() ||
                    RequestFile.REQUEST_FRAG_LIST_SNT == r.getState() )
            {
                r.setState ( RequestFile.REQUEST_FRAG_LIST_SNT );
                r.setLastRequest ( System.currentTimeMillis() );
                s.merge ( r );
                claimed = true;
            }

            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            claimed = false;
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return claimed;
    }

    @SuppressWarnings ( "unchecked" )
    public void setRequestedOn()
    {
        Session s = null;

        long today = System.currentTimeMillis();

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE x.requestedOn is null OR "
                                      + "x.requestedOn = 0" );
            q.setMaxResults ( 500 );
            List<RequestFile> l = q.list();

            for ( RequestFile rf : l )
            {
                rf.setRequestedOn ( today );
                s.merge ( rf );
            }

            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

    }

    @SuppressWarnings ( "unchecked" )
    public List<RequestFile> findFileListFrags ( String rid, long backtime )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE x.requestId = :rid AND "
                                      + "(x.state = :st OR "
                                      + "   (x.state = :sts AND x.lastRequest < :rt) "
                                      + ") ORDER BY "
                                      + "x.priority DESC, x.lastRequest ASC" );
            q.setParameter ( "rid",  rid );
            q.setParameter ( "st", RequestFile.REQUEST_FRAG_LIST );
            q.setParameter ( "sts", RequestFile.REQUEST_FRAG_LIST_SNT );
            q.setParameter ( "rt", System.currentTimeMillis() - backtime );
            q.setMaxResults ( 500 );
            List<RequestFile> l = q.list();
            s.close();
            return l;
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return null;
    }

    @SuppressWarnings ( "unchecked" )
    public List<RequestFile> listRequestFiles ( int state, int max )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE "
                                      + "x.state = :st ORDER BY x.priority DESC" );
            q.setParameter ( "st", state );
            q.setMaxResults ( max );
            List<RequestFile> r = q.list();
            s.close();
            return r;
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return new LinkedList<RequestFile>();
    }

    @SuppressWarnings ( "unchecked" )
    public List<RequestFile> listRequestFilesNE ( int state, int max )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE "
                                      + "x.state != :st AND x.priority > 0 ORDER BY x.priority DESC, x.state DESC" );
            q.setParameter ( "st", state );
            q.setMaxResults ( max );
            List<RequestFile> r = q.list();
            s.close();
            return r;
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return new LinkedList<RequestFile>();
    }

    public boolean claimFileComplete ( RequestFile rf )
    {
        boolean climaed = false;
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            RequestFile nf = ( RequestFile ) s.get ( RequestFile.class, rf.getId() );

            if ( nf.getState() != RequestFile.COMPLETE )
            {
                nf.setState ( RequestFile.COMPLETE );
                nf.setFragsComplete ( nf.getFragsTotal() );
                s.merge ( nf );
                climaed = true;
            }

            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            climaed = false;
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return climaed;
    }

    private RequestFile createRF ( CObj hasfile, File lf, int state, int comp )
    {
        Session s = null;

        try
        {
            Long pri = hasfile.getNumber ( CObj.PRIORITY );
            int priority = 5;

            if ( pri != null )
            {
                long prl = pri;
                priority = ( int ) prl;
            }

            boolean upgrade = false;
            String upstr = hasfile.getPrivate ( CObj.UPGRADEFLAG );
            log.info ( "Download requested.  upgrade flag: " + upstr );

            if ( "true".equals ( upstr ) )
            {
                upgrade = true;
            }

            RequestFile rf = new RequestFile();
            rf.setRequestedOn ( System.currentTimeMillis() );
            rf.setUpgrade ( upgrade );
            rf.setCommunityId ( hasfile.getString ( CObj.COMMUNITYID ) );
            rf.setFileSize ( hasfile.getNumber ( CObj.FILESIZE ) );
            rf.setFragmentDigest ( hasfile.getString ( CObj.FRAGDIGEST ) );
            rf.setFragSize ( hasfile.getNumber ( CObj.FRAGSIZE ) );
            rf.setFragsTotal ( hasfile.getNumber ( CObj.FRAGNUMBER ) );
            rf.setWholeDigest ( hasfile.getString ( CObj.FILEDIGEST ) );
            rf.setRequestId ( hasfile.getString ( CObj.CREATOR ) );
            rf.setPriority ( priority );
            rf.setLocalFile ( lf.getPath() );
            rf.setState ( state );
            rf.setFragsComplete ( comp );
            s = session.getSession();
            s.getTransaction().begin();
            s.merge ( rf );
            s.getTransaction().commit();
            s.close();
            return rf;
        }

        catch ( Exception e )
        {
            e.printStackTrace();

            if ( s != null )
            {
                try
                {
                    if ( s.getTransaction().isActive() )
                    {
                        s.getTransaction().rollback();
                    }

                }

                catch ( Exception e2 )
                {
                }

                try
                {
                    s.close();
                }

                catch ( Exception e2 )
                {
                }

            }

        }

        return null;
    }

    public RequestFile createRequestFile ( CObj hasfile )
    {
        if ( CObj.USR_DOWNLOAD_FILE.equals ( hasfile.getType() ) )
        {

            String comid = hasfile.getString ( CObj.COMMUNITYID ) ;
            String pdig = hasfile.getString ( CObj.FRAGDIGEST ) ;
            String wdig = hasfile.getString ( CObj.FILEDIGEST ) ;
            String creator = hasfile.getString ( CObj.CREATOR ) ;

            boolean okcreate = true;
            String localpath = null;

            try
            {
                //Check if we already have it
                CObjList myhlst = index.getMyHasFiles ( wdig, pdig );
                log.info ( "Number of matching files: " + myhlst.size() );

                for ( int i = 0; i < myhlst.size(); i++ )
                {
                    CObj hf = myhlst.get ( i );

                    if ( hf != null )
                    {
                        String tcr = hf.getString ( CObj.CREATOR );
                        String tcm = hf.getString ( CObj.COMMUNITYID );
                        String lp = hf.getPrivate ( CObj.LOCALFILE );
                        String cdig = FUtils.digWholeFile ( lp );
                        log.info ( "Checking if matches: " + cdig );

                        if ( cdig != null && cdig.equals ( wdig ) )
                        {
                            localpath = lp;
                            log.info ( "Digest matches: " + localpath );

                            if ( creator.equals ( tcr ) && comid.equals ( tcm ) )
                            {
                                log.info ( "We already have it!" );
                                okcreate = false;
                            }

                        }

                    }

                }

                myhlst.close();

            }

            catch ( Exception e )
            {
            }


            if ( okcreate )
            {
                //check if for some reason the existing localfile is actually
                //the correct file, if not and it exists anyway rename the current
                //file to backup.
                String lfs = hasfile.getPrivate ( CObj.LOCALFILE );
                log.info ( "Existing localfile: " + lfs );

                if ( lfs != null )
                {
                    String dig = FUtils.digWholeFile ( lfs );

                    if ( wdig.equals ( dig ) )
                    {
                        //We already have the file defined in the localfile.  set localpath to
                        //the locafile value.  It won't copy over itself bellow.
                        localpath = lfs;
                    }

                    else
                    {
                        //Make a back-up file of the existing
                        File back = new File ( lfs + ".backup" );
                        File lf = new File ( lfs );

                        if ( lf.exists() )
                        {
                            lf.renameTo ( back );
                        }

                        try
                        {
                            lf.createNewFile();
                        }

                        catch ( IOException e )
                        {
                            e.printStackTrace();
                        }

                    }

                }

                if ( localpath != null )
                {
                    log.info ( "Localpath exists: " + localpath );

                    if ( nfp != null )
                    {
                        //If localfile is set then we should copy the existing file. :(
                        //when localfile is set it means the user wants it saved to this
                        //specific file
                        if ( lfs != null )
                        {
                            File t = new File ( lfs );
                            File s = new File ( localpath );

                            try
                            {
                                FUtils.copy ( s, t );
                                localpath = t.getPath();
                            }

                            catch ( IOException e )
                            {
                                e.printStackTrace();
                            }

                        }

                        //We have it in another group.  Create a new hasfile for this
                        CObj nhf = new CObj();
                        nhf.setType ( CObj.HASFILE );
                        nhf.pushString ( CObj.COMMUNITYID, comid );
                        nhf.pushString ( CObj.CREATOR, creator );
                        nhf.pushPrivate ( CObj.LOCALFILE, localpath );
                        nfp.process ( nhf );

                        File lf = new File ( localpath );
                        long fn = hasfile.getNumber ( CObj.FRAGNUMBER );
                        log.info ( "SAVE requestfile: " + fn );
                        return createRF ( hasfile, lf, RequestFile.COMPLETE, ( int ) fn );
                    }

                }

                else
                {

                    File lf = null;

                    //The localfile value is set.
                    if ( lfs != null )
                    {
                        lf = new File ( lfs );
                    }

                    log.info ( "LF: " + lf );

                    if ( lf == null )
                    {
                        String filename = hasfile.getString ( CObj.NAME );
                        lf = new File ( downloadDir.getPath() + File.separator + filename );

                        int idx = 0;
                        String fname = filename;
                        String ext = "";
                        Matcher m = Pattern.compile ( "(.+)\\.(\\w+)$" ).matcher ( filename );

                        if ( m.find() )
                        {
                            fname = m.group ( 1 );
                            ext = m.group ( 2 );
                        }

                        log.info ( "File name: " + fname + " ext: " + ext );

                        while ( lf.exists() )
                        {
                            lf = new File ( downloadDir.getPath() + File.separator + fname + "." + idx +
                                            "." + ext );
                            log.info ( "Check file: " + lf );
                            idx++;
                        }

                        try
                        {
                            lf.createNewFile();
                        }

                        catch ( IOException e )
                        {
                            e.printStackTrace();
                        }

                    }

                    return createRF ( hasfile, lf, RequestFile.REQUEST_FRAG_LIST, 0 );
                }

            }

        }

        return null;
    }

}
