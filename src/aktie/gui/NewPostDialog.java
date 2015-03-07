package aktie.gui;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static int REPLY_WRAP_LENGTH = 80;

    private Text subject;
    private Label lblPostingToCommunity;
    private Label lblNewLabel;
    private CObj postIdentity;
    private CObj community;
    private SWTApp app;
    private StyledText postBody;
    private CObj fileRef;
    private CObj prvFileRef;
    private CObj replyPost;
    private Text file1Text;
    private Text file2Text;

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

    public static String formatDisplay ( String body, boolean quote )
    {
        if ( body != null )
        {
            StringBuilder rb = new StringBuilder();
            rb.append ( "\n" );
            String lines[] = body.split ( "\r\n|\n" );

            Matcher mt = Pattern.compile ( "^> " ).matcher ( "" );

            for ( int c = 0; c < lines.length; c++ )
            {
                String bln = lines[c];
                mt.reset ( bln );

                if ( !mt.find() )
                {
                    if ( bln.length() == 0 )
                    {
                        if ( quote )
                        {
                            rb.append ( "> " );
                        }

                        rb.append ( "\n" );
                    }

                    Matcher whitemat = Pattern.compile ( "\\s+" ).matcher ( "" );
                    int cn = 0;

                    while ( cn < bln.length() )
                    {

                        if ( quote )
                        {
                            rb.append ( "> " );
                        }

                        int end = Math.min ( cn + REPLY_WRAP_LENGTH, bln.length() );
                        whitemat.reset ( bln.substring ( end - 1, end ) );
                        boolean wspc = whitemat.find();

                        while ( !wspc && end < bln.length() )
                        {
                            end++;
                            whitemat.reset ( bln.substring ( end - 1, end ) );
                            wspc = whitemat.find();
                        }

                        if ( wspc )
                        {
                            while ( wspc && end < bln.length() )
                            {
                                end++;
                                whitemat.reset ( bln.substring ( end - 1, end ) );
                                wspc = whitemat.find();
                            }

                            if ( !wspc ) { end--; }

                        }

                        rb.append ( bln.substring ( cn, end ) );
                        rb.append ( "\n" );
                        cn = end;
                    }

                }

                else
                {
                    if ( quote )
                    {
                        rb.append ( "> " );
                    }

                    rb.append ( bln );
                    rb.append ( "\n" );
                }

            }

            return rb.toString();

        }

        return "";

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

                if ( replyPost != null )
                {
                    String fdig = replyPost.getString ( CObj.FILEDIGEST );

                    if ( fdig != null )
                    {
                        fileRef = replyPost;
                    }

                    fdig = replyPost.getString ( CObj.PRV_FILEDIGEST );

                    if ( fdig != null )
                    {
                        prvFileRef = new CObj();
                        prvFileRef.pushString ( CObj.NAME, replyPost.getString       ( CObj.PRV_NAME ) );
                        prvFileRef.pushNumber ( CObj.FILESIZE, replyPost.getNumber   ( CObj.PRV_FILESIZE ) );
                        prvFileRef.pushString ( CObj.FRAGDIGEST, replyPost.getString ( CObj.PRV_FRAGDIGEST ) );
                        prvFileRef.pushNumber ( CObj.FRAGSIZE, replyPost.getNumber   ( CObj.PRV_FRAGSIZE ) );
                        prvFileRef.pushNumber ( CObj.FRAGNUMBER, replyPost.getNumber ( CObj.PRV_FRAGNUMBER ) );
                        prvFileRef.pushString ( CObj.FILEDIGEST, replyPost.getString ( CObj.PRV_FILEDIGEST ) );
                    }

                    String subj = replyPost.getString ( CObj.SUBJECT );
                    String body = replyPost.getText ( CObj.BODY );

                    if ( subj == null ) { subj = ""; }

                    Matcher m = Pattern.compile ( "^Re:" ).matcher ( subj );

                    if ( !m.find() )
                    {
                        subj = "Re: " + subj;

                    }

                    subject.setText ( subj );

                    postBody.setText ( formatDisplay ( body, true ) );
                    postBody.setFocus();

                }

                if ( prvFileRef != null )
                {
                    file1Text.setText ( prvFileRef.getString ( CObj.NAME ) );
                }

                else
                {
                    file1Text.setText ( "" );
                }

                if ( fileRef != null )
                {
                    file2Text.setText ( fileRef.getString ( CObj.NAME ) );
                }

                else
                {
                    file2Text.setText ( "" );
                }

            }

        }

    }

    public void reply ( CObj id, CObj comid, CObj rpst )
    {
        fileRef = null;
        prvFileRef = null;
        replyPost = rpst;
        selectIdentity ( id, comid );
        super.open();
    }

    public void open ( CObj id, CObj comid, CObj prv, CObj lref )
    {
        replyPost = null;
        prvFileRef = prv;
        fileRef = lref;
        selectIdentity ( id, comid );
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

        postBody = new StyledText ( container, SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        postBody.setLayoutData ( new GridData ( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        new Label ( container, SWT.NONE );

        Label lblFile = new Label ( container, SWT.NONE );
        lblFile.setText ( "Preview File" );

        file1Text = new Text ( container, SWT.BORDER );
        file1Text.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
        file1Text.setEditable ( false );

        new Label ( container, SWT.NONE );

        Label lblFile_1 = new Label ( container, SWT.NONE );
        lblFile_1.setText ( "Complete File" );

        file2Text = new Text ( container, SWT.BORDER );
        file2Text.setLayoutData ( new GridData ( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
        file2Text.setEditable ( false );
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

            if ( prvFileRef != null )
            {
                p.pushString ( CObj.PRV_NAME,       prvFileRef.getString ( CObj.NAME ) );
                p.pushNumber ( CObj.PRV_FILESIZE,   prvFileRef.getNumber ( CObj.FILESIZE ) );
                p.pushString ( CObj.PRV_FRAGDIGEST, prvFileRef.getString ( CObj.FRAGDIGEST ) );
                p.pushNumber ( CObj.PRV_FRAGSIZE,   prvFileRef.getNumber ( CObj.FRAGSIZE ) );
                p.pushNumber ( CObj.PRV_FRAGNUMBER, prvFileRef.getNumber ( CObj.FRAGNUMBER ) );
                p.pushString ( CObj.PRV_FILEDIGEST, prvFileRef.getString ( CObj.FILEDIGEST ) );
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
        return new Point ( 700, 500 );
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

    public Text getFile1Text()
    {
        return file1Text;
    }

    public Text getFile2Text()
    {
        return file2Text;
    }

}
