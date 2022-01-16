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
public abstract class AbstractManyCallable implements Callable<Collection<Tuple>> {
	
	/**
	 * 
	 */
	protected TupleSpace tupleSpacePart;
	
	/**
	 * 
	 */
	protected Tuple template;

	/**
	 * @param _tuples
	 * @param _template
	 */
	public AbstractManyCallable(TupleSpace _tuples, Tuple _template) {
		this.tupleSpacePart = _tuples;
		this.template = _template;
	}

}