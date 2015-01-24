package aktie.gui;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CObjListDisplayNameColumnLabelProvider extends ColumnLabelProvider
{

    @Override
    public String getText ( Object element )
    {
        CObjListArrayElement o = ( CObjListArrayElement ) element;
        return o.getCObj().getDisplayName();
    }

}
