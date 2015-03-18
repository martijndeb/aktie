package aktie.gui;

import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import aktie.data.CObj;

public class CObjListDateColumnLabelProvider extends ColumnLabelProvider
{

    private String key;

    public CObjListDateColumnLabelProvider ( String k )
    {
        key = k;
    }

    @Override
    public String getText ( Object element )
    {
        CObjListGetter o = ( CObjListGetter ) element;
        CObj co = o.getCObj();

        if ( co != null )
        {
            Long cl = co.getNumber ( key );

            if ( cl != null )
            {
                return ( new Date ( o.getCObj().getNumber ( key ) ) ).toString();
            }

        }

        return "";
    }

}
