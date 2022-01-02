/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;
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
public class LindaServeur extends UnicastRemoteObject implements Linda {
	
	private CentralizedLinda local;

	/**
	 * @throws RemoteException
	 */
	public LindaServeur() throws RemoteException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param port
	 * @throws RemoteException
	 */
	public LindaServeur(int port) throws RemoteException {
		super(port);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param port
	 * @param csf
	 * @param ssf
	 * @throws RemoteException
	 */
	public LindaServeur(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
		super(port, csf, ssf);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write(Tuple tuple) {
		local.write(tuple);
	}

	@Override
	public Tuple take(Tuple template) {
		return local.take(template);
	}

	@Override
	public Tuple read(Tuple template) {
		return local.read(template);
	}

	@Override
	public Tuple tryTake(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tuple tryRead(Tuple template) {
		return local.tryRead(template);
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		return local.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		return local.readAll(template);
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		local.eventRegister(mode, timing, template, callback);
	}

	@Override
	public void debug(String prefix) {
		System.err.println( prefix );
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
