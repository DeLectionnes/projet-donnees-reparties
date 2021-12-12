package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
/* TODO : Improve code sharing. */
/* Maybe rely on callbacks for all actions */
/* The following tests are currently running correctly:
 * - BasicTest1
 * - BasicTest2
 * - CentralizedTestSimple
 * - BasicTestAsyncCallback
 * The following test is currently broken due to concurrent access to triggers iterators :
 * - BasicTestCallback as it registers an immediate callback from the registered callback.
 */
public class CentralizedLinda implements Linda {
	
	/**
	 * Is a writer currently working on the tuples ?
	 */
	private boolean writerInside; 
	
	
	/**
	 * Are there some readers currently working on the tuples ?
	 */
	private int numberReadersInside;
	
	
	/**
	 * Is a taker currently working on the tuples ?
	 */
	private boolean takerInside;
	
	private Lock monitor; 
	
	private Condition readingPossible;
	private Condition writingPossible;
	private Condition takingPossible;
	
	private List<Tuple> tupleSpaces; 
	
	private static class LindaCallBack {
		private Tuple template;
		public Tuple getTemplate() {
			return template;
		}

		public Callback getCallback() {
			return callback;
		}

		private Callback callback;
		
		public LindaCallBack(Tuple template, Callback callback) {
			this.template = template;
			this.callback = callback;
		}
		
	}
	
	private List<LindaCallBack> readers;
	private List<LindaCallBack> takers;
	
    /**
     * 
     */
    public CentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	this.writerInside = false; 
    	this.numberReadersInside = 0;
    	this.takerInside = false;
    	
    	this.writingPossible = monitor.newCondition();
    	this.readingPossible = monitor.newCondition();
    	this.takingPossible = monitor.newCondition();
    	
    	this.tupleSpaces = new ArrayList<Tuple>();
    	this.readers = new ArrayList<LindaCallBack>();
    	this.takers = new ArrayList<LindaCallBack>();
    }
    
    private boolean canRead() {
    	return ((! this.writerInside) && (! this.takerInside));
    }
    
    private boolean readerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.readingPossible) != 0);
    }
    
	private void waitingToRead(Tuple template) {
		while (! this.canRead()) {
			try {
				System.err.println("Read sleeping: " + template);
				this.readingPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private Tuple readOnce(Tuple template) {
    	Tuple t_read = null;
		numberReadersInside += 1;
		for(Tuple tuple : this.tupleSpaces) {
			if (tuple.matches(template)) {
				t_read = tuple.deepclone();
				break;
			}
		}
		numberReadersInside -= 1;
		return t_read;
	}
	
	private Collection<Tuple> readMany(Tuple template) {
    	Collection<Tuple> t_read = new ArrayList<Tuple>();
		numberReadersInside += 1;
		for(Tuple tuple : this.tupleSpaces) {
			if (tuple.matches(template)) {
				t_read.add(tuple.deepclone());
				break;
			}
		}
		numberReadersInside -= 1;
		return t_read;
	}
	
	private void wakeAfterReading() {
    	if (this.readerWaiting()) {
    		this.readingPossible.signalAll();
    	} else {
    		if (this.numberReadersInside == 0) {
    			if (this.writerWaiting()) {
    				this.writingPossible.signalAll();
    			} else {
    				if (this.takerWaiting()) {
        				this.takingPossible.signalAll();
        			}
    			}
    		}
    	}
	}
    
	/**
	 * Reads and returns a tuple if one is already available. Blocks and waits for the next write if none are available.
	 */
	/**
	 *
	 */
	@Override
	public Tuple read(Tuple template) {
    	Tuple t_read = null;
		System.err.println("Entering read: " + template);
		this.monitor.lock();
    	do {
    		this.waitingToRead(template);
    		t_read = this.readOnce(template);
    		// If it was not possible to read a tuple compatible with the pattern
    		// sleep until other tuples are written
    		if (t_read == null) {
    			try {
    				System.err.println("Read sleeping: " + template);
    				this.readingPossible.await();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	} while (t_read == null);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	System.err.println("Exiting read:" + template);
    	return t_read;
    }
  
	/**
	 *
	 */
	@Override
    public Tuple tryRead(Tuple template) {
		System.err.println("Entering read: " + template);
		this.monitor.lock();
		this.waitingToRead(template);
    	Tuple t_read = this.readOnce(template);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	System.err.println("Exiting read:" + template);
    	return t_read;
    }
	
	@Override
	public Collection<Tuple> readAll(Tuple template) {
		System.err.println("Entering read all: " + template);
		this.monitor.lock();
		this.waitingToRead(template);
    	Collection<Tuple> t_read = this.readMany(template);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	System.err.println("Exiting read all:" + template);
    	return t_read;
	}

    
    private boolean canWrite() {
    	return ((this.numberReadersInside == 0) && (! this.takerInside) && (! this.writerInside));
    }
    
    private boolean writerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.writingPossible) != 0);
    }
    
    private void waitingToWrite(Tuple template) {
    	while (! (this.canWrite())) {
    		try {
    			System.err.println("Write sleeping: " + template);
    			this.writingPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void wakeAfterWriting() {
    	if (this.readerWaiting()) {
    		this.readingPossible.signalAll();
    	} else {
    		if (this.writerWaiting()) {
    			this.writingPossible.signalAll();
    		} else {
    			if (this.takerWaiting()) {
        			this.takingPossible.signalAll();
        		}
    		}
   		}
    }
    
	@Override
    public void write(Tuple tuple) {
		System.err.println("Entering write: " + tuple);
		this.monitor.lock();
		waitingToWrite(tuple);
    	writerInside = true;
    	tupleSpaces.add(tuple);
    	this.triggers(tuple);
    	writerInside = false;
    	this.wakeAfterWriting();
    	this.monitor.unlock();
    	System.err.println("Exiting write:" + tuple);
    }
    
    private boolean canTake() {
    	return ((this.numberReadersInside == 0) && (! this.takerInside) && (! writerInside));
    }
    
    private boolean takerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.takingPossible) != 0);
    }
	
	private void waitingToTake(Tuple template) {
		while (! this.canTake()) {
			try {
				System.err.println("Take sleeping: " + template);
				this.takingPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private Tuple takeOnce(Tuple template) {
		Tuple t_take = null;
		this.takerInside =  true;
		System.err.println("Taker inside: " + template);
		for(Tuple tuple : this.tupleSpaces) {
			if(tuple.matches(template)) {
				t_take = tuple.deepclone();
				boolean b = this.tupleSpaces.remove(tuple);
				break;
			}
		}
		System.err.println("Taker outside: " + template);
		this.takerInside = false;
		return t_take;
	}
	
	private Collection<Tuple> takeMany(Tuple template) {
		Collection<Tuple> t_take = new ArrayList<Tuple>();
		this.takerInside =  true;
		System.err.println("Taker inside: " + template);
		Iterator<Tuple> iterator = this.tupleSpaces.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if(tuple.matches(template)) {
				t_take.add(tuple.deepclone());
				iterator.remove();
			}
		}
		System.err.println("Taker outside: " + template);
		this.takerInside = false;
		return t_take;
	}
	
	private void wakeAfterTaking() {
    	if (this.readerWaiting()) {
    		this.readingPossible.signalAll();
    	} else {
    		if (this.writerWaiting()) {
    			this.writingPossible.signalAll();
    		} else {
    			if (this.takerWaiting()) {
        			this.takingPossible.signalAll();
        		}
    		}
   		}
	}
     
	/**
	 *
	 */
	@Override
    public Tuple take(Tuple template) {
    	Tuple t_take = null;
		System.err.println("Entering take: " + template);
    	this.monitor.lock();
    	do {
    		this.waitingToTake(template);
    		t_take = this.takeOnce(template);
    		// If it was not possible to take a tuple compatible with the pattern
    		// sleep until other tuples are written
    		if (t_take == null) {
    			try {
    				System.err.println("Take sleeping: " + template);
    				this.takingPossible.await();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	} while (t_take == null);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	System.err.println("Exiting take:" + template);
    	return t_take;
    }

	@Override
    public Tuple tryTake(Tuple template) {
		System.err.println("Entering try take: " + template);
    	this.monitor.lock();
    	this.waitingToTake(template);
    	Tuple t_take = this.takeOnce(template);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	System.err.println("Exiting try take:" + template);
    	return t_take;
    }

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		System.err.println("Entering try take all: " + template);
    	this.monitor.lock();
    	this.waitingToTake(template);
    	Collection<Tuple> t_take = this.takeMany(template);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	System.err.println("Exiting try take all:" + template);
    	return t_take;
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		// TODO Auto-generated method stub
		System.err.println("Registering an " + mode + " " + timing + " callback on " + template);
		switch (mode) {
		case READ:
			readers.add(new LindaCallBack(template,callback));
			if (timing == eventTiming.IMMEDIATE) {
				// TODO: Checks that there are no issues.
				Collection<Tuple> tuples = this.readMany(template);
				for (Tuple tuple : tuples) {
					System.err.println( "Calling an immediate reader callback on " + tuple);
					callback.call(tuple);
				}
			}
			break;
		case TAKE:
			takers.add(new LindaCallBack(template,callback));
			if (timing == eventTiming.IMMEDIATE) {
				// TODO: Checks that there are no issues.
				Collection<Tuple> tuples = this.takeMany(template);
				for (Tuple tuple : tuples) {
					System.err.println( "Calling an immediate taker callback on " + tuple);
					callback.call(tuple);
				}
			}
			break;
		}
		
	}
	
	private void triggers(Tuple tuple) {
		Iterator<LindaCallBack> iterator = this.readers.iterator();
		while (iterator.hasNext()) {
			LindaCallBack reader = iterator.next();
			if (tuple.matches(reader.getTemplate())) {
				iterator.remove();
				System.err.println( "Calling a reader callback on " + tuple);
				reader.getCallback().call(tuple);
			}

		}
		iterator = this.takers.iterator();
		while (iterator.hasNext()) {
			LindaCallBack taker = iterator.next();
			if (tuple.matches(taker.getTemplate())) {
				iterator.remove();
				System.err.println( "Calling a reader callback on " + tuple);
				taker.getCallback().call(tuple);
			}
		}
	}

	@Override
	public void debug(String prefix) {
		// TODO Auto-generated method stub
		
	}

}
