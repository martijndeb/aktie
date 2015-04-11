package aktie.user;

import java.io.File;
import java.io.IOException;

import aktie.data.CObj;
import aktie.data.DirectoryShare;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.utils.FUtils;
import aktie.utils.HasFileCreator;

public class ShareManager
{

    private Index index;
    private HasFileCreator hfc;

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
            addFile ( s, f );
        }

        mlst.close();
    }

    /*
        If we search for all share files first.  Files renamed or moved
        in s share will already have thie hasfile record updated with the
        new filename before this is run.  When this is run for all hasfile
        records, it should only find files that have been deleted from a
        share, so we set them as stillhas false.
    */
    private void checkHasFile ( CObj hf )
    {
        if ( CObj.HASFILE.equals ( hf.getType() ) )
        {
            throw new RuntimeException ( "You Supid." );
        }

        String lf = hf.getPrivate ( CObj.LOCALFILE );
        String wd = hf.getString ( CObj.FILEDIGEST );

        if ( lf != null && wd != null )
        {
            File f = new File ( lf );
            boolean remove = true;

            if ( f.exists() )
            {
                String rdig = FUtils.digWholeFile ( lf );

                if ( wd.equals ( rdig ) )
                {
                    remove = false;
                }

            }

            if ( remove )
            {
                hf.pushString ( CObj.STILLHASFILE, "false" );
                hfc.createHasFile ( hf );
                hfc.updateFileInfo ( hf );
            }

        }

    }

}
