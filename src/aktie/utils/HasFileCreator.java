package aktie.utils;

import java.io.File;
import java.io.IOException;

import org.hibernate.Session;

import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.CommunityMember;
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
        String share = f.getString ( CObj.SHARE_NAME );

        if ( txtname == null )
        {
            txtname = name;
        }

        if ( digofdigs != null && wholedig != null && name != null && comid != null && filesize != null &&
                fragsize != null && fragnumber != null )
        {

            CObjList wl = index.getHasFiles ( comid, wholedig, digofdigs );
            int numberhasfile = wl.size();
            wl.close();

            String id = Utils.mergeIds ( comid, digofdigs, wholedig );
            CObj fi = index.getFileInfo ( id );

            if ( fi == null )
            {
                fi = new CObj();
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
            }

            fi.pushNumber ( CObj.NUMBER_HAS, numberhasfile );

            try
            {
                if ( localfile != null )
                {
                    if ( "false".equals ( stillhas ) )
                    {
                        fi.pushString(CObj.STATUS, "");
                        localfile = "";
                    }
                    else {
                        fi.pushString(CObj.STATUS, "done");
                    }

                    fi.pushString ( CObj.LOCALFILE, localfile );
                    fi.pushString ( CObj.SHARE_NAME, share );
                }

                index.index ( fi );

            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }


        }

    }

    public static String getCommunityMemberId ( String creator, String comid )
    {
        return Utils.mergeIds ( creator, comid );
    }

    public static String getHasFileId ( String commemid, String digofdigs, String wholedig )
    {
        String hasfileid = Utils.mergeIds ( commemid, digofdigs, wholedig );
        return hasfileid;
    }

    public static String getHasFileId ( String creator, String comid, String digofdigs, String wholedig )
    {
        String id = getCommunityMemberId ( creator, comid );
        return getHasFileId ( id, digofdigs, wholedig );
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

        String id = getCommunityMemberId ( creator, comid );
        String hasfileid = getHasFileId ( id, digofdigs, wholedig );

        o.setId ( hasfileid ); //only 1 has file per user per community per file digest
        //This is an upgrade.  We have to make adjustments for this
        //to be null when validating signatures for old hasfile records

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

        if ( lf != null )
        {
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
