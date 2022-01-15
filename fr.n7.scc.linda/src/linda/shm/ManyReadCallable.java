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
public class ManyReadCallable extends AbstractManyCallable {
	
	/**
	 * 
	 */
	public ManyReadCallable(TupleSpace _tupleSpacePart, Tuple _template) {
		super(_tupleSpacePart, _template);
	}

	/**
	 * Calls the readOnce method from one part of the tuple space using the cancelled AtomicBoolean to synchronize the concurrent
	 * reading on the other parts of the tuple space. 
	 */
	@Override
	public Collection<Tuple> call() throws Exception {
		return this.tupleSpacePart.readMany(this.template);
	}

}
