package aktie.gui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import aktie.data.DirectoryShare;

public class DirectoryShareLabelProvider extends  StyledCellLabelProvider
{

    @Override
    public void update ( ViewerCell cell )
    {
        DirectoryShare ds = ( DirectoryShare ) cell.getElement();
        cell.setText ( ds.getShareName() );

    }

}
