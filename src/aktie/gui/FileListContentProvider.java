package aktie.gui;

import aktie.data.HH2Session;
import aktie.index.CObjList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileListContentProvider implements IStructuredContentProvider
{

    private CObjList list;
    private HH2Session session;

    public void setHH2Session ( HH2Session s )
    {
        session = s;
    }

    @Override
    public void dispose()
    {
        if ( list != null )
        {
            list.close();
        }

    }

    @Override
    public void inputChanged ( Viewer arg0, Object arg1, Object arg2 )
    {
    }

    @Override
    public Object[] getElements ( Object i )
    {
        if ( i instanceof CObjList && i != null )
        {
            list = ( CObjList ) i;
            Object r[] = new Object[list.size()];

            for ( int c = 0; c < r.length; c++ )
            {
                r[c] = new FileListArrayElement ( session, list, c );
            }

            return r;
        }

        return new Object[] {};

    }

}
