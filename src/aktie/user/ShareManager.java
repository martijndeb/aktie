package aktie.user;

import java.io.File;
import java.io.IOException;

import aktie.data.DirectoryShare;
import aktie.index.CObjList;
import aktie.index.Index;

public class ShareManager
{

    private Index index;

    private void addFile ( DirectoryShare s, File f )
    {

    }

    private void checkFoundFile ( DirectoryShare s, File f, boolean quick )
    {
        String fp = f.getAbsolutePath();

        try
        {
            fp = f.getCanonicalPath();
        }

        catch ( IOException e )
        {
            e.printStackTrace();
        }

        CObjList mlst = index.getLocalHasFiles ( s.getCommunityId(), s.getMemberId(), fp );

        if ( mlst.size() == 0 )
        {
            //File not found.  Add it!

        }

        mlst.close();
    }

}
