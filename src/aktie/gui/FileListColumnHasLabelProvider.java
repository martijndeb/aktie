package aktie.gui;

import aktie.data.FileInfo;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class FileListColumnHasLabelProvider extends ColumnLabelProvider
{

    @Override
    public String getText ( Object element )
    {
        FileListArrayElement o = ( FileListArrayElement ) element;
        FileInfo fi = o.getFileInfo();
        String r = "";

        if ( fi != null )
        {
            r = Long.toString ( fi.getNumberHasFile() );
        }

        return r;
    }

}
