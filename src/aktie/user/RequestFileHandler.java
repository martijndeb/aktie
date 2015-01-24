package aktie.user;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.data.RequestFile;

public class RequestFileHandler
{

    private HH2Session session;
    private File downloadDir;

    public RequestFileHandler ( HH2Session s, String downdir )
    {
        session = s;
        downloadDir = new File ( downdir );

        if ( !downloadDir.exists() )
        {
            downloadDir.mkdirs();
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

            if ( RequestFile.REQUEST_FRAG_LIST == r.getState() )
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
    public List<RequestFile> findFileListFrags ( String rid, long backtime )
    {
        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            Query q = s.createQuery ( "SELECT x FROM RequestFile x WHERE x.requestId = :rid AND"
                                      + "(x.state = :st OR "
                                      + "   (x.state = :sts AND x.lastRequest < :rt) "
                                      + ") ORDER BY "
                                      + "x.priority DESC, x.lastRequest ASC" );
            q.setParameter ( "rid",  rid );
            q.setParameter ( "st", RequestFile.REQUEST_FRAG_LIST );
            q.setParameter ( "sts", RequestFile.REQUEST_FRAG_LIST_SNT );
            q.setParameter ( "rt", System.currentTimeMillis() - backtime );
            q.setMaxResults ( 100 );
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

    public RequestFile createRequestFile ( CObj hasfile )
    {
        if ( CObj.USR_DOWNLOAD_FILE.equals ( hasfile.getType() ) )
        {
            Session s = null;

            try
            {
                File lf = null;
                String lfs = hasfile.getPrivate ( CObj.LOCALFILE );

                if ( lfs != null )
                {
                    lf = new File ( lfs );
                }

                if ( lf == null )
                {
                    String filename = hasfile.getString ( CObj.NAME );
                    lf = new File ( downloadDir.getPath() + File.separator + filename );

                    if ( !lf.exists() )
                    {
                        lf.createNewFile();
                    }

                }

                Long pri = hasfile.getNumber ( CObj.PRIORITY );
                int priority = 5;

                if ( pri != null )
                {
                    long prl = pri;
                    priority = ( int ) prl;
                }

                RequestFile rf = new RequestFile();
                rf.setCommunityId ( hasfile.getString ( CObj.COMMUNITYID ) );
                rf.setFileSize ( hasfile.getNumber ( CObj.FILESIZE ) );
                rf.setFragmentDigest ( hasfile.getString ( CObj.FRAGDIGEST ) );
                rf.setFragSize ( hasfile.getNumber ( CObj.FRAGSIZE ) );
                rf.setFragsTotal ( hasfile.getNumber ( CObj.FRAGNUMBER ) );
                rf.setWholeDigest ( hasfile.getString ( CObj.FILEDIGEST ) );
                rf.setRequestId ( hasfile.getString ( CObj.CREATOR ) );
                rf.setPriority ( priority );
                rf.setLocalFile ( lf.getPath() );
                rf.setState ( RequestFile.REQUEST_FRAG_LIST );
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

        }

        return null;
    }

}
