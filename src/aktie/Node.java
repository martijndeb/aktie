package aktie;

import java.io.File;
import java.io.IOException;

import aktie.data.CObj;
import aktie.data.HH2Session;
import aktie.gui.GuiCallback;
import aktie.index.CObjList;
import aktie.index.Index;
import aktie.net.ConnectionListener;
import aktie.net.ConnectionManager;
import aktie.net.Destination;
import aktie.net.DestinationThread;
import aktie.net.Net;
import aktie.user.IdentityManager;
import aktie.user.NewCommunityProcessor;
import aktie.user.NewFileProcessor;
import aktie.user.NewIdentityProcessor;
import aktie.user.NewMembershipProcessor;
import aktie.user.NewPostProcessor;
import aktie.user.NewPushProcessor;
import aktie.user.NewSubscriptionProcessor;
import aktie.user.RequestFileHandler;
import aktie.user.UsrReqComProcessor;
import aktie.user.UsrReqFileProcessor;
import aktie.user.UsrReqHasFileProcessor;
import aktie.user.UsrReqIdentityProcessor;
import aktie.user.UsrReqMemProcessor;
import aktie.user.UsrReqPostProcessor;
import aktie.user.UsrReqSubProcessor;
import aktie.user.UsrSeed;
import aktie.user.UsrSeedCommunity;

public class Node
{

    private Net network;
    private Index index;
    private HH2Session session;
    private ProcessQueue userQueue;
    private IdentityManager identManager;
    private GuiCallback usrCallback;
    private GuiCallback netCallback;
    private ConnectionListener conCallback;
    private ConnectionManager conMan;
    private RequestFileHandler requestHandler;
    private Settings settings;


    public Node ( String nodedir, Net net, GuiCallback uc,
                  GuiCallback nc, ConnectionListener cc ) throws IOException
    {
        usrCallback = uc;
        netCallback = nc;
        conCallback = cc;
        network = net;
        settings = new Settings ( nodedir );
        File idxdir = new File ( nodedir + File.separator + "index" );
        index = new Index();
        index.setIndexdir ( idxdir );
        index.init();
        session = new HH2Session();
        session.init ( nodedir + File.separator + "h2" );
        identManager = new IdentityManager ( session, index );
        requestHandler = new RequestFileHandler ( session, nodedir + File.separator + "downloads" );
        conMan = new ConnectionManager ( session, index, requestHandler, identManager, usrCallback );
        userQueue = new ProcessQueue();
        userQueue.addProcessor ( new NewCommunityProcessor ( session, index, usrCallback ) );
        userQueue.addProcessor ( new NewFileProcessor ( session, index, usrCallback ) );
        userQueue.addProcessor ( new NewIdentityProcessor ( network, conMan, session,
                                 index, usrCallback, netCallback, conCallback, conMan, requestHandler ) );
        userQueue.addProcessor ( new NewMembershipProcessor ( session, index, usrCallback ) );
        userQueue.addProcessor ( new NewPostProcessor ( session, index, usrCallback ) );
        userQueue.addProcessor ( new NewSubscriptionProcessor ( session, index, usrCallback ) );
        userQueue.addProcessor ( new UsrReqComProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrReqFileProcessor ( requestHandler, usrCallback ) );
        userQueue.addProcessor ( new UsrReqHasFileProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrReqIdentityProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrReqMemProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrReqPostProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrReqSubProcessor ( identManager ) );
        userQueue.addProcessor ( new UsrSeed ( session, index, netCallback ) );
        userQueue.addProcessor ( new UsrSeedCommunity ( session, index, netCallback ) );
        userQueue.addProcessor ( new NewPushProcessor ( index, conMan ) );
        CObjList myids = index.getMyIdentities();

        for ( int c = 0; c < myids.size(); c++ )
        {
            CObj myid = myids.get ( c );
            String destfile = myid.getPrivate ( CObj.DEST );

            if ( destfile != null )
            {
                File f = new File ( destfile );
                Destination d = network.getExistingDestination ( f );
                DestinationThread dt =
                    new DestinationThread ( d, conMan, session, index, netCallback, conCallback, requestHandler );
                dt.setIdentity ( myid );
                conMan.addDestination ( dt );
            }

        }

        myids.close();
    }

    public void close()
    {
        conMan.stop();
        userQueue.stop();
        index.close();
    }

    public void enqueue ( CObj o )
    {
        userQueue.enqueue ( o );
    }

    public Index getIndex()
    {
        return index;
    }

    public HH2Session getSession()
    {
        return session;
    }

    public RequestFileHandler getFileHandler()
    {
        return requestHandler;
    }

    public void closeDestinationConnections ( CObj id )
    {
        conMan.closeDestinationConnections ( id );
    }

    public void closeAllConnections()
    {
        conMan.closeAllConnections();
    }

    public Settings getSettings()
    {
        return settings;
    }

}
