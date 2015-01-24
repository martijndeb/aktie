package aktie.net;

import java.io.IOException;

import org.hibernate.Session;

import aktie.GenericProcessor;
import aktie.crypto.Utils;
import aktie.data.CObj;
import aktie.data.CommunityMember;
import aktie.data.HH2Session;
import aktie.gui.GuiCallback;
import aktie.index.Index;
import aktie.utils.DigestValidator;
import aktie.utils.HasFileCreator;
import aktie.utils.SubscriptionValidator;

public class InHasFileProcessor extends GenericProcessor
{

    private GuiCallback guicallback;
    private Index index;
    private HH2Session session;
    private DigestValidator validator;
    private SubscriptionValidator subvalid;
    private CObj destIdent;
    private HasFileCreator hfc;

    public InHasFileProcessor ( CObj id, HH2Session s, Index i, GuiCallback cb, HasFileCreator h )
    {
        hfc = h;
        destIdent = id;
        index = i;
        session = s;
        guicallback = cb;
        validator = new DigestValidator ( index );
        subvalid = new SubscriptionValidator ( index );
    }

    @Override
    public boolean process ( CObj b )
    {
        String type = b.getType();

        if ( CObj.HASFILE.equals ( type ) )
        {
            if ( validator.newAndValid ( b ) )
            {
                Long seqnum = b.getNumber ( CObj.SEQNUM );
                String creatorid = b.getString ( CObj.CREATOR );
                String comid = b.getString ( CObj.COMMUNITYID );
                String wdig = b.getString ( CObj.FILEDIGEST );
                String ddig = b.getString ( CObj.FRAGDIGEST );

                if ( comid != null && creatorid != null && wdig != null && ddig != null && seqnum != null )
                {
                    String id = Utils.mergeIds ( creatorid, comid );
                    CObj mysubid = subvalid.isMyUserSubscribed ( comid, destIdent.getId() );
                    CObj sid = subvalid.isUserSubscribed ( comid, creatorid );

                    if ( mysubid != null && sid != null )
                    {
                        Session s = null;

                        try
                        {
                            s = session.getSession();
                            s.getTransaction().begin();
                            CommunityMember m = ( CommunityMember )
                                                s.get ( CommunityMember.class, id );

                            if ( m == null )
                            {
                                m = new CommunityMember();
                                m.setId ( id );
                                m.setCommunityId ( comid );
                                m.setMemberId ( creatorid );
                                s.persist ( m );
                            }

                            if ( m.getLastFileNumber() + 1 == ( long ) seqnum )
                            {
                                m.setLastFileNumber ( seqnum );
                                s.merge ( m );
                            }

                            s.getTransaction().commit();
                            s.close();
                            index.index ( b );
                            hfc.updateFileInfo ( b );
                            guicallback.update ( b );
                        }

                        catch ( IOException e )
                        {
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

            }

        }

        return false;
    }

}
