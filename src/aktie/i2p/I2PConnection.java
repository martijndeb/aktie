package aktie.i2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.i2p.client.streaming.I2PSocket;
import aktie.net.Connection;

public class I2PConnection implements Connection 
{

	private I2PSocket socket;
	
	public I2PConnection(I2PSocket s) 
	{
		socket = s;
	}
	
	@Override
	public InputStream getInputStream() 
	{
		try 
		{
			return socket.getInputStream();
		}
		
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public OutputStream getOutputStream() 
	{
		try 
		{
			return socket.getOutputStream();
		} 
		
		catch (IOException e) 
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
			socket.close();
		} 
		
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

}
