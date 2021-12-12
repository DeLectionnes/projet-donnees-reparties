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
public class CentralizedLinda implements Linda {
	
	/**
	 * Is a writer currently working on the tuples ?
	 */
	private boolean writerInside; 
	private int numberReadersInside;
	private boolean takerInside;
	
	private Lock monitor; 
	
	private Condition readPossible;
	private Condition writePossible;
	private Condition takePossible;
	
	private List<Tuple> tupleSpaces; 
	
    /**
     * 
     */
    public CentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	
    	this.writerInside = false; 
    	this.numberReadersInside = 0;
    	this.takerInside = false;
    	
    	this.writePossible = monitor.newCondition();
    	this.readPossible = monitor.newCondition();
    	this.takePossible = monitor.newCondition();
    	
    	this.tupleSpaces = new ArrayList<Tuple>();
    }
    
	@Override
    public void write(Tuple t) {
    	if (! ((numberReadersInside == 0) && (! takerInside) && (((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writerInside))) {
    		try {
				writePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	writerInside = true;
    	tupleSpaces.add(t);
    	writerInside = false;
    }
    
	
	/**
	 * Reads and returns a tuple if one is already available. Blocks and waits for the next write if none are available.
	 */
	/**
	 *
	 */
	@Override
	public Tuple read(Tuple t) {
    	if (! ((! writerInside) && (! takerInside))) {
    		try {
				readPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	numberReadersInside += 1;

    	Tuple t_read = null;
    	
    	while (t_read == null) {	
	    	for(Tuple tuple : tupleSpaces) {
	    		if (tuple.matches(t)) {
	    			t_read = t.deepclone();
	    			break;
	    		}
	    	}
    	}
    	
    	numberReadersInside -= 1;
    	return t_read;
    }
  
	/**
	 *
	 */
	@Override
    public Tuple tryRead(Tuple t) {
    	if (! ((! writerInside) && (! takerInside))) {
    		try {
				readPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	numberReadersInside += 1;
    	
    	Tuple t_read;
    	
    	for(Tuple tuple : tupleSpaces) {
    		if(tuple.matches(t)) {
    			t_read = t.deepclone();
    			return t_read;
    		}
    	}
    	
    	t_read = null;
    	
    	numberReadersInside -= 1;
    	return t_read;
    }
     
	/**
	 *
	 */
	@Override
    public Tuple take(Tuple t) {
    	
    	if (! ((numberReadersInside == 0) && (! this.takerInside) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.writePossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writerInside))) {
    		try {
				takePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	this.takerInside =  true;
    	Tuple t_take = null;
    	while (t_take == null) {
	    	for(Tuple tuple : tupleSpaces) {
	    		if(tuple.matches(t)) {
	    			t_take = t.deepclone();
	    			boolean b = tupleSpaces.remove(tuple);
	    			break;
	    		}
	    	}
    	}
    	return t_take;
    }

	@Override
    public Tuple tryTake(Tuple t) {
    	
    	if (! ((numberReadersInside == 0) && (! this.takerInside) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.writePossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writerInside)) ) {
    		try {
				takePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	Tuple t_take;
    	
    	for(Tuple tuple : tupleSpaces) {
    		if(tuple.matches(t)) {
    			t_take = t.deepclone();
    			boolean b = tupleSpaces.remove(tuple);
    			return t_take;
    		}
    	}
    	t_take = null;
    	return t_take;
    }

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		// TODO Auto-generated method stub
		return null;
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
