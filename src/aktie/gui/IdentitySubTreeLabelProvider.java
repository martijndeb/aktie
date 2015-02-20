package aktie.gui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import aktie.data.CObj;
import aktie.gui.IdentitySubTreeProvider.TreeIdentity;
import aktie.gui.IdentitySubTreeProvider.TreeSubscription;

public class IdentitySubTreeLabelProvider implements ILabelProvider
{

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
    public String getText ( Object a )
    {
        if ( a instanceof TreeIdentity )
        {
            TreeIdentity e = ( TreeIdentity ) a;
            return e.display;
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

            return name;
        }

        return null;
    }

}
