/**
 * 
 */
package linda.shm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author bgros, cpantel, rmonvill
 *
 */
public class CentralizedLinda extends AbstractCentralizedLinda {
	
	/**
	 * A single tuple space is used in this sequential (almost) version.
	 */
	protected TupleSpace tuples; 

	/**
	 * 
	 */
	public CentralizedLinda() {
		super();
    	this.tuples = new TupleSpace();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public CentralizedLinda(String name) throws IOException {
		super();
    	this.tuples = new TupleSpace(name);
	}
	
	/**
	 * @param name
	 * @throws IOException
	 */
	public void store(String name) throws IOException {
		this.tuples.store(name);
	}
	
	/**
	 *
	 */
	protected Tuple readOnce(Tuple template) {
    	Tuple t_read = null;
//		this.debug("Entering readOnce: " + template);
    	t_read = this.tuples.readOnce(template);
//		this.debug("Exiting readOnce: " + template + " " + t_read);
		return t_read;
	}
	
	/**
	 *
	 */
	protected Collection<Tuple> readMany(Tuple template) {
    	Collection<Tuple> t_read = null;
//		this.debug("Entering readMany: " + template);
    	t_read = this.tuples.readMany(template);
//		this.debug("Exiting readMany: " + template + " " + t_read);
		return t_read;
	}
	
	/**
	 *
	 */
	@Override
	protected void writeOnce(Tuple tuple) {
		this.tuples.writeOnce(tuple);
	}
	
	/**
	 *
	 */
	protected Tuple takeOnce(Tuple template) {
		Tuple t_take = null;
//		this.debug("Entering takeOnce: " + template);
		t_take = this.tuples.takeOnce(template);
//		this.debug("Exiting takeOnce: " + template + " " + t_take);
		return t_take;
	}
	
	/**
	 *
	 */
	protected Collection<Tuple> takeMany(Tuple template) {
		Collection<Tuple> t_take = null;
//		this.debug("Entering takeMany: " + template);
		t_take = this.tuples.takeMany(template);
//		this.debug("Exiting takeMany: " + template + " " + t_take.size());
		return t_take;
	}

	@Override
	public void stop() {
		this.debug("Stopping");
	}

}
