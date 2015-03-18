package aktie.i2p;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.Properties;

import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.client.streaming.I2PSocketManagerFactory;
import net.i2p.router.Router;
import aktie.net.Destination;
import aktie.net.Net;

public class I2PNet  implements Net
{

    private Properties customProps;

    private File i2pdir;
    private Router router;

    public I2PNet ( String nodedir, Properties p )
    {
        customProps = p;

        i2pdir = new File ( nodedir + File.separator + "i2p" );

        if ( i2pdir.exists() )
        {
            i2pdir.mkdirs();
        }

        if ( !externHost() )
        {
            if ( !testClient() )
            {
                System.out.println ( "No i2p found.  Starting router." );
                startI2P ();
            }

            else
            {
                System.out.println ( "Router seems to be running already." );
            }

        }

        else
        {
            System.out.println ( "You have selected to use an external I2P router." );
        }

    }

    private boolean externHost()
    {
        if ( customProps != null )
        {
            String hst = customProps.getProperty ( "i2cp.tcp.host" );

            if ( hst != null && !"".equals ( hst ) )
            {
                return true;
            }

        }

        return false;
    }

    private void startI2P ()
    {
        System.setProperty ( "java.net.preferIPv4Stack", "false" );
        System.setProperty ( "i2p.dir.base", i2pdir.getPath() );
        System.setProperty ( "loggerFilenameOverride", "log" + File.separator + "log-router.log" );
        router = new Router();
        router.setKillVMOnEnd ( false );
        router.runRouter();
    }

    public void exit()
    {
        if ( router != null )
        {
            router.shutdown ( Router.EXIT_HARD );
        }

    }

    @Override
    public Destination getExistingDestination ( File privateinfo )
    {
        String hst = "127.0.0.1";
        int port = 7654;
        Properties p = new Properties();

        if ( customProps == null )
        {
            p.setProperty ( "i2cp.tcp.host", hst );
            p.setProperty ( "i2cp.tcp.port", Integer.toString ( port ) );
            p.setProperty ( "inbound.nickname", "aktie" );
        }

        else
        {
            p.putAll ( customProps );
            String h = customProps.getProperty ( "i2cp.tcp.host" );

            if ( h != null )
            {
                hst = h;
            }

            String pt = customProps.getProperty ( "i2cp.tcp.port" );

            if ( pt != null )
            {
                port = Integer.valueOf ( pt );
            }

        }

        try
        {
            I2PSocketManager manager = null;

            while ( manager == null )
            {
                FileInputStream fis = new FileInputStream ( privateinfo );
                manager = I2PSocketManagerFactory.createManager ( fis, hst, port, p );
                fis.close();

                if ( manager == null )
                {
                    System.out.println ( "Wating for socket manager for existing destination." );
                    Thread.sleep ( 1000 );
                }

            }

            return new I2PDestination ( i2pdir, manager );
        }

        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Destination getNewDestination()
    {
        String hst = "127.0.0.1";
        int port = 7654;
        Properties p = new Properties();

        if ( customProps == null )
        {
            p.setProperty ( "i2cp.tcp.host", hst );
            p.setProperty ( "i2cp.tcp.port", Integer.toString ( port ) );
            p.setProperty ( "inbound.nickname", "aktie" );
        }

        else
        {
            p.putAll ( customProps );
            String h = customProps.getProperty ( "i2cp.tcp.host" );

            if ( h != null )
            {
                hst = h;
            }

            String pt = customProps.getProperty ( "i2cp.tcp.port" );

            if ( pt != null )
            {
                port = Integer.valueOf ( pt );
            }

        }

        I2PSocketManager manager = null;

        while ( manager == null )
        {
            manager = I2PSocketManagerFactory.createManager ( hst, port, p );

            if ( manager == null )
            {
                System.out.println ( "Wating for socket manager for new destination." );

                try
                {
                    Thread.sleep ( 1000 );
                }

                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }

            }

        }

        return new I2PDestination ( i2pdir, manager );
    }


    public void waitUntilReady()
    {
        while ( !testClient() )
        {
            try
            {
                Thread.sleep ( 1000 );
            }

            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }

        }

    }

    public boolean testClient()
    {
        try
        {
            String hst = "127.0.0.1";
            int port = 7654;

            if ( customProps != null )
            {
                String h = customProps.getProperty ( "i2cp.tcp.host" );

                if ( h != null )
                {
                    hst = h;
                }

                String pt = customProps.getProperty ( "i2cp.tcp.port" );

                if ( pt != null )
                {
                    port = Integer.valueOf ( pt );
                }

            }

            Socket s = new Socket ( hst, port );
            s.close();
            return true;
        }

        catch ( Exception e )
        {
        }

        return false;
    }


}
