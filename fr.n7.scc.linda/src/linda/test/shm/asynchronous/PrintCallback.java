/**
 * 
 */
package linda.test.shm.asynchronous;

import linda.Callback;
import linda.Tuple;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class PrintCallback implements Callback {
	
	/**
	 * 
	 */
	public PrintCallback() {
	}

	@Override
	public void call(Tuple t) {
		System.out.println(t);
	}

}
