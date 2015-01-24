package aktie.user;

import org.hibernate.Session;

import aktie.GenericProcessor;
import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.CommunityMember;
import aktie.data.HH2Session;
import aktie.gui.GuiCallback;
import aktie.index.Index;
import aktie.utils.SubscriptionValidator;

public class NewPostProcessor extends GenericProcessor
{


    private GuiCallback guicallback;
    private Index index;
    private HH2Session session;
    private SubscriptionValidator validator;

    public NewPostProcessor ( HH2Session s, Index i, GuiCallback cb )
    {
        session = s;
        index = i;
        guicallback = cb;
        validator = new SubscriptionValidator ( index );
    }

    /**
        must set:
        type: post
        string: creator, community

    */
    @Override
    public boolean process ( CObj o )
    {
        String type = o.getType();

        if ( CObj.POST.equals ( type ) )
        {
            //Get the creator and make sure it is this user
            String creator = o.getString ( CObj.CREATOR );
            String comid = o.getString ( CObj.COMMUNITYID );
            String id = Utils.mergeIds ( creator, comid );
            CObj myid = validator.isMyUserSubscribed ( comid, creator );

            if ( myid == null )
            {
                o.pushString ( CObj.ERROR, "You must be subscribed" );
                guicallback.update ( o );
                return true;
            }

            //Set the sequence number
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

                long num = m.getLastPostNumber();
                num++;
                o.pushNumber ( CObj.SEQNUM, num );
                m.setLastPostNumber ( num );
                s.merge ( m );
                s.getTransaction().commit();
                s.close();
            }

            catch ( Exception e )
            {
                e.printStackTrace();
                o.pushString ( CObj.ERROR, "Bad error: " + e.getMessage() );
                guicallback.update ( o );

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

                return true;
            }

            //Sign it.
            o.sign ( Utils.privateKeyFromString ( myid.getPrivate ( CObj.PRIVATEKEY ) ) );

            try
            {
                index.index ( o );
            }

            catch ( Exception e )
            {
                e.printStackTrace();
                o.pushString ( CObj.ERROR, "Subscription could not be indexed" );
                guicallback.update ( o );
                return true;
            }

            guicallback.update ( o );
        }

        return false;
    }

}
