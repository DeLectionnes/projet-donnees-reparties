package linda.shm;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public abstract class AbstractOneCallable implements Callable<Tuple> {

	/**
	 * 
	 */
	protected AtomicBoolean cancelled;
	
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
	 * @param _cancelled
	 */
	public AbstractOneCallable(TupleSpace _tuples, Tuple _template, AtomicBoolean _cancelled) {
		this.tupleSpacePart = _tuples;
		this.template = _template;
		this.cancelled = _cancelled;
	}

}