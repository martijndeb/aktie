package aktie.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import aktie.data.CObj;
import aktie.index.CObjList;
import aktie.index.Index;

public class LocalFileColumnLabelProvider extends ColumnLabelProvider
{

    private Index index;

    public LocalFileColumnLabelProvider()
    {

    }

    public void setIndex ( Index i )
    {
        index = i;
    }

    @Override
    public String getText ( Object element )
    {
        CObjListGetter o = ( CObjListGetter ) element;

        CObj pst = o.getCObj();

        if ( pst != null )
        {

            String lf = pst.getPrivate ( CObj.LOCALFILE );

            if ( lf == null )
            {

                String comid = pst.getString ( CObj.COMMUNITYID );
                String wdig = pst.getString ( CObj.FILEDIGEST );
                String pdig = pst.getString ( CObj.FRAGDIGEST );

                if ( comid != null && wdig != null && pdig != null )
                {

                    CObjList clst = index.getMyHasFiles ( comid, wdig, pdig );

                    if ( clst.size() > 0 )
                    {
                        try
                        {
                            CObj hr = clst.get ( 0 );
                            lf = hr.getPrivate ( CObj.LOCALFILE );

                            if ( lf != null )
                            {
                                pst.pushPrivate ( CObj.LOCALFILE, lf );
                                return lf;
                            }

                        }

                        catch ( Exception e )
                        {
                        }

                    }

                    clst.close();

                }

            }

            else
            {
                return lf;
            }

        }

        return "";

    }

}
