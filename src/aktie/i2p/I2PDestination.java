package aktie.i2p;

import java.io.File;

import net.i2p.I2PException;
import net.i2p.client.I2PSession;
import net.i2p.client.streaming.I2PServerSocket;
import net.i2p.client.streaming.I2PSocket;
import net.i2p.client.streaming.I2PSocketManager;
import net.i2p.data.PrivateKeyFile;
import aktie.net.Connection;
import aktie.net.Destination;

public class I2PDestination implements Destination
{

	private File i2pdir;
	private I2PSocketManager manager;
	private I2PServerSocket server;
	
	public I2PDestination(File dir, I2PSocketManager mngr) 
	{
		manager = mngr;
		i2pdir = dir;
		server = manager.getServerSocket();
	}
	
	
	@Override
	public File savePrivateDestinationInfo() 
	{
		try 
		{
			File f = File.createTempFile("dest", ".bin", i2pdir);
			PrivateKeyFile pkf = new PrivateKeyFile(f, manager.getSession());
			pkf.write();
			return f;
		}
		
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public String getPublicDestinationInfo() 
	{
		I2PSession ses = manager.getSession();
		return ses.getMyDestination().toBase64(); 
	}

	@Override
	public Connection connect(String destination) 
	{
		try 
		{
			I2PSocket sock = manager.connect(
					new net.i2p.data.Destination(destination));
			return new I2PConnection(sock);
		} 
		
		catch (Exception e) 
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
			I2PSocket sock = server.accept();
			return new I2PConnection(sock);
		} 
		
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public void close() 
	{
		try 
		{
			server.close();
		} 
		
		catch (I2PException e) 
		{
			e.printStackTrace();
		}
		
	}

}
