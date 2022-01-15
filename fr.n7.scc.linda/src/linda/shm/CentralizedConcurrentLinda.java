/**
 * 
 */
package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author cpantel
 * 
 * We can use several parts for the tuple space that can be traversed concurrently (using Threads from a pool).
 * It should not be complex for readMany/takeMany.
 * A bit more painful for readOnce/takeOnce as we will get several results to select a single one.
 *
 */
public class CentralizedConcurrentLinda extends AbstractCentralizedLinda {
	
	private ExecutorService engine;
	
	private int size;
	
	private int nextSlot;
	
	private TupleSpace[] parts;

	/**
	 * 
	 */
	public CentralizedConcurrentLinda(int _size) {
		super();
		this.size = _size;
		this.nextSlot = 0;
		this.parts = new TupleSpace [this.size];
		for (int i = 0; i < this.size; i++) {
			this.parts[i] = new TupleSpace();
		}
		this.engine = Executors.newFixedThreadPool(this.size);
	}

	@Override
	protected Tuple readOnce(Tuple template) {
		CompletionService<Tuple> waiter = new ExecutorCompletionService<Tuple>(this.engine);
		Tuple t_read = null;
		AtomicBoolean cancelled = new AtomicBoolean(false);
		for (int i = 0; i < this.size; i++) {
			waiter.submit(new OneReadCallable( this.parts[i], template, cancelled));
		}
		Future<Tuple> result;
		try {
			int finished = 0;
			result = waiter.take();
			finished++;
			while ((result.get() == null) && (finished < this.size)) {
				result = waiter.take();
				finished++;
			}
			t_read = result.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t_read;
	}

	@Override
	protected Collection<Tuple> readMany(Tuple template) {
		CompletionService<Collection<Tuple>> waiter = new ExecutorCompletionService<Collection<Tuple>>(this.engine);
		Collection<Tuple> t_read = new ArrayList<Tuple>();
		for (int i = 0; i < this.size; i++) {
			waiter.submit(new ManyReadCallable( this.parts[i], template));
		}
		Future<Collection<Tuple>> result;
		try {
			for (int i = 0; i < this.size; i++) {
				result = waiter.take();
				if (result.get() != null) {
					t_read.addAll(result.get());
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t_read;
	}

	@Override
	protected void writeOnce(Tuple tuple) {
		this.parts[this.nextSlot].writeOnce(tuple);
		this.nextSlot = (this.nextSlot + 1) % this.size;
	}

	@Override
	protected Tuple takeOnce(Tuple template) {
		CompletionService<Tuple> waiter = new ExecutorCompletionService<Tuple>(this.engine);
		Tuple t_taken = null;
		AtomicBoolean cancelled = new AtomicBoolean(false);
		for (int i = 0; i < this.size; i++) {
			waiter.submit(new OneTakeCallable( this.parts[i], template, cancelled));
		}
		Future<Tuple> result;
		try {
			int finished = 0;
			result = waiter.take();
			finished++;
			while ((result.get() == null) && (finished < this.size)) {
				result = waiter.take();
				finished++;
			}
			t_taken = result.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t_taken;
	}

	@Override
	protected Collection<Tuple> takeMany(Tuple template) {
		CompletionService<Collection<Tuple>> waiter = new ExecutorCompletionService<Collection<Tuple>>(this.engine);
		Collection<Tuple> t_taken = new ArrayList<Tuple>();
		for (int i = 0; i < this.size; i++) {
			waiter.submit(new ManyTakeCallable( this.parts[i], template));
		}
		Future<Collection<Tuple>> result;
		try {
			for (int i = 0; i < this.size; i++) {
				result = waiter.take();
				if (result.get() != null) {
					t_taken.addAll(result.get());
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t_taken;
	}

}
