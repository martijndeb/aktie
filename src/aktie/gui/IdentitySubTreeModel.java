package aktie.gui;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import aktie.data.CObj;
import aktie.index.Index;

public class IdentitySubTreeModel
{

    private SortedMap<String, CObj> identities;
    private SortedMap<String, SortedMap<String, CObj>> subCommunities;
    private Index index;

    public IdentitySubTreeModel ( Index i )
    {
        index = i;
        identities = new TreeMap<String, CObj>();
        subCommunities = new TreeMap<String, SortedMap<String, CObj>> ( new Comparator<String>()
        {
            @Override
            public int compare ( String o1, String o2 )
            {
                CObj co1 = identities.get ( o1 );
                CObj co2 = identities.get ( o2 );

                if ( co1 != null && co2 != null )
                {
                    String n1 = co1.getDisplayName();
                    String n2 = co2.getDisplayName();

                    if ( n1 != null && n2 != null )
                    {
                        return n1.compareTo ( n2 );
                    }

                }

                return 0;
            }

        } );

    }

    public void update ( CObj c )
    {
        if ( CObj.IDENTITY.equals ( c.getType() ) )
        {
            identities.put ( c.getId(), c );
            SortedMap<String, CObj> sm = subCommunities.get ( c.getId() );

            if ( sm == null )
            {
                sm = new TreeMap<String, CObj> ( new Comparator<String>()
                {

                    @Override
                    public int compare ( String o1, String o2 )
                    {
                        CObj co1 = index.getByDig ( o1 );
                        CObj co2 = index.getByDig ( o2 );

                        if ( co1 != null && co2 != null )
                        {
                            String n1 = co1.getPrivateDisplayName();
                            String n2 = co2.getPrivateDisplayName();

                            if ( n1 != null && n2 != null )
                            {
                                return n1.compareTo ( n2 );
                            }

                        }

                        return 0;
                    }

                } );

                subCommunities.put ( c.getId(), sm );
            }

        }

        if ( CObj.SUBSCRIPTION.equals ( c.getType() ) )
        {
            String cid = c.getString ( CObj.CREATOR );
            SortedMap<String, CObj> sm = subCommunities.get ( cid );

            if ( sm == null )
            {
                sm = new TreeMap<String, CObj> ( new Comparator<String>()
                {

                    @Override
                    public int compare ( String o1, String o2 )
                    {
                        CObj co1 = index.getByDig ( o1 );
                        CObj co2 = index.getByDig ( o2 );

                        if ( co1 != null && co2 != null )
                        {
                            String n1 = co1.getPrivateDisplayName();
                            String n2 = co2.getPrivateDisplayName();

                            if ( n1 != null && n2 != null )
                            {
                                return n1.compareTo ( n2 );
                            }

                        }

                        return 0;
                    }

                } );

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

    public Map<String, SortedMap<String, CObj>> getSubCommunities()
    {
        return subCommunities;
    }

    public Index getIndex()
    {
        return index;
    }

}
