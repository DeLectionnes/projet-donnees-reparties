/**
 * 
 */
package linda.shm;

import linda.Callback;
import linda.Tuple;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class LindaCallback {
	private Tuple template;
	public Tuple getTemplate() {
		return template;
	}

	public Callback getCallback() {
		return callback;
	}

	private Callback callback;
	
	public LindaCallback(Tuple template, Callback callback) {
		this.template = template;
		this.callback = callback;
	}
	
}
