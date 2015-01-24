package aktie.net;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.gui.GuiCallback;
import aktie.index.Index;
import aktie.user.RequestFileHandler;

public class DestinationThread implements Runnable
{

    public static Map<String, DestinationThread> threadlist = new HashMap<String, DestinationThread>();

    private Index index;
    private HH2Session session;
    private GuiCallback callback;

    public static void stopAll()
    {
        synchronized ( threadlist )
        {
            for ( DestinationThread d : threadlist.values() )
            {
                d.stop();
            }

        }

    }

    private boolean stop;
    private Destination dest;
    private Map<String, List<ConnectionThread>> connections;
    private CObj identity;
    private GetSendData sendData;
    private ConnectionListener conListener;
    private RequestFileHandler fileHandler;

    public DestinationThread ( Destination d, GetSendData sd, HH2Session s, Index i, GuiCallback cb, ConnectionListener cl, RequestFileHandler rf )
    {
        fileHandler = rf;
        conListener = cl;
        index = i;
        session = s;
        callback = cb;
        sendData = sd;
        dest = d;
        connections = new HashMap<String, List<ConnectionThread>>();
        Thread t = new Thread ( this );
        t.start();

        synchronized ( threadlist )
        {
            threadlist.put ( d.getPublicDestinationInfo(), this );
        }

    }

    public void setIdentity ( CObj o )
    {
        identity = o;
    }

    public CObj getIdentity()
    {
        return identity;
    }

    public boolean isStopped()
    {
        return stop;
    }

    public Destination getDest()
    {
        return dest;
    }

    public void closeConnections()
    {
        synchronized ( connections )
        {
            for ( List<ConnectionThread> tl : connections.values() )
            {
                for ( ConnectionThread ct : tl )
                {
                    ct.stop();
                }

            }

        }

    }

    public void stop()
    {
        stop = true;
        dest.close();
        closeConnections();
    }

    public void addEstablishedConnection ( ConnectionThread con )
    {
        String d = con.getEndDestination().getId();

        if ( d != null )
        {
            synchronized ( connections )
            {
                List<ConnectionThread> l = connections.get ( d );

                if ( l == null )
                {
                    l = new LinkedList<ConnectionThread>();
                    connections.put ( d, l );
                }

                l.add ( con );
            }

        }

    }

    public List<String> getConnectedIds()
    {
        List<String> r = new LinkedList<String>();

        synchronized ( connections )
        {
            r.addAll ( connections.keySet() );
        }

        return r;
    }

    public void send ( String id, CObj d )
    {
        List<ConnectionThread> ct = new LinkedList<ConnectionThread>();

        synchronized ( connections )
        {
            ct.addAll ( connections.get ( id ) );
        }

        for ( ConnectionThread c : ct )
        {
            c.enqueue ( d );
        }

    }

    public void connectionClosed ( ConnectionThread con )
    {
        if ( con.getEndDestination() != null )
        {
            String d = con.getEndDestination().getId();

            if ( d != null )
            {
                synchronized ( connections )
                {
                    List<ConnectionThread> l = connections.get ( d );

                    if ( l != null )
                    {
                        l.remove ( con );
                    }

                }

            }

        }

    }

    public int numberConnection()
    {
        synchronized ( connections )
        {
            return connections.size();
        }

    }

    public void send ( CObj o )
    {
        List<ConnectionThread> tl = new LinkedList<ConnectionThread>();

        synchronized ( connections )
        {
            for ( List<ConnectionThread> l : connections.values() )
            {
                tl.addAll ( l );
            }

        }

        for ( ConnectionThread t : tl )
        {
            t.enqueue ( o );
        }

    }

    public boolean isConnected ( String id )
    {
        synchronized ( connections )
        {
            List<ConnectionThread> clst = connections.get ( id );

            if ( clst == null ) { return false; }

            if ( clst.size() == 0 ) { return false; }

            return true;
        }

    }

    public void connect ( String d )
    {
        Connection con = dest.connect ( d );

        if ( con != null )
        {
            buildConnection ( con );
        }

    }

    public List<ConnectionThread> getConnectionThreads()
    {
        List<ConnectionThread> l = new LinkedList<ConnectionThread>();

        synchronized ( connections )
        {
            for ( List<ConnectionThread> cl : connections.values() )
            {
                l.addAll ( cl );
            }

        }

        return l;
    }

    private void buildConnection ( Connection c )
    {
        if ( identity == null )
        {
            c.close();
        }

        else
        {
            ConnectionThread ct = new ConnectionThread ( this, session, index, c, sendData, callback, conListener, fileHandler );
            ct.enqueue ( identity );
            conListener.update ( ct );
        }

    }


    @Override
    public void run()
    {
        try
        {
            while ( !stop )
            {
                Connection c = dest.accept();
                buildConnection ( c );
            }

        }

        catch ( Exception e )
        {
        }

        stop();
    }

}
