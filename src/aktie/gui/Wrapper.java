package aktie.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Wrapper
{

    public static String RUNDIR = "aktie_run_dir";
    public static String JARFILE = "aktie.jar";

    public static void main ( String args[] )
    {
        //Check the system
        String systype = System.getProperty ( "os.name" );
        System.out.println ( "SYS: " + systype );
        //Test if rundir exists.
        File f = new File ( RUNDIR );

        if ( !f.exists() )
        {
            unZipIt();

            if ( "Mac OS X".equals ( systype ) )
            {
                File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_osx.jar" );
                File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_osx.jar" );
                sfile.renameTo ( destfile );
            }

        }

        if ( !f.isDirectory() )
        {
            System.out.println ( "Oops.  I sorry.  I thought aktie should be a directory!" );
            System.exit ( 1 );
        }

        //Make bak dir
        File bd = new File ( RUNDIR + File.separator + "bak" );

        if ( !bd.exists() )
        {
            bd.mkdirs();
        }

        //Check for upgrades
        File updir = new File ( RUNDIR + File.separator + "upgrade" );

        if ( !updir.exists() )
        {
            updir.mkdirs();
        }

        File libd = new File ( RUNDIR + File.separator + "lib" );
        File uplst[] = updir.listFiles();

        for ( int c = 0; c < uplst.length; c++ )
        {
            File uf = uplst[c];
            File bakfile = new File ( bd.getPath() + File.separator + uf.getName() + ".bak" );

            //delete if bak file already there.
            if ( bakfile.exists() )
            {
                bakfile.delete();
            }

            File ef = new File ( libd.getPath() + File.separator + uf.getName() );

            if ( ef.exists() )
            {
                ef.renameTo ( bakfile );
            }

            //Now change upgrade file
            System.out.println ( "UPGRADING: " + ef.getPath() );
            uf.renameTo ( ef );
        }

        //Just run it!
        //java -XstartOnFirstThread -cp aktie.jar:aktie/lib/*:org.eclipse.swt/swt.jar aktie.gui.SWTApp aktie_node
        List<String> cmd = new LinkedList<String>();
        cmd.add ( "java" );
        cmd.add ( "-XstartOnFirstThread" );
        cmd.add ( "-cp" );
        StringBuilder sb = new StringBuilder();
        File ll[] = libd.listFiles();

        if ( ll.length > 0 )
        {
            sb.append ( ll[0] );

            for ( int c = 1; c < ll.length; c++ )
            {
                sb.append ( ":" );
                sb.append ( ll[c] );
            }

        }

        cmd.add ( sb.toString() );
        cmd.add ( "aktie.gui.SWTApp" );
        cmd.add ( RUNDIR + File.separator + "aktie_node" );
        cmd.add ( "comdump.dat" );
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream ( true );
        pb.command ( cmd );

        try
        {
            Process pc = pb.start();
            pc.getInputStream();
            byte buf[] = new byte[1024];
            InputStream is = pc.getInputStream();
            int ln = is.read ( buf );

            while ( ln >= 0 )
            {
                if ( ln > 0 )
                {
                    System.out.write ( buf, 0, ln );
                }

                ln = is.read ( buf );
            }

            System.out.println ( "EXITTING.." );
        }

        catch ( IOException e )
        {
            e.printStackTrace();
        }

    }



    public static void unZipIt()
    {

        byte[] buffer = new byte[1024];

        try
        {

            //create output directory is not exists
            File folder = new File ( RUNDIR );

            if ( !folder.exists() )
            {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                new ZipInputStream ( new FileInputStream ( JARFILE ) );
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while ( ze != null )
            {

                String fileName = ze.getName();
                File newFile = new File ( RUNDIR + File.separator + fileName );

                System.out.println ( "file unzip : " + newFile.getAbsoluteFile() );

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File ( newFile.getParent() ).mkdirs();

                FileOutputStream fos = new FileOutputStream ( newFile );

                int len;

                while ( ( len = zis.read ( buffer ) ) > 0 )
                {
                    fos.write ( buffer, 0, len );
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println ( "Done" );

        }

        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

    }

}
