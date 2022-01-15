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
import linda.shm.AbstractCentralizedLinda;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class LindaServer {
	
	public static final int DEFAULT_SERVER_PORT = 4000;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int port = DEFAULT_SERVER_PORT;
		String serverURI = null;
		Registry registry = null;
		RemoteLinda server = null; 
		try {
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			registry = LocateRegistry.createRegistry(port);
			serverURI = "rmi://" + InetAddress.getLocalHost().getHostName() + ":" + port + "/LindaServer";
			server = new RemoteLindaImpl(port);
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
