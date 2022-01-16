/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import linda.Callback;
import linda.Tuple;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class RemoteCallbackImpl extends UnicastRemoteObject implements RemoteCallback {
	
	private Callback local;

	/**
	 * @throws RemoteException
	 */
	public RemoteCallbackImpl(Callback _local) throws RemoteException {
		this.local = _local;
	}

	@Override
	public void call(Tuple t) {
		this.local.call(t);
	}

}
