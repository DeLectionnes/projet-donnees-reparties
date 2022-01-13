/**
 * 
 */
package linda.shm;

import linda.Callback;
import linda.Tuple;

/**
 * @author cpantel
 *
 */
public class LindaCallBack {
	private Tuple template;
	public Tuple getTemplate() {
		return template;
	}

	public Callback getCallback() {
		return callback;
	}

	private Callback callback;
	
	public LindaCallBack(Tuple template, Callback callback) {
		this.template = template;
		this.callback = callback;
	}
	
}
