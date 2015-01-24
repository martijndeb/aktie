package aktie.net;

import java.util.logging.Logger;

import aktie.GenericProcessor;
import aktie.data.CObj;
import aktie.index.CObjList;
import aktie.index.Index;

public class ReqSubProcessor extends GenericProcessor
{

    Logger log = Logger.getLogger ( "aktie" );

    private Index index;
    private ConnectionThread connection;

    public ReqSubProcessor ( Index i, ConnectionThread c )
    {
        index = i;
        connection = c;
    }

    @Override
    public boolean process ( CObj b )
    {
        String type = b.getType();

        if ( CObj.CON_REQ_SUBS.equals ( type ) )
        {
            String comid = b.getString ( CObj.COMMUNITYID );
            String conid = connection.getEndDestination().getId();
            CObj community = index.getCommunity ( comid );

            if ( community != null && conid != null )
            {
                boolean ok = false;
                String creator = community.getString ( CObj.CREATOR );

                if ( CObj.SCOPE_PUBLIC.equals ( community.getString ( CObj.SCOPE ) ) )
                {
                    ok = true;
                }

                else if ( conid.equals ( creator ) )
                {
                    ok = true;
                }

                else
                {
                    CObjList mem = index.getMembership ( comid, conid );

                    if ( mem.size() > 0 )
                    {
                        ok = true;
                    }

                    mem.close();
                }

                if ( ok )
                {
                    CObjList cl = index.getSubsUnsubs ( comid );
                    connection.enqueue ( cl );
                }

                else
                {
                    log.warning ( "Requested subscriptions without membership!" );
                }

            }

            return true;
        }

        return false;
    }

}
