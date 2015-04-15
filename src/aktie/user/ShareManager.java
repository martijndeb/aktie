package aktie.user;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import aktie.ProcessQueue;
import aktie.data.CObj;
import aktie.data.DirectoryShare;
import aktie.data.HH2Session;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.utils.FUtils;
import aktie.utils.HasFileCreator;

public class ShareManager implements Runnable
{

    private Index index;
    private HasFileCreator hfc;
    private ProcessQueue userQueue;
    private HH2Session session;

    public ShareManager ( HH2Session s, Index i, HasFileCreator h, ProcessQueue pq )
    {
        session = s;
        index = i;
        hfc = h;
        userQueue = pq;
        Thread t = new Thread ( this );
        t.setDaemon ( true );
        t.start();
    }

    private void addFile ( DirectoryShare s, File f )
    {
        CObj hf = new CObj();
        hf.setType ( CObj.HASFILE );
        hf.pushString ( CObj.CREATOR, s.getMemberId() );
        hf.pushString ( CObj.COMMUNITYID, s.getCommunityId() );
        hf.pushString ( CObj.SHARE_NAME, s.getShareName() );
        hf.pushPrivate ( CObj.LOCALFILE, f.getPath() ); //Canonical name gotten during processing
        userQueue.enqueue ( hf );
    }

    private void checkFoundFile ( DirectoryShare s, File f )
    {
        String fp = f.getAbsolutePath();

        try
        {
            fp = f.getCanonicalPath();
        }

        catch ( IOException e )
        {
            e.printStackTrace();
        }

        CObjList mlst = index.getLocalHasFiles ( s.getCommunityId(), s.getMemberId(), fp );

        if ( mlst.size() == 0 )
        {
            addFile ( s, f );
        }

        mlst.close();
    }

    /*
        If we search for all share files first.  Files renamed or moved
        in s share will already have thie hasfile record updated with the
        new filename before this is run.  When this is run for all hasfile
        records, it should only find files that have been deleted from a
        share, so we set them as stillhas false.
    */
    private void checkHasFile ( CObj hf )
    {
        String lf = hf.getPrivate ( CObj.LOCALFILE );
        String wd = hf.getString ( CObj.FILEDIGEST );

        if ( lf != null && wd != null )
        {
            File f = new File ( lf );
            boolean remove = true;

            if ( f.exists() )
            {
                String rdig = FUtils.digWholeFile ( lf );

                if ( wd.equals ( rdig ) )
                {
                    remove = false;
                }

            }

            if ( remove )
            {
                hf.pushString ( CObj.STILLHASFILE, "false" );
                hfc.createHasFile ( hf );
                hfc.updateFileInfo ( hf );
            }

        }

    }

    private void crawlDirectory ( DirectoryShare s, File df )
    {
        if ( df != null && df.exists() && df.isDirectory() )
        {
            File lsd[] = df.listFiles();

            for ( int c = 0; c < lsd.length; c++ )
            {
                File f = lsd[c];

                if ( f.exists() )
                {
                    if ( f.isDirectory() )
                    {
                        s.setNumberSubFolders ( s.getNumberSubFolders() + 1 );
                        crawlDirectory ( s, f );
                    }

                    else if ( f.isFile() )
                    {
                        s.setNumberFiles ( s.getNumberFiles() + 1 );
                        checkFoundFile ( s, f );
                    }

                }

            }

        }

        else
        {
            s.setMessage ( "Not a directory: " + df );
        }

    }

    private void crawlShare ( DirectoryShare s )
    {
        String ds = s.getDirectory();

        if ( ds != null )
        {
            File df = new File ( ds );
            crawlDirectory ( s, df );
        }

        else
        {
            s.setMessage ( "Directory not set." );
        }

    }

    @SuppressWarnings ( "unchecked" )
    private void processShares()
    {
        Session s = null;

        try
        {
            s = session.getSession();
            List<DirectoryShare> l = s.createCriteria ( DirectoryShare.class ).list();

            for ( DirectoryShare ds : l )
            {
                ds.setNumberSubFolders ( 0 );
                ds.setNumberFiles ( 0 );
                crawlShare ( ds );
                saveShare ( s, ds );
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

    private void checkAllHasFile()
    {
        CObjList myhf = index.getAllMyHasFiles();

        try
        {
            for ( int c = 0; c < myhf.size(); c++ )
            {
                CObj hf = myhf.get ( c );
                checkHasFile ( hf );
            }

        }

        catch ( Exception e )
        {
            e.printStackTrace();
        }

        myhf.close();
    }

    private void saveShare ( Session s, DirectoryShare d )
    {
        try
        {
            s.getTransaction().begin();
            s.merge ( d );
            s.getTransaction().commit();
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

            }

        }

    }


    @SuppressWarnings ( "unchecked" )
    public List<DirectoryShare> listShares ( String comid, String memid )
    {
        Session s = null;
        List<DirectoryShare> r = new LinkedList<DirectoryShare>();

        try
        {
            s = session.getSession();
            Query q = s.createQuery ( "SELECT x FROM DirectoryShare x WHERE "
                                      + "x.communityId = :comid AND "
                                      + "x.memberId = :memid" );
            q.setParameter ( "comid", comid );
            q.setParameter ( "memid", memid );
            r = q.list();

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

        return r;

    }

    @SuppressWarnings ( "unchecked" )
    public void deleteShare ( String comid, String memid, String name )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();

            Query q = s.createQuery ( "SELECT x FROM DirectoryShare x WHERE "
                                      + "x.shareName = :name AND "
                                      + "x.communityId = :comid AND x.memberId = :memid" );
            q.setParameter ( "name", name );
            q.setParameter ( "comid", comid );
            q.setParameter ( "memid", memid );
            List<DirectoryShare> sl = q.list();

            for ( int c = 0; c < sl.size(); c++ )
            {
                DirectoryShare d = sl.get ( c );
                s.delete ( d );
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
    public void addShare ( String comid, String memid, String name, String dir )
    {
        String conn = null;
        File sd = new File ( dir );

        if ( sd.exists() )
        {
            try
            {
                conn = sd.getCanonicalPath();
            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

        }

        if ( conn != null )
        {
            Session s = null;

            try
            {
                s = session.getSession();
                s.getTransaction().begin();

                DirectoryShare d = null;
                Query q = s.createQuery ( "SELECT x FROM DirectoryShare x WHERE "
                                          + "( x.directory = :path OR x.shareName = :name ) AND "
                                          + "x.communityId = :comid AND x.memberId = :memid" );
                q.setParameter ( "name", name );
                q.setParameter ( "path", conn );
                q.setParameter ( "comid", comid );
                q.setParameter ( "memid", memid );
                List<DirectoryShare> sl = q.list();

                if ( sl.size() > 0 )
                {
                    d = sl.get ( 0 );
                }

                if ( d == null )
                {
                    d = new DirectoryShare();
                }

                d.setCommunityId ( comid );
                d.setDirectory ( conn );
                d.setMemberId ( memid );
                d.setShareName ( name );

                s.merge ( d );

                s.getTransaction().commit();
                s.close();

                newshare = true;
                go();
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

    }

    private boolean stop = false;
    private boolean newshare = false;

    public synchronized void go()
    {
        notifyAll();
    }

    public synchronized void stop()
    {
        stop = true;
        notifyAll();
    }

    public static long SHARE_DELAY = 1L * 60L * 60L * 1000L;

    public synchronized void delay()
    {
        try
        {
            wait ( SHARE_DELAY );
        }

        catch ( Exception e )
        {
        }

    }

    public void run()
    {
        while ( !stop )
        {
            newshare = false;
            processShares();
            checkAllHasFile();

            if ( !newshare )
            {
                delay();
            }

        }

    }


}
