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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import linda.Tuple;
import linda.TupleSpace;

/**
 * @author bgros, cpantel, rmonvill
 * 
 * We can use several parts for the tuple space that can be traversed concurrently (using Threads from a pool).
 * It should not be complex for readMany/takeMany.
 * A bit more painful for readOnce/takeOnce as we will get several results to select a single one.
 *
 */
public class CentralizedConcurrentLinda extends AbstractCentralizedLinda {
	
	/**
	 * 
	 */
	private ExecutorService engine;
	
	/**
	 * 
	 */
	private final static int DEFAULT_THREAD_PER_PARTS = 16;
	
	/**
	 * 
	 */
	private final static int DEFAULT_NUMBER_OF_PARTS = 16;
	
	/**
	 * 
	 */
	private final int numberOfParts;
	
	/**
	 * 
	 */
	private final int threadsPerPart;
	
	/**
	 * 
	 */
	private int nextSlot;
	
	/**
	 * 
	 */
	private TupleSpace[] parts;
	
	/**
	 * 
	 */
	public CentralizedConcurrentLinda() {
		this( DEFAULT_NUMBER_OF_PARTS, DEFAULT_THREAD_PER_PARTS);
	}

	/**
	 * @param _numberOfParts 
	 * @param _threadsPerPart 
	 * 
	 */
	public CentralizedConcurrentLinda(int _numberOfParts, int _threadsPerPart) {
		super();
		this.numberOfParts = _numberOfParts;
		this.threadsPerPart = _threadsPerPart;
		this.nextSlot = 0;
		this.parts = new TupleSpace [this.numberOfParts];
		for (int i = 0; i < this.numberOfParts; i++) {
			this.parts[i] = new TupleSpace();
		}
		this.engine = Executors.newFixedThreadPool(this.numberOfParts);
	}
	
	/**
	 *
	 */
	@Override
	public void stop() {
		try {
			this.debug("Entering stop");
			this.engine.shutdown();
			this.engine.awaitTermination(1, TimeUnit.SECONDS);
			this.debug("Exiting stop");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	@Override
	protected Tuple readOnce(Tuple template) {
		CompletionService<Tuple> waiter = new ExecutorCompletionService<Tuple>(this.engine);
		Tuple t_read = null;
		AtomicBoolean cancelled = new AtomicBoolean(false);
		for (int i = 0; i < this.numberOfParts; i++) {
			waiter.submit(new OneReadCallable( this.parts[i], template, cancelled));
		}
		Future<Tuple> result;
		try {
			int finished = 0;
			result = waiter.take();
			finished++;
			while ((result.get() == null) && (finished < this.numberOfParts)) {
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

	/**
	 *
	 */
	@Override
	protected Collection<Tuple> readMany(Tuple template) {
		CompletionService<Collection<Tuple>> waiter = new ExecutorCompletionService<Collection<Tuple>>(this.engine);
		Collection<Tuple> t_read = new ArrayList<Tuple>();
		for (int i = 0; i < this.numberOfParts; i++) {
			waiter.submit(new ManyReadCallable( this.parts[i], template));
		}
		Future<Collection<Tuple>> result;
		try {
			for (int i = 0; i < this.numberOfParts; i++) {
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

	/**
	 *
	 */
	@Override
	protected void writeOnce(Tuple tuple) {
		this.parts[this.nextSlot].writeOnce(tuple);
		this.nextSlot = (this.nextSlot + 1) % this.numberOfParts;
	}

	/**
	 *
	 */
	@Override
	protected Tuple takeOnce(Tuple template) {
		CompletionService<Tuple> waiter = new ExecutorCompletionService<Tuple>(this.engine);
		Tuple t_taken = null;
		AtomicBoolean cancelled = new AtomicBoolean(false);
		for (int i = 0; i < this.numberOfParts; i++) {
			waiter.submit(new OneTakeCallable( this.parts[i], template, cancelled));
		}
		Future<Tuple> result;
		try {
			int finished = 0;
			result = waiter.take();
			finished++;
			while ((result.get() == null) && (finished < this.numberOfParts)) {
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

	/**
	 *
	 */
	@Override
	protected Collection<Tuple> takeMany(Tuple template) {
		CompletionService<Collection<Tuple>> waiter = new ExecutorCompletionService<Collection<Tuple>>(this.engine);
		Collection<Tuple> t_taken = new ArrayList<Tuple>();
		for (int i = 0; i < this.numberOfParts; i++) {
			waiter.submit(new ManyTakeCallable( this.parts[i], template));
		}
		Future<Collection<Tuple>> result;
		try {
			for (int i = 0; i < this.numberOfParts; i++) {
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
