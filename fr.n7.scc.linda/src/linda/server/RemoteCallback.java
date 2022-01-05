/**
 * 
 */
package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import linda.Callback;
import linda.Tuple;

/**
 * @author cpantel
 *
 */
public interface RemoteCallback extends Remote {
	
    /** Callback when a tuple appears. 
     * See Linda.eventRegister for details.
     * 
     * @param t the new tuple
     */
    void call(Tuple t) throws RemoteException;

}
