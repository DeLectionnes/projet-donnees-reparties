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
 * - CentralizedTestSimple (I don't know what the expected result is, but no errors are signaled).
 * - BasicTestAsyncCallback
 * - BasicTestCallback
 */
public abstract class AbstractCentralizedLinda implements Linda {
	
	/**
	 * Is an event being registered ?
	 */
	
	private boolean eventRegisterInside;
	
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
	
	private Condition eventRegisteringPossible;
	private Condition readingPossible;
	private Condition writingPossible;
	private Condition takingPossible;
	
	protected List<LindaCallBack> readers;
	protected List<LindaCallBack> takers;
	
    /**
     * 
     */
    public AbstractCentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	this.eventRegisterInside = false;
    	this.writerInside = false; 
    	this.numberReadersInside = 0;
    	this.takerInside = false;
    	
    	this.eventRegisteringPossible = monitor.newCondition();
    	this.writingPossible = monitor.newCondition();
    	this.readingPossible = monitor.newCondition();
    	this.takingPossible = monitor.newCondition();
    	
    	this.readers = new ArrayList<LindaCallBack>();
    	this.takers = new ArrayList<LindaCallBack>();
    }
    
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }
    
    private String getStatus() {
    	return "register(" + this.eventRegisterInside + ") reader(" + this.numberReadersInside + ") writer(" + this.writerInside + ") taker(" + this.takerInside + ")";
    }
    
    private boolean canRegister() {
    	this.debug( "canRegister" );
    	return ((! this.eventRegisterInside) && (this.numberReadersInside == 0) && (! this.writerInside) && (! this.takerInside));
    }
    
    private boolean eventRegisteringWaiting() {
    	return (((ReentrantLock) this.monitor).getWaitQueueLength(this.eventRegisteringPossible) != 0);
    }
    
	private void waitingToRegister(Tuple template) {
		while (! this.canRegister()) {
			try {
				this.debug("Register sleeping: " + template);
				this.eventRegisteringPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void wakeAfterRegistering() {
		if (this.eventRegisteringWaiting()) {
			this.eventRegisteringPossible.signalAll();
		} else {
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
	}
    
    private boolean canRead() {
    	this.debug( "canRead" );
    	return ((! this.eventRegisterInside) && (! this.writerInside) && (! this.takerInside));
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
	
	protected abstract Tuple readOnce(Tuple template);
	
	protected abstract Collection<Tuple> readMany(Tuple template);
	
	// TODO : gestion des register
	private void wakeAfterReading() {
		if (this.eventRegisteringWaiting()) {
			this.eventRegisteringPossible.signalAll();
		} else {
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
	}
    
	/**
	 * Reads and returns a tuple if one is already available. Blocks and waits for the next write if none are available.
	 */
	@Override
	public Tuple read(Tuple template) {
    	Tuple t_read = null;
		this.debug("Entering read: " + template);
		this.monitor.lock();
    	do {
    		this.waitingToRead(template);
    		numberReadersInside += 1;
    		t_read = this.readOnce(template);
    		numberReadersInside -= 1;
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
    	this.debug("Exiting read:" + template + " -> " + t_read);
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
		numberReadersInside += 1;
    	Tuple t_read = this.readOnce(template);
		numberReadersInside -= 1;
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
		numberReadersInside += 1;
    	Collection<Tuple> t_read = this.readMany(template);
		numberReadersInside -= 1;
    	this.wakeAfterReading();
    	this.monitor.unlock();
    	this.debug("Exiting read all:" + template);
    	return t_read;
	}

    
    private boolean canWrite() {
    	this.debug( "canWrite");
    	return ((! this.eventRegisterInside) && (this.numberReadersInside == 0) && (! this.takerInside) && (! this.writerInside));
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
    
 // TODO : gestion des register
    private void wakeAfterWriting() {
		if (this.eventRegisteringWaiting()) {
			this.eventRegisteringPossible.signalAll();
		} else {
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
    }
    
    protected abstract void writeOnce(Tuple tuple);
    
	@Override
    public void write(Tuple tuple) {
		this.debug("Entering write: " + tuple);
		this.monitor.lock();
		waitingToWrite(tuple);
    	writerInside = true;
    	// TODO : Should not add if a taker callback is triggered
    	// Triggers readers and takers (TODO : should remove the tuple) 
    	List<LindaCallBack> triggeredReaders = this.triggersReader( tuple );
    	LindaCallBack trigerredTaker = this.triggersTaker( tuple );
    	if (trigerredTaker == null) {
        	writeOnce(tuple);
    	}
    	// TODO : vérifier que c'est au bon endroit.
    	writerInside = false;
    	this.wakeAfterWriting();
    	this.monitor.unlock();
    	this.debug("Execute triggered readers: " + tuple);
		// Then execute all the reader callbacks
		for (LindaCallBack reader : triggeredReaders) {
			this.debug( "Calling a reader callback on " + tuple);
			reader.getCallback().call(tuple);
		}

    	this.debug("Execute triggered taker: " + tuple);
		if (trigerredTaker != null) {
			this.debug( "Calling a taker callback on " + tuple);
			trigerredTaker.getCallback().call(tuple);
		}

    	this.debug("Exiting write:" + tuple);
    }
    
    private boolean canTake() {
    	this.debug( "canTake");
    	return ((! this.eventRegisterInside) && (this.numberReadersInside == 0) && (! this.takerInside) && (! this.writerInside));
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
	
	protected abstract Tuple takeOnce(Tuple template);
	
	protected abstract Collection<Tuple> takeMany(Tuple template);
	
	private void wakeAfterTaking() {
		if (this.eventRegisteringWaiting()) {
			this.eventRegisteringPossible.signalAll();
		} else {
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
    		this.takerInside = true;
    		t_take = this.takeOnce(template);
    		this.takerInside = false;
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
	/* TODO: Il est possible d'améliorer en distinguant les lectures/prises. En effet, la lecture peut être faite en // avec d'autres lectures.
	 * Il faut regarder si c'est un IMMEDIATE, puis selon le mode, passer en lecture/prise non bloquante, en cas d'échec ou si ce n'est pas un
	 * IMMEDIATE, passer en enregistrement.
	 */
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		this.monitor.lock();
		waitingToRegister(template);
		this.eventRegisterInside = true;
		this.debug("Registering a " + mode + " " + timing + " callback on " + template);
		Collection<Tuple> readTuples = null;
		Tuple takenTuple = null;
		switch (mode) {
		case READ:
			if (timing == eventTiming.IMMEDIATE) {
				readTuples = this.readMany(template);
				if (readTuples.isEmpty()) {
					readers.add(new LindaCallBack(template,callback));
				}
			} else {
				readers.add(new LindaCallBack(template,callback));
			}
			break;
		case TAKE:
			if (timing == eventTiming.IMMEDIATE) {
				takenTuple = this.takeOnce(template);
				if (takenTuple == null) {
					takers.add(new LindaCallBack(template,callback));
				}
			} else {
				takers.add(new LindaCallBack(template,callback));
			}
			break;
		}
		this.eventRegisterInside = false;
		this.monitor.unlock();
		if (readTuples != null) {
			for (Tuple tuple : readTuples) {
				this.debug( "Calling an immediate callback on " + tuple);
				callback.call(tuple);
			}
		}
		if (takenTuple != null) {
			this.debug( "Calling an immediate callback on " + takenTuple);
			callback.call(takenTuple);
		}
	}
	
	protected abstract List<LindaCallBack> triggersReader(Tuple tuple);
	
	protected abstract LindaCallBack triggersTaker(Tuple tuple);

	@Override
	public void debug(String message) {
		System.err.println(this.getThreadId() + " " + message + " " + this.getStatus());
	}

}
