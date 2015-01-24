package aktie.utils;

import aktie.crypto.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FUtils
{

    public static File createTestFile ( long size ) throws IOException
    {
        File f = File.createTempFile ( "junkfile", ".dat" );
        FileOutputStream fos = new FileOutputStream ( f );
        byte tb[] = new byte[1024];

        while ( size > 0 )
        {
            Utils.Random.nextBytes ( tb );
            size -= tb.length;
            fos.write ( tb );
        }

        fos.close();
        return f;
    }

    public static boolean diff ( File f0, File f1 ) throws IOException
    {
        if ( f0.length() != f1.length() ) { return false; }

        boolean r = true;
        FileInputStream fi0 = new FileInputStream ( f0 );
        FileInputStream fi1 = new FileInputStream ( f1 );
        int v0 = fi0.read();
        int v1 = fi1.read();

        if ( v0 != v1 ) { r = false; }

        while ( v0 >= 0 && v1 >= 0 && r )
        {
            v0 = fi0.read();
            v1 = fi1.read();

            if ( v0 != v1 ) { r = false; }

        }

        fi0.close();
        fi1.close();
        return r;
    }

    public static void deleteDir ( File f )
    {
        if ( f.exists() )
        {
            if ( f.isDirectory() )
            {
                File l[] = f.listFiles();

                for ( File nf : l )
                {
                    deleteDir ( nf );
                }

            }

            f.delete();
        }

    }

}
