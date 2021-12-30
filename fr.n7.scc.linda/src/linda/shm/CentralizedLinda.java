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
    
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }
    
    private boolean canRead() {
    	this.debug( "canRead : writer(" + this.writerInside + ") taker(" + this.takerInside + ")");
    	return ((! this.writerInside) && (! this.takerInside));
    }
    
    private boolean readerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.readingPossible) != 0);
    }
    
	private void waitingToRead(Tuple template) {
		while (! this.canRead()) {
			try {
				this.debug("Read sleeping: " + template);
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
		this.debug("Entering read: " + template);
		this.monitor.lock();
    	do {
    		this.waitingToRead(template);
    		t_read = this.readOnce(template);
    		// If it was not possible to read a tuple compatible with the pattern
    		// sleep until other tuples are written
    		if (t_read == null) {
    			try {
    				this.debug("Read sleeping: " + template);
    				this.readingPossible.await();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	} while (t_read == null);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	this.debug("Exiting read:" + template);
    	return t_read;
    }
  
	/**
	 *
	 */
	@Override
    public Tuple tryRead(Tuple template) {
		this.debug("Entering read: " + template);
		this.monitor.lock();
		this.waitingToRead(template);
    	Tuple t_read = this.readOnce(template);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	this.debug("Exiting read:" + template);
    	return t_read;
    }
	
	@Override
	public Collection<Tuple> readAll(Tuple template) {
		this.debug("Entering read all: " + template);
		this.monitor.lock();
		this.waitingToRead(template);
    	Collection<Tuple> t_read = this.readMany(template);
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	this.debug("Exiting read all:" + template);
    	return t_read;
	}

    
    private boolean canWrite() {
    	this.debug( "canWrite : writer(" + this.writerInside + ") taker(" + this.takerInside + ") reader(" + this.numberReadersInside + ")");
    	return ((this.numberReadersInside == 0) && (! this.takerInside) && (! this.writerInside));
    }
    
    private boolean writerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.writingPossible) != 0);
    }
    
    private void waitingToWrite(Tuple template) {
    	while (! (this.canWrite())) {
    		try {
    			this.debug("Write sleeping: " + template);
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
		this.debug("Entering write: " + tuple);
		this.monitor.lock();
		waitingToWrite(tuple);
    	writerInside = true;
    	// TODO : Should not add if a taker callback is triggered
    	// Triggers readers and takers (TODO : should remove the tuple) 
    	if (! this.triggers(tuple)) {
        	tupleSpaces.add(tuple);
        	this.wakeAfterWriting();
    	}
    	writerInside = false;
    	
    	this.monitor.unlock();
    	this.debug("Exiting write:" + tuple);
    }
    
    private boolean canTake() {
    	this.debug( "canTake : writer(" + this.writerInside + ") taker(" + this.takerInside + ") reader(" + this.numberReadersInside + ")");
    	return ((this.numberReadersInside == 0) && (! this.takerInside) && (! this.writerInside));
    }
    
    private boolean takerWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.takingPossible) != 0);
    }
	
	private void waitingToTake(Tuple template) {
		while (! this.canTake()) {
			try {
				this.debug("Take sleeping: " + template);
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
		this.debug("Taker inside: " + template);
		for(Tuple tuple : this.tupleSpaces) {
			if(tuple.matches(template)) {
				t_take = tuple.deepclone();
				boolean b = this.tupleSpaces.remove(tuple);
				break;
			}
		}
		this.debug("Taker outside: " + template);
		this.takerInside = false;
		return t_take;
	}
	
	private Collection<Tuple> takeMany(Tuple template) {
		Collection<Tuple> t_take = new ArrayList<Tuple>();
		this.takerInside =  true;
		this.debug("Taker inside: " + template);
		Iterator<Tuple> iterator = this.tupleSpaces.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if(tuple.matches(template)) {
				t_take.add(tuple.deepclone());
				iterator.remove();
			}
		}
		this.debug("Taker outside: " + template);
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
		this.debug("Entering take: " + template);
    	this.monitor.lock();
    	do {
    		this.waitingToTake(template);
    		t_take = this.takeOnce(template);
    		// If it was not possible to take a tuple compatible with the pattern
    		// sleep until other tuples are written
    		if (t_take == null) {
    			try {
    				this.debug("Take sleeping: " + template);
    				this.takingPossible.await();
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	} while (t_take == null);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	this.debug("Exiting take:" + template);
    	return t_take;
    }

	@Override
    public Tuple tryTake(Tuple template) {
		this.debug("Entering try take: " + template);
    	this.monitor.lock();
    	this.waitingToTake(template);
    	Tuple t_take = this.takeOnce(template);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	this.debug("Exiting try take:" + template);
    	return t_take;
    }

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		this.debug("Entering try take all: " + template);
    	this.monitor.lock();
    	this.waitingToTake(template);
    	Collection<Tuple> t_take = this.takeMany(template);
    	this.wakeAfterTaking();
    	this.monitor.unlock();
    	this.debug("Exiting try take all:" + template);
    	return t_take;
	}

	@Override
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		// TODO : We need to protect the readers/takers callback lists
		// There should only be a single thread inside eventRegister at a time...
		this.debug("Registering an " + mode + " " + timing + " callback on " + template);
		switch (mode) {
		case READ:
			if ((timing == eventTiming.IMMEDIATE) && this.canRead()) {
				// TODO: Should not be able to read if there is a writer/taker inside.
				// Currently incorrect as it is reading without any synchronisation...
				// It should make a tryRead...
				Collection<Tuple> tuples = this.readMany(template);
				if (tuples.isEmpty()) {
					readers.add(new LindaCallBack(template,callback));
				} else {
					for (Tuple tuple : tuples) {
						this.debug( "Calling an immediate reader callback on " + tuple);
						callback.call(tuple);
					}
				}
			} else {
				readers.add(new LindaCallBack(template,callback));
			}
			break;
		case TAKE:
			if ((timing == eventTiming.IMMEDIATE) && this.canTake()) {
				// TODO: Should not be able to take if there is a reader/writer/taker inside.
				// Currently incorrect as it is taking without any synchronisation...
				// It should make a tryTake...
				Collection<Tuple> tuples = this.takeMany(template);
				if (tuples.isEmpty()) {
					takers.add(new LindaCallBack(template,callback));
				} else {
					for (Tuple tuple : tuples) {
						this.debug( "Calling an immediate taker callback on " + tuple);
						callback.call(tuple);
					}
				}
			} else {
				takers.add(new LindaCallBack(template,callback));
			}
			break;
		}
		
	}
	
	private boolean triggers(Tuple tuple) {
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
		// Then execute all the reader callbacks
		for (LindaCallBack reader : triggered) {
			this.debug( "Calling a reader callback on " + tuple);
			reader.getCallback().call(tuple);
		}
		boolean taken = false;
		LindaCallBack taker = null;
		iterator = this.takers.iterator();
		while (iterator.hasNext() && (! taken)) {
			taker = iterator.next();
			if (tuple.matches(taker.getTemplate())) {
				taken = true;
				iterator.remove();
			}
		}
		if (taken) {
			this.debug( "Calling a taker callback on " + tuple);
			taker.getCallback().call(tuple);
		}
		return taken;
	}

	@Override
	public void debug(String message) {
		// TODO Auto-generated method stub
		System.err.println(this.getThreadId() + " " + message);
	}

}
