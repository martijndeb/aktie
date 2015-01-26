package aktie.gui;

import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;

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
        return ( new Date ( o.getCObj().getNumber ( key ) ) ).toString();
    }

}
