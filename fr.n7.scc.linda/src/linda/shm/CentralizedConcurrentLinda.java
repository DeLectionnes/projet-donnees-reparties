/**
 * 
 */
package linda.shm;

import java.util.Collection;
import java.util.List;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author cpantel
 * 
 * We can use several tuple spaces that can be traversed concurrently (using Threads from a pool).
 * It should not be complex for readMany/takeMany.
 * A bit more painful for readOnce/takeOnce as we will get several results to select a single one.
 *
 */
public class CentralizedConcurrentLinda extends AbstractCentralizedLinda {
	
	private int size;
	
	private int nextSlot;
	
	private TupleSpace[] spaces;

	/**
	 * 
	 */
	public CentralizedConcurrentLinda(int _size) {
		this.size = _size;
		this.nextSlot = 0;
		this.spaces = new TupleSpace [this.size];
		for (int i = 0; i < this.size; i++) {
			this.spaces[i] = new TupleSpace();
		}
	}

	@Override
	protected Tuple readOnce(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<Tuple> readMany(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeOnce(Tuple tuple) {
		this.spaces[this.nextSlot].writeOnce(tuple);
		this.nextSlot = (this.nextSlot + 1) % this.size;
	}

	@Override
	protected Tuple takeOnce(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Collection<Tuple> takeMany(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<LindaCallback> triggersReader(Tuple tuple) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected LindaCallback triggersTaker(Tuple tuple) {
		// TODO Auto-generated method stub
		return null;
	}

}
