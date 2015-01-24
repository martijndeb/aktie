package aktie.gui;

import java.io.IOException;
import java.lang.ref.SoftReference;

import aktie.index.CObjList;
import aktie.data.CObj;

public class CObjListArrayElement
{

    private int index;
    private CObjList list;
    private SoftReference<CObj> softCObj;

    public CObjListArrayElement ( CObjList l, int idx )
    {
        index = idx;
        list = l;
    }

    public CObj getCObj()
    {
        CObj r = null;

        if ( softCObj != null )
        {
            r = softCObj.get();
        }

        if ( r == null )
        {
            try
            {
                r = list.get ( index );
                softCObj = new SoftReference<CObj> ( r );
            }

            catch ( IOException e )
            {
                e.printStackTrace();
            }

        }

        return r;
    }

}
