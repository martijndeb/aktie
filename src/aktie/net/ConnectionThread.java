package aktie.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;

import aktie.BatchProcessor;
import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.data.RequestFile;
import aktie.gui.GuiCallback;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.json.CleanParser;
import aktie.user.IdentityManager;
import aktie.user.RequestFileHandler;
import aktie.utils.HasFileCreator;

public class ConnectionThread implements Runnable, GuiCallback
{
    Logger log = Logger.getLogger ( "aktie" );

    public static int MAXQUEUESIZE = 100; //Long lists should be in CObjList each one could have open indexreader!

    private boolean stop;
    private Connection con;
    private BatchProcessor preprocProcessor;
    private BatchProcessor inProcessor;
    private ConcurrentLinkedQueue<CObj> inQueue;
    private ConcurrentLinkedQueue<Object> outqueue;
    private GetSendData sendData;
    private OutputProcessor outproc;
    private CObj endDestination;
    private DestinationThread dest;
    private Index index;
    private HH2Session session;
    private GuiCallback guicallback;
    private OutputStream outstream;
    private HasFileCreator hfc;
    private RequestFileHandler fileHandler;
    private int listCount;
    private Set<String> accumulateTypes; //Types to combine in list before processing
    private List<CObj> currentList;
    private long inBytes;
    private long inNonFileBytes;
    private long outBytes;
    private ConnectionListener conListener;
    private ConnectionThread This;
    private IdentityManager IdentManager;
    private long lastMyRequest;

    public ConnectionThread ( DestinationThread d, HH2Session s, Index i, Connection c, GetSendData sd, GuiCallback cb, ConnectionListener cl, RequestFileHandler rf )
    {
        This = this;
        conListener = cl;
        guicallback = cb;
        sendData = sd;
        con = c;
        dest = d;
        index = i;
        session = s;
        fileHandler = rf;
        lastMyRequest = System.currentTimeMillis();
        IdentManager = new IdentityManager ( session, index );
        hfc = new HasFileCreator ( session, index );
        outqueue = new ConcurrentLinkedQueue<Object>();
        inQueue = new ConcurrentLinkedQueue<CObj>();
        accumulateTypes = new HashSet<String>();
        accumulateTypes.add ( CObj.FRAGMENT );
        preprocProcessor = new BatchProcessor();
        InIdentityProcessor ip = new InIdentityProcessor ( session, index, this );
        preprocProcessor.addProcessor ( new ConnectionValidatorProcessor ( ip, d, this ) );
        //!!!!!!!!!!!!!!!!!! InFragProcessor - should be first !!!!!!!!!!!!!!!!!!!!!!!
        //Otherwise the list of fragments will be interate though for preceding processors
        //that don't need to and time will be wasted.
        preprocProcessor.addProcessor ( new InFragProcessor ( session, index, this ) );
        preprocProcessor.addProcessor ( ip );
        preprocProcessor.addProcessor ( new InFileProcessor ( this ) );
        preprocProcessor.addProcessor ( new InComProcessor ( session, index, this ) );
        preprocProcessor.addProcessor ( new InHasFileProcessor ( dest.getIdentity(), session, index, this, hfc ) );
        preprocProcessor.addProcessor ( new InMemProcessor ( session, index, this ) );
        preprocProcessor.addProcessor ( new InPostProcessor ( dest.getIdentity(), session, index, this ) );
        preprocProcessor.addProcessor ( new InSubProcessor ( session, index, this ) );
        //!!!!!!!!!!!!!!!!! EnqueueRequestProcessor - must be last !!!!!!!!!!!!!!!!!!!!
        //Otherwise requests from the other node will not be processed.
        preprocProcessor.addProcessor ( new EnqueueRequestProcessor ( this ) );
        //These process requests from the other node.
        inProcessor = new BatchProcessor();
        inProcessor.addProcessor ( new ReqIdentProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqFragListProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqFragProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqComProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqHasFileProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqMemProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqPostsProcessor ( i, this ) );
        inProcessor.addProcessor ( new ReqSubProcessor ( i, this ) );
        outstream = con.getOutputStream();
        outproc = new OutputProcessor();
        Thread t = new Thread ( this );
        t.start();
    }

    public long getInBytes()
    {
        return inBytes;
    }

    public long getOutBytes()
    {
        return outBytes;
    }

    public CObj getEndDestination()
    {
        return endDestination;
    }

    public DestinationThread getLocalDestination()
    {
        return dest;
    }

    public void setEndDestination ( CObj o )
    {
        endDestination = o;
    }

    public void stop()
    {
        boolean wasstopped = stop;
        stop = true;
        outproc.go();
        dest.connectionClosed ( this );
        con.close();

        if ( !wasstopped )
        {
            CObj endd = getEndDestination();

            if ( endd != null )
            {
                IdentManager.connectionClose ( endd.getId(),
                                               getInNonFileBytes(),
                                               getInBytes(), getOutBytes() );
            }

            conListener.closed ( this );
        }

    }

    public boolean isStopped()
    {
        return stop;
    }

    public boolean enqueueRemoteRequest ( CObj o )
    {
        if ( inQueue.size() < MAXQUEUESIZE )
        {
            inQueue.add ( o );
            outproc.go();
            return true;
        }

        return false;
    }

    public boolean enqueue ( Object o )
    {
        if ( outqueue.size() < MAXQUEUESIZE )
        {
            outqueue.add ( o );
            outproc.go();
            return true;
        }

        if ( o instanceof CObjList )
        {
            CObjList l = ( CObjList ) o;
            l.close();
        }

        return false;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength ( int length )
    {
        this.length = length;
    }

    public void poke()
    {
        if ( outproc != null )
        {
            outproc.go();
        }

    }

    public void setLoadFile ( boolean loadFile )
    {
        this.loadFile = loadFile;
    }

    private void process()
    {
        CObj o = inQueue.poll();

        try
        {
            if ( o != null )
            {
                inProcessor.processCObj ( o );
            }

        }

        catch ( Exception e )
        {
            e.printStackTrace();
            stop();
        }

    }

    private class OutputProcessor implements Runnable
    {
        private int tick = 0;
        public OutputProcessor()
        {
            Thread t = new Thread ( this );
            t.start();
        }

        public synchronized void go()
        {
            notifyAll();
        }

        private synchronized void doWait()
        {
            if ( inQueue.size() == 0 && outqueue.size() == 0 )
            {
                try
                {
                    wait ( 60000 );
                }

                catch ( InterruptedException e )
                {
                }

            }

        }

        private Object getOutQueueData()
        {
            Object r = outqueue.poll();

            if ( r == null )
            {
                //Ok, process some of the requests from the other node.
                //Do this as late as possible so we don't have a bunch
                //of request data piling up waiting to go back.
                process();
                r = outqueue.poll();
            }

            return r;
        }

        private Object getLocalRequests()
        {
            if ( dest != null && endDestination != null && !loadFile )
            {
                Object r = sendData.next ( dest.getIdentity().getId(),
                                           endDestination.getId() );

                return r;
            }

            return null;
        }

        private Object getData()
        {
            Object r = null;

            //Try to make fair by alternating seeing if we have a request
            //to go out or if the other node has requested data to send
            //back first.
            if ( tick == 0 )
            {
                r = getOutQueueData();
                tick = 1;
            }

            else
            {
                r = getLocalRequests();
                tick = 0;
            }

            //Ok, now just do both in case the first one we picked above
            //had no data.
            if ( r == null )
            {
                r = getOutQueueData();
            }

            if ( r == null )
            {
                r = getLocalRequests();
            }

            return r;
        }

        private void sendCObj ( CObj c ) throws IOException
        {
            JSONObject ot = c.getJSON();
            String os = ot.toString();
            byte ob[] = os.getBytes ( "UTF-8" );
            outBytes += ob.length;
            outstream.write ( ob );
            outstream.flush();
        }

        private void sendCObjNoFlush ( CObj c ) throws IOException
        {
            JSONObject ot = c.getJSON();
            String os = ot.toString();
            byte ob[] = os.getBytes ( "UTF-8" );
            outBytes += ob.length;
            outstream.write ( ob );
        }

        private void seeIfUseless()
        {
            long curtime = System.currentTimeMillis();
            long cuttime = curtime - ConnectionManager.MAX_TIME_WITH_NO_REQUESTS;

            if ( lastMyRequest < cuttime )
            {
                stop();

            }

        }

        @Override
        public void run()
        {
            while ( !stop )
            {
                try
                {

                    seeIfUseless();

                    Object o = getData();

                    if ( o == null )
                    {
                        doWait();
                    }

                    else
                    {
                        if ( o instanceof CObj )
                        {
                            CObj c = ( CObj ) o;

                            if ( log.getLevel() == Level.INFO )
                            {
                                StringBuilder dbmsg = new StringBuilder();
                                dbmsg.append ( "CONTHREAD: Sending: " );
                                dbmsg.append ( c.getType() );

                                if ( dest.getIdentity() != null )
                                {
                                    dbmsg.append ( " from: " );
                                    dbmsg.append ( dest.getIdentity().getDisplayName() );
                                }

                                else
                                {
                                    dbmsg.append ( " from: null??" );
                                }

                                if ( endDestination != null )
                                {
                                    dbmsg.append ( " to: " );
                                    dbmsg.append ( endDestination.getDisplayName() );
                                }

                                else
                                {
                                    dbmsg.append ( " to: ?? " );
                                }

                                log.info ( dbmsg.toString() );
                            }

                            sendCObj ( c );

                            if ( CObj.FILEF.equals ( c.getType() ) )
                            {
                                String lfs = c.getPrivate ( CObj.LOCALFILE );
                                Long offset = c.getNumber ( CObj.FRAGOFFSET );
                                Long len = c.getNumber ( CObj.FRAGSIZE );

                                if ( lfs != null && offset != null && len != null )
                                {
                                    byte buf[] = new byte[1024];
                                    File lf = new File ( lfs );
                                    RandomAccessFile raf = new RandomAccessFile ( lf, "rw" );
                                    raf.seek ( offset );
                                    long ridx = 0;

                                    while ( ridx < len )
                                    {
                                        int l = raf.read ( buf, 0, Math.min ( buf.length,
                                                                              ( int ) ( len - ridx ) ) );

                                        if ( l < 0 )
                                        {
                                            throw new IOException ( "Oops." );
                                        }

                                        if ( l > 0 )
                                        {
                                            outstream.write ( buf, 0, l );
                                            outBytes += l;
                                            ridx += l;
                                        }

                                    }

                                    outstream.flush();
                                    raf.close();
                                }

                            }

                        }

                        else if ( o instanceof CObjList )
                        {
                            CObjList cl = ( CObjList ) o;
                            int len = cl.size();
                            CObj lo = new CObj();
                            lo.setType ( CObj.CON_LIST );
                            lo.pushNumber ( CObj.COUNT, len );
                            sendCObj ( lo );

                            for ( int c = 0; c < len; c++ )
                            {
                                sendCObjNoFlush ( cl.get ( c ) );
                            }

                            outstream.flush();

                            cl.close();
                        }

                        else
                        {
                            throw new RuntimeException ( "wtf? " + o.getClass().getName() );
                        }

                        conListener.update ( This );
                    }

                }

                catch ( Exception e )
                {
                    stop();
                }

            }

        }

    }

    private boolean loadFile;
    private int length;

    private void readFileData ( InputStream i ) throws IOException
    {
        if ( loadFile )
        {
            byte buf[] = new byte[1024];

            RIPEMD256Digest fdig = new RIPEMD256Digest();
            File tmpf = File.createTempFile ( "rxfile", ".dat" );
            FileOutputStream fos = new FileOutputStream ( tmpf );
            int rl = 0;

            while ( rl < length )
            {
                int len = i.read ( buf, 0, Math.min ( buf.length, length - rl ) );

                if ( len < 0 )
                {
                    stop();
                    fos.close();
                    throw new IOException ( "End of socket." );
                }

                if ( len > 0 )
                {
                    inBytes += len;
                    fdig.update ( buf, 0, len );
                    fos.write ( buf, 0, len );
                    rl += len;
                }

            }

            fos.close();
            byte expdig[] = new byte[fdig.getDigestSize()];
            fdig.doFinal ( expdig, 0 );
            String dstr = Utils.toString ( expdig );
            processFragment ( dstr, tmpf );
            loadFile = false;
            lastMyRequest = System.currentTimeMillis();
            outproc.go(); //it holds off on local requests until the file is read.
            //now we have it, tell outproc to go again.
        }

    }

    @SuppressWarnings ( "unchecked" )
    private void processFragment ( String dig, File fpart ) throws IOException
    {
        byte buf[] = new byte[1024];
        CObjList flist = index.getFragments ( dig );

        for ( int c = 0; c < flist.size(); c++ )
        {
            RandomAccessFile raf = null;
            FileInputStream fis = null;
            Session s = null;
            CObj fg = flist.get ( c );
            String wdig = fg.getString ( CObj.FILEDIGEST );
            String fdig = fg.getString ( CObj.FRAGDIGEST );
            Long fidx = fg.getNumber ( CObj.FRAGOFFSET );
            Long flen = fg.getNumber ( CObj.FRAGSIZE );
            String cplt = fg.getString ( CObj.COMPLETE );

            if ( wdig != null && fdig != null && fidx != null &&
                    flen != null && ( !"true".equals ( cplt ) ) )
            {
                try
                {
                    s = session.getSession();
                    Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE x.wholeDigest = :wdig "
                                              + "AND x.fragmentDigest = :fdig" );
                    q.setParameter ( "wdig", wdig );
                    q.setParameter ( "fdig", fdig );
                    List<RequestFile> lrf = q.list();
                    String lf = null;

                    for ( RequestFile rf : lrf )
                    {
                        s.getTransaction().begin();
                        rf = ( RequestFile ) s.get ( RequestFile.class, rf.getId() );
                        rf.setFragsComplete ( rf.getFragsComplete() + 1 );
                        s.merge ( rf );
                        //Copy the fragment to the whole file.
                        raf = new RandomAccessFile ( rf.getLocalFile(), "rw" );
                        fis = new FileInputStream ( fpart );
                        raf.seek ( fidx );
                        int ridx = 0;

                        while ( ridx < flen )
                        {
                            int len = fis.read ( buf, 0, Math.min ( buf.length, ( int ) ( flen - ridx ) ) );

                            if ( len < 0 )
                            {
                                fis.close();
                                raf.close();
                                throw new IOException ( "Oops." );
                            }

                            if ( len > 0 )
                            {
                                raf.write ( buf, 0, len );
                                ridx += len;
                            }

                        }

                        raf.close();
                        fis.close();
                        s.getTransaction().commit();
                        lf = rf.getLocalFile();
                        //If we're done, then create a new HasFile for us!
                    }

                    if ( lf != null )
                    {
                        fg.pushPrivate ( CObj.COMPLETE, "true" );
                        fg.pushPrivate ( CObj.LOCALFILE, lf );
                        index.index ( fg );
                    }

                    //Commit the transaction
                    //Ok now count how many fragments of each is done.
                    for ( RequestFile rf : lrf )
                    {
                        s.getTransaction().begin();
                        rf = ( RequestFile ) s.get ( RequestFile.class, rf.getId() );

                        if ( rf != null )
                        {
                            CObjList fdone = index.getFragmentsComplete ( rf.getCommunityId(),
                                             rf.getWholeDigest(), rf.getFragmentDigest() );
                            int numdone = fdone.size();
                            fdone.close();
                            rf.setFragsComplete ( numdone );
                            s.merge ( rf );
                            s.getTransaction().commit();

                            if ( rf.getFragsComplete() >= rf.getFragsTotal() )
                            {
                                if ( fileHandler.claimFileComplete ( rf ) )
                                {
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
                                    hf.pushPrivate ( CObj.UPGRADEFLAG, rf.isUpgrade() ? "true" : "false" );
                                    hfc.createHasFile ( hf );
                                    hfc.updateFileInfo ( hf );
                                    guicallback.update ( hf );

                                }

                            }

                            guicallback.update ( rf );
                        }

                        else
                        {
                            s.getTransaction().commit();
                        }

                    }

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

                            s.close();
                        }

                        catch ( Exception e2 )
                        {
                            e2.printStackTrace();
                        }

                    }

                    if ( raf != null )
                    {
                        try
                        {
                            raf.close();
                        }

                        catch ( Exception e2 )
                        {
                        }

                    }

                    if ( fis != null )
                    {
                        try
                        {
                            fis.close();
                        }

                        catch ( Exception e2 )
                        {
                        }

                    }

                }

            }

        }

        flist.close();
    }

    public static long LONGESTLIST = 100000000;

    @Override
    public void run()
    {
        try
        {
            InputStream is = con.getInputStream();
            CleanParser clnpar = new CleanParser ( is );

            while ( !stop )
            {
                JSONObject jo = clnpar.next();
                inBytes += clnpar.getBytesRead();
                inNonFileBytes += clnpar.getBytesRead();
                CObj r = new CObj();
                r.loadJSON ( jo );

                if ( CObj.CON_LIST.equals ( r.getType() ) )
                {
                    if ( currentList == null )
                    {
                        long lc = r.getNumber ( CObj.COUNT );

                        if ( lc > LONGESTLIST ) { stop(); }

                        listCount = ( int ) lc;
                    }

                    else
                    {
                        //This means they sent a new list while one was still
                        //going, this is a bad thing on them.
                        stop();
                    }

                }

                else
                {
                    if ( listCount > 0 )
                    {
                        if ( accumulateTypes.contains ( r.getType() ) )
                        {
                            if ( currentList == null )
                            {
                                currentList = new LinkedList<CObj>();
                            }

                            currentList.add ( r );
                            listCount--;

                        }

                        else
                        {
                            currentList = null;
                            listCount = 0;
                        }

                    }

                    //If we're populating a list, then don't process until
                    //we have the whole list, at which time listCount will be zero
                    //if we're not collecting a list listCount is always zero
                    if ( listCount == 0 )
                    {
                        if ( currentList == null )
                        {
                            //Not a list, just process it.
                            try
                            {
                                preprocProcessor.processCObj ( r );
                            }

                            catch ( Exception e )
                            {
                                //Make sure we can debug processing bugs
                                e.printStackTrace();
                                stop();
                            }

                        }

                        else
                        {
                            try
                            {
                                preprocProcessor.processObj ( currentList );
                            }

                            catch ( Exception e )
                            {
                                //Make sure we can debug processing bugs
                                e.printStackTrace();
                                stop();
                            }

                            currentList = null;
                        }

                    }

                    readFileData ( is );
                }

                conListener.update ( This );
            }

        }

        catch ( Exception e )
        {
        }

        stop();
    }

    public long getInNonFileBytes()
    {
        return inNonFileBytes;
    }

    public long getLastMyRequest()
    {
        return lastMyRequest;
    }

    @Override
    public void update ( Object o )
    {
        guicallback.update ( o );
        lastMyRequest = System.currentTimeMillis();
    }

}
