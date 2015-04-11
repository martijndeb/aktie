package aktie.utils;

import java.io.File;
import java.io.IOException;

import org.hibernate.Session;

import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.CommunityMember;
import aktie.data.FileInfo;
import aktie.data.HH2Session;
import aktie.index.CObjList;
import aktie.index.Index;

public class HasFileCreator
{

    private HH2Session session;
    private Index index;
    private SubscriptionValidator validator;

    public HasFileCreator ( HH2Session s, Index i )
    {
        index = i;
        session = s;
        validator = new SubscriptionValidator ( index );
    }

    public FileInfo getFileInfo ( CObj f )
    {
        return null;
    }

    public FileInfo getFileInfo ( String wholedig, String fragdig )
    {
        return null;
    }

    public FileInfo getFileInfo ( String id )
    {
        return null;
    }

    public void updateDownloadRequested ( CObj hasfile )
    {

    }

    /**
        Called whenever we get a hasfile record
        @param f
    */
    public void updateFileInfo ( CObj f )
    {
        if ( !CObj.HASFILE.equals ( f.getType() ) )
        {
            throw new RuntimeException ( "This should only be called with hasfile." );
        }

        //Create FILE type CObj.  only index if new
        String comid = f.getString ( CObj.COMMUNITYID );
        Long filesize = f.getNumber ( CObj.FILESIZE );
        Long fragsize = f.getNumber ( CObj.FRAGSIZE );
        Long fragnumber = f.getNumber ( CObj.FRAGNUMBER );
        String digofdigs = f.getString ( CObj.FRAGDIGEST );
        String wholedig = f.getString ( CObj.FILEDIGEST );
        String name = f.getString ( CObj.NAME );
        String localfile = f.getPrivate ( CObj.LOCALFILE );
        String txtname = f.getString ( CObj.TXTNAME );
        String stillhas = f.getString ( CObj.STILLHASFILE );

        if ( txtname == null )
        {
            txtname = name;
        }

        if ( digofdigs != null && wholedig != null && name != null && comid != null && filesize != null &&
                fragsize != null && fragnumber != null )
        {
            String id = Utils.mergeIds ( comid, digofdigs, wholedig );
            CObj fi = new CObj();
            fi.setId ( id );
            fi.setType ( CObj.FILE );
            fi.pushString ( CObj.COMMUNITYID, comid );
            fi.pushString ( CObj.FILEDIGEST, wholedig );
            fi.pushString ( CObj.FRAGDIGEST, digofdigs );
            fi.pushString ( CObj.NAME, name );
            fi.pushNumber ( CObj.FILESIZE, filesize );
            fi.pushNumber ( CObj.FRAGSIZE, fragsize );
            fi.pushNumber ( CObj.FRAGNUMBER, fragnumber );
            fi.pushText ( CObj.TXTNAME, txtname );

            
            try
            {
                if ( localfile != null )
                {
                    if ( "false".equals ( stillhas ) )
                    {
                        localfile = "";
                    }

                    fi.pushString ( CObj.LOCALFILE, localfile );
                    index.index ( fi );
                }

                else
                {
                    index.index ( fi, true );
                }

            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

            //Count the hasfiles there are, and see if we have the file.
            CObjList wl = index.getHasFiles ( comid, wholedig, digofdigs );
            int numberhasfile = wl.size();
            boolean mine = false;

            for ( int c = 0; c < numberhasfile; c++ )
            {
                try
                {
                    CObj mhf = wl.get ( c );

                    if ( mhf.getPrivate ( CObj.LOCALFILE ) != null )
                    {
                        mine = true;
                    }

                }

                catch ( IOException e )
                {
                    e.printStackTrace();
                }

            }

            wl.close();
            //Save the FileInfo object
            Session s = null;

            try
            {
                s = session.getSession();
                s.getTransaction().begin();
                FileInfo info = ( FileInfo ) s.get ( FileInfo.class, id );

                if ( info == null )
                {
                    info = new FileInfo();
                    info.setId ( id );
                    info.setCommunityId ( comid );
                    info.setFragmentDigest ( digofdigs );
                    info.setWholeDigest ( wholedig );
                }

                info.setHasLocal ( mine );
                info.setNumberHasFile ( numberhasfile );
                s.merge ( info );
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

    }

    public boolean createHasFile ( CObj o )
    {
        //Set File sequence number for the community/creator
        String creator = o.getString ( CObj.CREATOR );
        String comid = o.getString ( CObj.COMMUNITYID );

        String digofdigs = o.getString ( CObj.FRAGDIGEST );
        String wholedig = o.getString ( CObj.FILEDIGEST );

        CObj myid = validator.isMyUserSubscribed ( comid, creator );

        if ( myid == null ) { return false; }

        String id = Utils.mergeIds ( creator, comid );
        String hasfileid = Utils.mergeIds ( id, digofdigs, wholedig );

        o.setId ( hasfileid ); //only 1 has file per user per community per file digest

        Session s = null;

        try
        {
            s = session.getSession();
            s.getTransaction().begin();
            CommunityMember m = ( CommunityMember ) s.get ( CommunityMember.class, id );

            if ( m == null )
            {
                m = new CommunityMember();
                m.setId ( id );
                m.setCommunityId ( comid );
                m.setMemberId ( creator );
                s.persist ( m );
            }

            long num = m.getLastFileNumber();
            num++;
            o.pushNumber ( CObj.SEQNUM, num );
            o.pushPrivate ( CObj.MINE, "true" );
            m.setLastFileNumber ( num );
            s.merge ( m );
            s.getTransaction().commit();
            s.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();
            o.pushString ( CObj.ERROR, "Bad error: " + e.getMessage() );
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

            return false;
        }

        //Make the path absolute to help with queries based on the file
        //name later.
        String lf = o.getPrivate ( CObj.LOCALFILE );
        File f = new File ( lf );

        if ( f.exists() )
        {
            try
            {
                o.pushPrivate ( CObj.LOCALFILE, f.getCanonicalPath() );
            }

            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

        //Set the created on time
        o.pushNumber ( CObj.CREATEDON, System.currentTimeMillis() );
        //Sign it.
        o.sign ( Utils.privateKeyFromString ( myid.getPrivate ( CObj.PRIVATEKEY ) ) );

        try
        {
            index.index ( o );
        }

        catch ( Exception e )
        {
            e.printStackTrace();
            o.pushString ( CObj.ERROR, "File record could not be indexed" );
            return false;
        }

        return true;
    }

}
