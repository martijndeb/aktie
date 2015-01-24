package aktie.gui;

import java.lang.ref.SoftReference;

import org.hibernate.Session;

import aktie.data.CObj;
import aktie.data.FileInfo;
import aktie.data.HH2Session;
import aktie.index.CObjList;

public class FileListArrayElement extends CObjListArrayElement
{

    private HH2Session session;
    private SoftReference<FileInfo> softFI;

    public FileListArrayElement ( HH2Session s, CObjList l, int idx )
    {
        super ( l, idx );
        session = s;
    }

    public FileInfo getFileInfo()
    {
        FileInfo r = null;

        if ( softFI != null )
        {
            r = softFI.get();
        }

        if ( r == null )
        {
            CObj o = getCObj();

            if ( o != null )
            {
                String id = o.getId();
                Session s = null;

                try
                {
                    s = session.getSession();
                    r = ( FileInfo ) s.get ( FileInfo.class, id );
                    s.close();
                }

                catch ( Exception e )
                {
                    e.printStackTrace();

                    if ( s != null )
                    {
                        try
                        {
                            s.close();
                        }

                        catch ( Exception e2 )
                        {
                        }

                    }

                }

                if ( r != null )
                {
                    softFI = new SoftReference<FileInfo> ( r );
                }

            }

        }

        return r;

    }

}
