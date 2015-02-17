package aktie.gui;

import java.util.Date;

import aktie.data.CObj;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;

public class NewPostDialog extends Dialog
{
    private Text subject;
    private Label lblPostingToCommunity;
    private Label lblNewLabel;
    private CObj postIdentity;
    private CObj community;
    private SWTApp app;
    private StyledText postBody;
    private CObj fileRef;

    /**
        Create the dialog.
        @param parentShell
    */
    public NewPostDialog ( Shell parentShell, SWTApp a )
    {
        super ( parentShell );
        setShellStyle ( getShellStyle() | SWT.RESIZE );
        app = a;
    }

    private void selectIdentity ( CObj id, CObj com )
    {
        postIdentity = id;
        community = com;

        if ( app != null )
        {
            if ( lblPostingToCommunity != null && !lblPostingToCommunity.isDisposed() &&
                    lblNewLabel != null && !lblNewLabel.isDisposed() &&
                    community != null && postIdentity != null )
            {
                lblPostingToCommunity.setText ( "Posting to community: " + community.getPrivateDisplayName() );
                lblNewLabel.setText ( "Posting as: " + postIdentity.getDisplayName() );
            }

        }

    }

    public void open ( CObj id, CObj comid, CObj fileref )
    {
        selectIdentity ( id, comid );
        fileRef = fileref;
        super.open();
    }

    /**
        Create contents of the dialog.
        @param parent
    */
    @Override
    protected Control createDialogArea ( Composite parent )
    {
        Composite container = ( Composite ) super.createDialogArea ( parent );
        container.setLayout ( new GridLayout ( 3, false ) );
        new Label ( container, SWT.NONE );
        new Label ( container, SWT.NONE );

        lblPostingToCommunity = new Label ( container, SWT.NONE );
        lblPostingToCommunity.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, false, false, 1, 1 ) );
        lblPostingToCommunity.setText ( "Posting to community: " );
        new Label ( container, SWT.NONE );
        new Label ( container, SWT.NONE );

        lblNewLabel = new Label ( container, SWT.NONE );
        lblNewLabel.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, false, false, 1, 1 ) );
        lblNewLabel.setText ( "Posting as:" );
        new Label ( container, SWT.NONE );

        Label lblSubject = new Label ( container, SWT.NONE );
        lblSubject.setLayoutData ( new GridData ( SWT.RIGHT, SWT.CENTER, false, false, 1, 1 ) );
        lblSubject.setText ( "Subject" );

        subject = new Text ( container, SWT.BORDER );
        subject.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
        new Label ( container, SWT.NONE );

        Label lblBody = new Label ( container, SWT.NONE );
        lblBody.setLayoutData ( new GridData ( SWT.LEFT, SWT.CENTER, false, true, 1, 1 ) );
        lblBody.setText ( "Body" );

        postBody = new StyledText ( container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        postBody.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        //scrolledComposite.setContent(bodyText);
        //scrolledComposite.setMinSize(bodyText.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        selectIdentity ( postIdentity, community );

        return container;
    }

    /**
        Create contents of the button bar.
        @param parent
    */
    @Override
    protected void createButtonsForButtonBar ( Composite parent )
    {
        Button button = createButton ( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                                       true );
        button.setText ( "Post" );
        createButton ( parent, IDialogConstants.CANCEL_ID,
                       IDialogConstants.CANCEL_LABEL, false );
    }

    @Override
    protected void okPressed()
    {
        if ( postIdentity != null && community != null && app != null )
        {
            CObj p = new CObj();
            p.setType ( CObj.POST );
            p.pushString ( CObj.CREATOR, postIdentity.getId() );
            p.pushString ( CObj.CREATOR_NAME, postIdentity.getDisplayName() );
            p.pushString ( CObj.COMMUNITYID, community.getDig() );
            p.pushString ( CObj.COMMUNITY_NAME, community.getPrivateDisplayName() );
            p.pushString ( CObj.SUBJECT, subject.getText() );
            p.pushNumber ( CObj.CREATEDON, ( new Date() ).getTime() );
            p.pushText ( CObj.BODY, postBody.getText() );

            if ( fileRef != null )
            {
                p.pushString ( CObj.NAME, fileRef.getString ( CObj.NAME ) );
                p.pushNumber ( CObj.FILESIZE, fileRef.getNumber ( CObj.FILESIZE ) );
                p.pushString ( CObj.FRAGDIGEST, fileRef.getString ( CObj.FRAGDIGEST ) );
                p.pushNumber ( CObj.FRAGSIZE, fileRef.getNumber ( CObj.FRAGSIZE ) );
                p.pushNumber ( CObj.FRAGNUMBER, fileRef.getNumber ( CObj.FRAGNUMBER ) );
                p.pushString ( CObj.FILEDIGEST, fileRef.getString ( CObj.FILEDIGEST ) );
            }

            app.getNode().enqueue ( p );
        }

        super.okPressed();
    }

    /**
        Return the initial size of the dialog.
    */
    @Override
    protected Point getInitialSize()
    {
        return new Point ( 497, 365 );
    }

    public Label getLblPostingToCommunity()
    {
        return lblPostingToCommunity;
    }

    public Label getLblNewLabel()
    {
        return lblNewLabel;
    }

    public Text getSubject()
    {
        return subject;
    }

    public StyledText getPostBody()
    {
        return postBody;
    }

}
