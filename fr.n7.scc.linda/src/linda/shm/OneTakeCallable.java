/**
 * 
 */
package linda.shm;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class OneTakeCallable extends AbstractOneCallable {
	
	/**
	 * @param _tupleSpacePart
	 * @param _template
	 * @param _cancelled
	 */
	public OneTakeCallable(TupleSpace _tupleSpacePart, Tuple _template, AtomicBoolean _cancelled) {
		super(_tupleSpacePart, _template, _cancelled);
	}

	/**
	 * Calls the takeOnce method from one part of the tuple space using the cancelled AtomicBoolean to synchronize the concurrent
	 * reading on the other parts of the tuple space. 
	 */
	@Override
	public Tuple call() throws Exception {
		return this.tupleSpacePart.takeOnce(this.template, this.cancelled);
	}

}
