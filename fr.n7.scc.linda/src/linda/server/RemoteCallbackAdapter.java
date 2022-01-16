/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;

import linda.Callback;
import linda.Tuple;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class RemoteCallbackAdapter implements Callback {

	/**
	 * 
	 */
	private RemoteCallback proxy;
	
	/**
	 * 
	 */
	public RemoteCallbackAdapter(RemoteCallback _proxy) {
		this.debug("Creating a remote Callback for: " + _proxy);
		this.proxy = _proxy;
	}
	
    /**
     * @return
     */
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }
	
	/**
	 * @param message
	 */
	public void debug(String message) {
		System.err.println(this.getThreadId() + " " + message);
	}

	@Override
	public void call(Tuple tuple) {
		try {
			this.debug("Entering remote Callback: " + tuple);
			this.proxy.call(tuple);
			this.debug("Exiting remote Callback: " + tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
