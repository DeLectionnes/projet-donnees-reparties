/**
 * 
 */
package linda.server;

import java.rmi.RemoteException;

import linda.Callback;
import linda.Tuple;

/**
 * @author cpantel
 *
 */
public class RemoteCallbackAdapter implements Callback {

	private RemoteCallback proxy;
	
	/**
	 * 
	 */
	public RemoteCallbackAdapter(RemoteCallback _proxy) {
		// TODO Auto-generated constructor stub
		this.proxy = _proxy;
	}

	@Override
	public void call(Tuple tuple) {
		// TODO Auto-generated method stub
		try {
			this.proxy.call(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
