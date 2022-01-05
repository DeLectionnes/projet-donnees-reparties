/**
 * 
 */
package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import linda.Tuple;

/**
 * @author cpantel
 *
 */
public class CentralizedLinda extends AbstractCentralizedLinda {
	
	protected List<Tuple> tupleSpaces; 

	/**
	 * 
	 */
	public CentralizedLinda() {
		super();
		// TODO Auto-generated constructor stub
    	this.tupleSpaces = new ArrayList<Tuple>();
	}
	
	protected Tuple readOnce(Tuple template) {
    	Tuple t_read = null;
		for(Tuple tuple : this.tupleSpaces) {
			if (tuple.matches(template)) {
				t_read = tuple.deepclone();
				break;
			}
		}
		return t_read;
	}
	
	protected Collection<Tuple> readMany(Tuple template) {
    	Collection<Tuple> t_read = new ArrayList<Tuple>();
		for(Tuple tuple : this.tupleSpaces) {
			if (tuple.matches(template)) {
				t_read.add(tuple.deepclone());
				break;
			}
		}
		return t_read;
	}
	
	protected List<LindaCallBack> triggersReader(Tuple tuple) {
		Iterator<LindaCallBack> iterator = this.readers.iterator();
		List<LindaCallBack> triggered = new ArrayList<LindaCallBack>();
		
		// First collects all the reader callbacks
		while (iterator.hasNext()) {
			LindaCallBack reader = iterator.next();
			if (tuple.matches(reader.getTemplate())) {
				triggered.add(reader);
				iterator.remove();
			}
		}
		return triggered;
	}
	
	protected LindaCallBack triggersTaker(Tuple tuple) {
		Iterator<LindaCallBack> iterator = this.takers.iterator();
		LindaCallBack triggered = null;
		LindaCallBack taker = null;
		while (iterator.hasNext() && (triggered == null)) {
			taker = iterator.next();
			if (tuple.matches(taker.getTemplate())) {
				triggered = taker;
				iterator.remove();
			}
		}
		return triggered;
	}

	@Override
	protected void writeOnce(Tuple tuple) {
		// TODO Auto-generated method stub
		this.tupleSpaces.add(tuple);
	}
	
	protected Tuple takeOnce(Tuple template) {
		Tuple t_take = null;
		this.debug("Entering takeOnce: " + template);
		for(Tuple tuple : this.tupleSpaces) {
			if(tuple.matches(template)) {
				t_take = tuple.deepclone();
				boolean b = this.tupleSpaces.remove(tuple);
				break;
			}
		}
		this.debug("Exiting takeOnce: " + template + " " + t_take);
		return t_take;
	}
	
	protected Collection<Tuple> takeMany(Tuple template) {
		Collection<Tuple> t_take = new ArrayList<Tuple>();
		this.debug("Entering takeMany: " + template);
		Iterator<Tuple> iterator = this.tupleSpaces.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if(tuple.matches(template)) {
				t_take.add(tuple.deepclone());
				iterator.remove();
			}
		}
		this.debug("Exiting takeMany: " + template + " " + t_take.size());
		return t_take;
	}

}
