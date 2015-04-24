package aktie.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import aktie.utils.FUtils;

public class Wrapper
{

    public static int RESTART_RC = 7;

    public static String VERSION = "version 0.1.14";
    public static String VERSION_FILE = "version.txt";
    //ADD ONE HOUR TO TIME.
    //This makes sure this time value is greater than the time of
    //the upgrade file added to the network by the developer account.
    //This keeps new installs from downloading the same version as
    //an upgrade
    public static long RELEASETIME = ( 1428129453L * 1000L ) + 3600000;

    public static String RUNDIR = "aktie_run_dir";
    public static String JARFILE = "aktie.jar";


    public static void main ( String args[] )
    {
        int rc = RESTART_RC;

        while ( rc == RESTART_RC )
        {
            rc = Main ( args );
            System.out.println ( "RC: " + rc );
        }

    }

    public static int Main ( String args[] )
    {
        boolean verbose = false;

        for ( int ct = 0; ct < args.length; ct++ )
        {
            if ( "-v".equals ( args[ct] ) )
            {
                verbose = true;
            }

        }

        //Check the system
        String systype = System.getProperty ( "os.name" );
        System.out.println ( "SYS: " + systype );
        //Test if rundir exists.
        File f = new File ( RUNDIR );

        boolean setstartonfirst = false;
        boolean usesemi = false;

        if ( systype.startsWith ( "Windows" ) )
        {
            usesemi = true;
        }

        if ( "Mac OS X".equals ( systype ) )
        {
            setstartonfirst = true;
        }

        if ( !f.exists() || isNewer() )
        {
            unZipIt();

            List<String> cmd = new LinkedList<String>();
            cmd.add ( "java" );
            cmd.add ( "-version" );
            ProcessBuilder pb = new ProcessBuilder();
            pb.redirectErrorStream ( true );
            pb.command ( cmd );

            boolean is64bit = false;

            try
            {

                Matcher m = Pattern.compile ( "64-Bit" ).matcher ( "" );
                Process pc = pb.start();
                BufferedReader br = new BufferedReader ( new InputStreamReader ( pc.getInputStream () ) );
                String ln = br.readLine ();

                while ( ln != null )
                {
                    m.reset ( ln );

                    if ( m.find() )
                    {
                        is64bit = true;
                    }

                    ln = br.readLine ();

                }

            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

            if ( systype.startsWith ( "Linux" ) )
            {
                if ( is64bit )
                {
                    File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_linux_64.jar" );
                    File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_linux_64.jar" );
                    sfile.renameTo ( destfile );
                }

                else
                {
                    File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_linux.jar" );
                    File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_linux.jar" );
                    sfile.renameTo ( destfile );
                }

            }

            if ( "Mac OS X".equals ( systype ) )
            {
                File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_osx.jar" );
                File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_osx.jar" );
                sfile.renameTo ( destfile );
            }

            if ( systype.startsWith ( "Windows" ) )
            {
                if ( is64bit )
                {
                    File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_win_64.jar" );
                    File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_win_64.jar" );
                    sfile.renameTo ( destfile );
                }

                else
                {
                    File sfile = new File ( RUNDIR + File.separator + "swt" + File.separator + "swt_win.jar" );
                    File destfile = new File ( RUNDIR + File.separator + "lib" + File.separator + "swt_win.jar" );
                    sfile.renameTo ( destfile );
                }

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

        Matcher comp = Pattern.compile ( "(.+)\\.COMPLETE$" ).matcher ( "" );

        for ( int c = 0; c < uplst.length; c++ )
        {
            File uf = uplst[c];

            comp.reset ( uf.getName() );

            if ( comp.find() )
            {
                String ufn = comp.group ( 1 );

                String hash = getUpdateHash ( ufn );
                String rhash = FUtils.digWholeFile ( uf.getPath() );

                if ( hash != null && hash.equals ( rhash ) )
                {


                    File bakfile = new File ( bd.getPath() + File.separator + ufn + ".bak" );

                    //delete if bak file already there.
                    if ( bakfile.exists() )
                    {
                        bakfile.delete();
                    }

                    File ef = new File ( libd.getPath() + File.separator + ufn );

                    if ( ef.exists() )
                    {
                        try
                        {
                            FUtils.copy ( ef, bakfile );
                        }

                        catch ( IOException e )
                        {
                            e.printStackTrace();
                        }

                    }

                    //Now change upgrade file
                    System.out.println ( "UPGRADING: " + ef.getPath() );

                    try
                    {
                        FUtils.copy ( uf, ef );

                        if ( !uf.delete() )
                        {
                            System.out.println ( "ERROR: Could not delete upgrade file." );
                        }

                    }

                    catch ( IOException e )
                    {
                        e.printStackTrace();
                    }

                }

                else
                {
                    uf.delete();
                }

            }

        }

        //Just run it!
        //java -XstartOnFirstThread -cp aktie.jar:aktie/lib/*:org.eclipse.swt/swt.jar aktie.gui.SWTApp aktie_node
        List<String> cmd = new LinkedList<String>();
        cmd.add ( "java" );

        if ( setstartonfirst )
        {
            cmd.add ( "-XstartOnFirstThread" );
        }

        cmd.add ( "-Xmx128m" );
        cmd.add ( "-cp" );
        StringBuilder sb = new StringBuilder();
        System.out.println ( "LIST LIBS: " + libd.getPath() );
        File ll[] = libd.listFiles();

        if ( ll.length > 0 )
        {

            sb.append ( ll[0] );

            for ( int c = 1; c < ll.length; c++ )
            {
                if ( usesemi )
                {
                    sb.append ( ";" );
                }

                else
                {
                    sb.append ( ":" );
                }

                sb.append ( ll[c] );
            }

        }

        cmd.add ( sb.toString() );
        cmd.add ( "aktie.gui.SWTApp" );
        cmd.add ( RUNDIR + File.separator + "aktie_node" );

        if ( verbose )
        {
            cmd.add ( "-v" );
            System.out.println ( "SETTING VERBOSE!" );
        }

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
            return pc.exitValue();
        }

        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return 99;

    }

    public static int[] convertVersionString ( String v )
    {
        Matcher m = Pattern.compile ( "(\\d+)\\.(\\d+)\\.(\\d+)" ).matcher ( v );

        if ( m.find() )
        {
            int va[] = new int[3];
            va[0] = Integer.valueOf ( m.group ( 1 ) );
            va[1] = Integer.valueOf ( m.group ( 2 ) );
            va[2] = Integer.valueOf ( m.group ( 3 ) );
            return va;
        }

        return new int[] {0, 0, 0};

    }

    /*
        Check if this jar is a newer version of the current files in the
        library
    */
    public static boolean isNewer()
    {
        File vf = new File ( RUNDIR + File.separator + "aktie_node" + File.separator + VERSION_FILE );

        if ( vf.exists() )
        {
            try
            {
                FileReader fr = new FileReader ( vf );
                BufferedReader br = new BufferedReader ( fr );
                String oldstr = br.readLine();
                br.close();
                int oldv[] = convertVersionString ( oldstr );
                int newv[] = convertVersionString ( VERSION );

                for ( int c = 0; c < oldv.length; c++ )
                {
                    if ( newv[c] < oldv[c] )
                    {
                        return false;
                    }

                    if ( newv[c] > oldv[c] )
                    {
                        return true;
                    }

                }

                return false;
            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

        }

        return true;
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
                File pd = new File ( newFile.getParent() );
                pd.mkdirs();

                if ( pd.isDirectory() && !ze.isDirectory() )
                {

                    FileOutputStream fos = new FileOutputStream ( newFile );

                    int len;

                    while ( ( len = zis.read ( buffer ) ) > 0 )
                    {
                        fos.write ( buffer, 0, len );
                    }

                    fos.close();
                }

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

    public static Properties loadExistingProps()
    {
        Properties p = new Properties();
        File propfile = new File ( RUNDIR + File.separator + "aktie_node" + File.separator + "aktie.pros" );

        if ( propfile.exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream ( propfile );
                p.load ( fis );
                fis.close();
            }

            catch ( Exception e )
            {
                e.printStackTrace();
            }

        }

        return p;
    }

    public static void savePropsFile ( Properties p )
    {
        File propfile = new File ( RUNDIR + File.separator + "aktie_node" + File.separator + "aktie.pros" );

        try
        {
            FileOutputStream fos = new FileOutputStream ( propfile );
            p.store ( fos, "Aktie properties" );
            fos.close();
        }

        catch ( Exception e )
        {
            e.printStackTrace();
        }

    }

    public static String getLastDevMessage()
    {
        String msg = "Developer messages.";

        Properties p = loadExistingProps();

        String m = p.getProperty ( "aktie.developerMessage" );

        if ( m != null )
        {
            msg = m;
        }

        return msg;
    }

    public static void saveLastDevMessage ( String msg )
    {
        Properties p = loadExistingProps();

        p.setProperty ( "aktie.developerMessage", msg );

        savePropsFile ( p );
    }

    public static String getUpdateHash ( String file )
    {
        Properties p = loadExistingProps();

        return p.getProperty ( "hash." + file );
    }

    public static void saveUpdateHash ( String file, String hash )
    {
        Properties p = loadExistingProps();

        p.setProperty ( "hash." + file, hash );

        savePropsFile ( p );
    }

}
