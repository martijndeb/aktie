package aktie.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class RawDestination implements Destination
{

    private ServerSocket servsock;
    private File nodeDir;

    public RawDestination ( ServerSocket s, File nd )
    {
        nodeDir = nd;
        servsock = s;
    }

    @Override
    public File savePrivateDestinationInfo()
    {
        try
        {
            File lf = File.createTempFile ( "desttest", ".dat", nodeDir );
            FileOutputStream fos = new FileOutputStream ( lf );
            PrintWriter pw = new PrintWriter ( fos );
            pw.println ( servsock.getLocalPort() );
            pw.close();
            return lf;
        }

        catch ( Exception e )
        {
            throw new RuntimeException ( "oops. ", e );
        }

    }

    @Override
    public String getPublicDestinationInfo()
    {
        String addr = servsock.getInetAddress().getHostAddress();
        addr = addr + ":" + servsock.getLocalPort();
        return addr;
    }

    @Override
    public Connection connect ( String destination )
    {
        String p[] = destination.split ( ":" );
        int prt = Integer.valueOf ( p[1] );

        try
        {
            System.out.println ( "ATTEMPT CONNECTION: " + p[0] + ":" + prt );
            Socket s = new Socket ( p[0], prt );
            System.out.println ( "CON: " + s );
            return new SocketConnection ( s );
        }

        catch ( UnknownHostException e )
        {
            e.printStackTrace();
        }

        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Connection accept()
    {
        try
        {
            Socket s = servsock.accept();
            System.out.println ( "Connection accepted: " + s.getRemoteSocketAddress() );
            return new SocketConnection ( s );
        }

        catch ( IOException e )
        {
        }

        return null;
    }

    @Override
    public void close()
    {
        try
        {
            servsock.close();
        }

        catch ( IOException e )
        {
        }

    }

}
