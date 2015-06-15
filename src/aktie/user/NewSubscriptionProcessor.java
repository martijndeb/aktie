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

public class NewSubscriptionProcessor extends GenericProcessor
{

    private GuiCallback guicallback;
    private Index index;
    private HH2Session session;
    private SubscriptionValidator validator;

    public NewSubscriptionProcessor ( HH2Session s, Index i, GuiCallback cb )
    {
        session = s;
        index = i;
        guicallback = cb;
        validator = new SubscriptionValidator ( index );
    }

    @Override
    public boolean process ( CObj o )
    {
        String type = o.getType();

        if ( CObj.SUBSCRIPTION.equals ( type ) )
        {
            //Get the creator and make sure it is this user
            String creator = o.getString ( CObj.CREATOR );

            if ( creator == null )
            {
                o.pushString ( CObj.ERROR, "creator must be defined" );
                guicallback.update ( o );
                return true;
            }

            CObj myid = index.getMyIdentity ( creator );

            if ( myid == null )
            {
                o.pushString ( CObj.ERROR, "you may only use your own identity" );
                guicallback.update ( o );
                return true;
            }

            //See if we can subscribe
            String comid = o.getString ( CObj.COMMUNITYID );

            if ( comid == null )
            {
                o.pushString ( CObj.ERROR, "community must be defined" );
                guicallback.update ( o );
                return true;
            }

            if ( !validator.canSubscribe ( comid, creator ) )
            {
                o.pushString ( CObj.ERROR, "may not subscribe" );
                guicallback.update ( o );
                return true;
            }

            //Create the id value.
            String id = Utils.mergeIds ( creator, comid );
            o.setId ( id );
            //get the sequence number
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

                long num = m.getLastSubscriptionNumber();
                num++;
                o.pushNumber ( CObj.SEQNUM, num );
                m.setLastSubscriptionNumber ( num );
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
            o.pushPrivate ( CObj.MINE, "true" );
            o.pushPrivate ( CObj.PRV_PUSH_REQ, "true" );
            o.pushPrivateNumber ( CObj.PRV_PUSH_TIME, System.currentTimeMillis() );

            o.sign ( Utils.privateKeyFromString ( myid.getPrivate ( CObj.PRIVATEKEY ) ) );

            try
            {
                //Set the ID, so we always overwrite the last subscription
                index.index ( o );
            }

            catch ( Exception e )
            {
                e.printStackTrace();
                o.pushString ( CObj.ERROR, "subscription could not be indexed" );
                guicallback.update ( o );
                return true;
            }

            guicallback.update ( o );
        }

        return false;
    }

}
