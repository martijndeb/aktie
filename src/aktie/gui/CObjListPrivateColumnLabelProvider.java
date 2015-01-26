package aktie.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CObjListPrivateColumnLabelProvider extends ColumnLabelProvider
{

    private String key;

    public CObjListPrivateColumnLabelProvider ( String k )
    {
        key = k;
    }

    @Override
    public String getText ( Object element )
    {
    	CObjListGetter o = ( CObjListGetter ) element;
        return o.getCObj().getPrivate ( key );
    }

}
