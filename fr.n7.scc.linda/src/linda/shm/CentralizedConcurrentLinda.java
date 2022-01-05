/**
 * 
 */
package linda.shm;

import java.util.Collection;
import java.util.List;

import linda.Tuple;

/**
 * @author cpantel
 * 
 * We can use several tuple spaces that can be traversed concurrently (using Threads from a pool).
 * It should not be complex for readMany/takeMany.
 * A bit more painful for readOnce/takeOnce as we will get several results to select a single one.
 *
 */
public class CentralizedConcurrentLinda extends AbstractCentralizedLinda {

	/**
	 * 
	 */
	public CentralizedConcurrentLinda() {
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub

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
	protected List<LindaCallBack> triggersReader(Tuple tuple) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected LindaCallBack triggersTaker(Tuple tuple) {
		// TODO Auto-generated method stub
		return null;
	}

}
