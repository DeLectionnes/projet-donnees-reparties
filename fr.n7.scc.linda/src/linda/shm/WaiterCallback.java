/**
 * 
 */
package linda.shm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import linda.Callback;
import linda.Tuple;

/**
 * @author cpantel
 * Wait for the writing of a tuple matching a template when a read or take action could not succeed as an appropriate tuple was not in the tuple space.
 */
public class WaiterCallback implements Callback {
	
	/**
	 * Need to have access to Linda monitor in order to signal the waiting read or take action.
	 */
	private Lock monitor;
	
	/**
	 * Single use condition that allows the read or take action to block while waiting for the appropriate write.
	 */
	private Condition condition;
	
	/**
	 * Allow to store and provide access to the result when the callback is triggered.
	 */
	private Tuple result;

	/**
	 * 
	 */
	public WaiterCallback(Lock _monitor) {
		this.monitor = _monitor;
		this.condition = this.monitor.newCondition();
		this.result = null;
	}

	/**
	 *
	 */
	@Override
	public void call(Tuple tuple) {
		this.result = tuple;
		this.monitor.lock();
		this.condition.signal();
		this.monitor.unlock();
	}

	/**
	 * @return the result
	 */
	public Tuple getResult() {
		return this.result;
	}

	public Condition getCondition() {
		return this.condition;
	}

}
