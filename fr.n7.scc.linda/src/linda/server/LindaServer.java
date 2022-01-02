/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;
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
	
	public static final String DEFAULT_SERVER_NAME = "localhost";
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
		String servername = null;
		int port = -1;
		String url = null;
		Registry registry = null;
		try {
			if (args.length == 0) {
				servername = DEFAULT_SERVER_NAME;
				port = DEFAULT_SERVER_PORT;
			} else {
				if (args.length == 1) {
					servername = args[0];
				} else {
					servername = args[0];
					port = Integer.parseInt(args[1]);
				}
			}
			LindaServer server = new LindaServer();
			
		} catch (Exception e) {
			
		}
	}

}
