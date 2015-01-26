package aktie.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.layout.GridData;

import aktie.data.CObj;

public class ShowMembersDialog extends Dialog
{
    private Table memberTable;
    private TableViewer memberTableViewer;
    private Label lblMembersOfCommunity;
    private SWTApp app;

    /**
        Create the dialog.
        @param parentShell
    */
    public ShowMembersDialog ( SWTApp a, Shell parentShell )
    {
        super ( parentShell );
        app = a;
    }

    /**
        Create contents of the dialog.
        @param parent
    */
    @Override
    protected Control createDialogArea ( Composite parent )
    {
        Composite container = ( Composite ) super.createDialogArea ( parent );
        container.setLayout ( new GridLayout ( 1, false ) );

        lblMembersOfCommunity = new Label ( container, SWT.NONE );
        lblMembersOfCommunity.setText ( "Members of Community: " );

        memberTableViewer = new TableViewer ( container, SWT.BORDER |
                                              SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL );
        memberTableViewer.setContentProvider ( new CObjListIdentPrivContentProvider (
                app.getNode().getIndex(), CObj.MEMBERID ) );
        memberTable = memberTableViewer.getTable();
        memberTable.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        memberTable.setHeaderVisible ( true );
        memberTable.setLinesVisible ( true );

        TableViewerColumn col0 = new TableViewerColumn ( memberTableViewer, SWT.NONE );
        col0.getColumn().setText ( "Identity" );
        col0.getColumn().setWidth ( 100 );
        col0.setLabelProvider ( new CObjListDisplayNameColumnLabelProvider() );

        lblSubscribers = new Label ( container, SWT.NONE );
        lblSubscribers.setText ( "Subscribers" );

        subTableViewer = new TableViewer ( container, SWT.BORDER | SWT.FULL_SELECTION |
                                           SWT.H_SCROLL | SWT.V_SCROLL );
        subTableViewer.setContentProvider ( new CObjListIdentPrivContentProvider (
                                                app.getNode().getIndex(), CObj.CREATOR ) );
        subTable = subTableViewer.getTable();
        subTable.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        subTable.setHeaderVisible ( true );
        subTable.setLinesVisible ( true );

        TableViewerColumn scol0 = new TableViewerColumn ( memberTableViewer, SWT.NONE );
        scol0.getColumn().setText ( "Identity" );
        scol0.getColumn().setWidth ( 100 );
        scol0.setLabelProvider ( new CObjListDisplayNameColumnLabelProvider() );

        return container;
    }

    /**
        Create contents of the button bar.
        @param parent
    */
    @Override
    protected void createButtonsForButtonBar ( Composite parent )
    {
        createButton ( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                       true );
        createButton ( parent, IDialogConstants.CANCEL_ID,
                       IDialogConstants.CANCEL_LABEL, false );
    }

    private CObj selectedCommunity;
    private Table subTable;
    private TableViewer subTableViewer;
    private Label lblSubscribers;

    public void setCommunity ( CObj com )
    {
        selectedCommunity = com;

        if ( selectedCommunity != null )
        {
            String name = selectedCommunity.getPrivate ( CObj.NAME );
            lblMembersOfCommunity.setText ( "Members of Community: " + name );
            //TODO here
        }

    }

    /**
        Return the initial size of the dialog.
    */
    @Override
    protected Point getInitialSize()
    {
        return new Point ( 450, 402 );
    }

    public Table getMemberTable()
    {
        return memberTable;
    }

    public TableViewer getMemberTableViewer()
    {
        return memberTableViewer;
    }

    public Label getLblMembersOfCommunity()
    {
        return lblMembersOfCommunity;
    }

    public Table getSubTable()
    {
        return subTable;
    }

    public TableViewer getSubTableViewer()
    {
        return subTableViewer;
    }

}
