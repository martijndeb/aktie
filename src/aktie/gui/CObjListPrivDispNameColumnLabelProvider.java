package aktie.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CObjListPrivDispNameColumnLabelProvider extends ColumnLabelProvider
{

    @Override
    public String getText ( Object element )
    {
        CObjListGetter o = ( CObjListGetter ) element;
        return o.getCObj().getPrivateDisplayName();
    }

}
