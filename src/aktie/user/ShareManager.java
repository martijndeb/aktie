package aktie.user;

import java.io.File;
import java.io.IOException;

import aktie.data.CObj;
import aktie.data.DirectoryShare;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.utils.FUtils;

public class ShareManager
{

    private Index index;

    private void addFile ( DirectoryShare s, File f )
    {

    }

    private void checkFoundFile ( DirectoryShare s, File f )
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
        	addFile(s, f);
        }

        mlst.close();
    }
    
    private void checkHasFile( CObj hf ) {
    	String lf = hf.getPrivate(CObj.LOCALFILE);
    	String wd = hf.getString(CObj.FILEDIGEST);
    	if (lf != null && wd != null) {
    		File f = new File(lf);
    		boolean remove = true;
    		if (f.exists()) {
    			String rdig = FUtils.digWholeFile(lf);
    			if (wd.equals(rdig)) {
    				remove = false;
    			}
    		}
    		if (remove) {
    			
    		}
    	}
    }

}
