package aktie.gui;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import aktie.data.CObj;
import aktie.gui.IdentitySubTreeProvider.TreeIdentity;
import aktie.gui.IdentitySubTreeProvider.TreeSubscription;

public class IdentitySubTreeLabelProvider implements IStyledLabelProvider
{
	
	private Styler blueStyle;

	public IdentitySubTreeLabelProvider() {
		Device device = Display.getCurrent ();
		final Color blue = new Color (device, 0, 0, 255);
		blueStyle = new Styler() {

			@Override
			public void applyStyles(TextStyle a) {
				a.foreground = blue;
			}
			
		};
	}
	
    @Override
    public void addListener ( ILabelProviderListener arg0 )
    {
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public boolean isLabelProperty ( Object arg0, String arg1 )
    {
        return false;
    }

    @Override
    public void removeListener ( ILabelProviderListener arg0 )
    {
    }

    @Override
    public Image getImage ( Object arg0 )
    {
        return null;
    }

    @Override
	public StyledString getStyledText(Object a) 
    {
        if ( a instanceof TreeIdentity )
        {
            TreeIdentity e = ( TreeIdentity ) a;
            StyledString ss = new StyledString(e.display, blueStyle);
            return ss;
        }

        else if ( a instanceof TreeSubscription )
        {
            TreeSubscription ts = ( TreeSubscription ) a;
            String scope = ts.community.getString ( CObj.SCOPE );
            String name = ts.community.getPrivateDisplayName();

            if ( CObj.SCOPE_PRIVATE.equals ( scope ) )
            {
                name = "* " + name;
            }

            return new StyledString(name);
        }

        return null;
    }

}
    