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

    private File i2pdir;
    private Router router;

    public I2PNet ( String nodedir )
    {
        i2pdir = new File ( nodedir + File.separator + "i2p" );

        if ( i2pdir.exists() )
        {
            i2pdir.mkdirs();
        }

        startI2P ();
    }

    private void startI2P ()
    {
        System.setProperty ( "java.net.preferIPv4Stack", "false" );
        System.setProperty ( "i2p.dir.base", i2pdir.getPath() );
        System.setProperty ( "loggerFilenameOverride", "log" + File.separator + "log-router.log" );
        router = new Router();
        router.runRouter();
    }

    public void exit()
    {
        router.shutdown ( Router.EXIT_HARD );
    }

    @Override
    public Destination getExistingDestination ( File privateinfo )
    {
        Properties p = new Properties();
        p.setProperty ( "i2cp.tcp.host", "127.0.0.1" );
        p.setProperty ( "i2cp.tcp.port", "7654" );
        p.setProperty ( "inbound.nickname", "aktie" );

        try
        {
            FileInputStream fis = new FileInputStream ( privateinfo );
            I2PSocketManager manager = I2PSocketManagerFactory.createManager ( fis, p );
            fis.close();
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
        Properties p = new Properties();
        p.setProperty ( "i2cp.tcp.host", "127.0.0.1" );
        p.setProperty ( "i2cp.tcp.port", "7654" );
        p.setProperty ( "inbound.nickname", "aktie" );
        I2PSocketManager manager = I2PSocketManagerFactory.createManager ( p );
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
            Socket s = new Socket ( "127.0.0.1", 7654 );
            s.close();
            return true;
        }

        catch ( Exception e )
        {
        }

        return false;
    }


}
