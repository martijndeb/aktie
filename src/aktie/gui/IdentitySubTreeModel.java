package aktie.gui;

import java.util.HashMap;
import java.util.Map;

import aktie.data.CObj;
import aktie.index.Index;

public class IdentitySubTreeModel
{

    private Map<String, CObj> identities;
    private Map<String, Map<String, CObj>> subCommunities;
    private Index index;

    public IdentitySubTreeModel ( Index i )
    {
        index = i;
        identities = new HashMap<String, CObj>();
        subCommunities = new HashMap<String, Map<String, CObj>>();
    }

    public void update ( CObj c )
    {
        if ( CObj.IDENTITY.equals ( c.getType() ) )
        {
            identities.put ( c.getId(), c );
            Map<String, CObj> sm = subCommunities.get ( c.getId() );

            if ( sm == null )
            {
                subCommunities.put ( c.getId(), new HashMap<String, CObj>() );
            }

        }

        if ( CObj.SUBSCRIPTION.equals ( c.getType() ) )
        {
            String cid = c.getString ( CObj.CREATOR );
            Map<String, CObj> sm = subCommunities.get ( cid );

            if ( sm == null )
            {
                sm = new HashMap<String, CObj>();
                subCommunities.put ( cid, sm );
            }

            if ( "true".equals ( c.getString ( CObj.SUBSCRIBED ) ) )
            {
                CObj com = index.getCommunity ( c.getString ( CObj.COMMUNITYID ) );

                if ( com != null )
                {
                    sm.put ( com.getDig(), com );
                }

            }

            else
            {
                sm.remove ( c.getString ( CObj.COMMUNITYID ) );
            }

        }

    }

    public Map<String, CObj> getIdentities()
    {
        return identities;
    }

    public Map<String, Map<String, CObj>> getSubCommunities()
    {
        return subCommunities;
    }

    public Index getIndex()
    {
        return index;
    }

}
