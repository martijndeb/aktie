package aktie.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CObjListStringColumnLabelProvider extends ColumnLabelProvider
{

    private String key;

    public CObjListStringColumnLabelProvider ( String k )
    {
        key = k;
    }

    @Override
    public String getText ( Object element )
    {
    	CObjListGetter o = ( CObjListGetter ) element;
        String r = o.getCObj().getString ( key );

        if ( r == null )
        {
            r = "";
        }

        return r;
    }

}
