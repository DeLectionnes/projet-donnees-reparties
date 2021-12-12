package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
/* TODO : Improve code sharing. */
public class CentralizedLinda implements Linda {
	
	/**
	 * Is a writer currently working on the tuples ?
	 */
	private boolean writerInside; 
	private int numberReadersInside;
	private boolean takerInside;
	
	private Lock monitor; 
	
	private Condition readingPossible;
	private Condition writingPossible;
	private Condition takingPossible;
	
	private List<Tuple> tupleSpaces; 
	
	private List<Callback> callbacks;
	
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
    	this.callbacks = new ArrayList<Callback>();
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
    public void write(Tuple template) {
		System.err.println("Entering write: " + template);
		this.monitor.lock();
		waitingToWrite(template);
    	writerInside = true;
    	tupleSpaces.add(template);
    	writerInside = false;
    	this.wakeAfterWriting();
    	this.monitor.unlock();
    	System.err.println("Exiting write:" + template);
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
		for(Tuple tuple : this.tupleSpaces) {
			if(tuple.matches(template)) {
				t_take.add(tuple.deepclone());
				boolean b = this.tupleSpaces.remove(tuple);
				break;
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
		
	}

	@Override
	public void debug(String prefix) {
		// TODO Auto-generated method stub
		
	}

}
