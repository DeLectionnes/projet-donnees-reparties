/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.shm.AbstractCentralizedLinda;
import linda.shm.CentralizedLinda;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class RemoteLindaImpl extends UnicastRemoteObject implements RemoteLinda {
	
	private AbstractCentralizedLinda local;

	/**
	 * @throws RemoteException
	 */
	public RemoteLindaImpl() throws RemoteException {
		this.local = new CentralizedLinda();
	}

	/**
	 * @param port
	 * @throws RemoteException
	 */
	public RemoteLindaImpl(int port) throws RemoteException {
		super(port);
		this.local = new CentralizedLinda();
	}

	/**
	 *
	 */
	@Override
	public void write(Tuple tuple) {
		this.local.write(tuple);
	}

	/**
	 *
	 */
	@Override
	public Tuple take(Tuple template) {
		return this.local.take(template);
	}

	/**
	 *
	 */
	@Override
	public Tuple read(Tuple template) {
		return this.local.read(template);
	}

	/**
	 *
	 */
	@Override
	public Tuple tryTake(Tuple template) {
		return this.local.tryTake(template);
	}

	/**
	 *
	 */
	@Override
	public Tuple tryRead(Tuple template) {
		return this.local.tryRead(template);
	}

	/**
	 *
	 */
	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		return this.local.takeAll(template);
	}

	/**
	 *
	 */
	@Override
	public Collection<Tuple> readAll(Tuple template) {
		return this.local.readAll(template);
	}

	/**
	 *
	 */
	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, RemoteCallback callback) {
		this.local.eventRegister(mode, timing, template, new RemoteCallbackAdapter( callback));
	}
	
    /**
     * @return
     */
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }

	/**
	 *
	 */
	@Override
	public void debug(String message) {
		System.err.println(this.getThreadId() + " " + message);
	}

}
