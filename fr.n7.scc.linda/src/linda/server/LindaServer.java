/**
 * 
 */
package linda.server;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

/**
 * @author cpantel
 *
 */
public class LindaServer extends UnicastRemoteObject implements Linda {
	
	public static final int DEFAULT_SERVER_PORT = 4000;
	private CentralizedLinda local;

	/**
	 * @throws RemoteException
	 */
	public LindaServer() throws RemoteException {
		// TODO Auto-generated constructor stub
		this.local = new CentralizedLinda();
	}
	
	@Override
	public void write(Tuple tuple) {
		this.local.write(tuple);
	}

	@Override
	public Tuple take(Tuple template) {
		return this.local.take(template);
	}

	@Override
	public Tuple read(Tuple template) {
		return this.local.read(template);
	}

	@Override
	public Tuple tryTake(Tuple template) {
		// TODO Auto-generated method stub
		return this.local.tryTake(template);
	}

	@Override
	public Tuple tryRead(Tuple template) {
		return this.local.tryRead(template);
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		return this.local.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		return this.local.readAll(template);
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		this.local.eventRegister(mode, timing, template, callback);
	}
	
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }

	@Override
	public void debug(String message) {
		// TODO Auto-generated method stub
		System.err.println(this.getThreadId() + " " + message);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = DEFAULT_SERVER_PORT;
		String serverURI = null;
		Registry registry = null;
		try {
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			registry = LocateRegistry.createRegistry(port);
			serverURI = "rmi://" + InetAddress.getLocalHost().getHostName() + ":" + port + "/LindaServer";
			LindaServer server = new LindaServer();
			server.debug("Binding LindaServer at URI: " + serverURI);
			Naming.rebind(serverURI, server);
		} catch (NumberFormatException e) {
			System.err.println( "The provided parameter is not a port number: " + args[0]);
		} catch (RemoteException e) {
			System.err.println( "There was an issue creating/accessing the registry at port: " + port);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.err.println( "There was an issue accessing the hostname of the system running the LindaServer program.");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			System.err.println( "The following URL for registering the LindaServer is malformed:" + serverURI);
			e.printStackTrace();
		}
	}

}
