package linda.shm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	private boolean writer; 
	private int nbReaders;
	private boolean taker;
	
	private Lock monitor; 
	
	private Condition readPossible;
	private Condition writePossible;
	private Condition takePossible;
	
	private ArrayList<Tuple> TSpaces; 
	
    public CentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	this.writer = false; 
    	this.nbReaders = 0;
    	this.taker = false;
    	
    	this.writePossible = monitor.newCondition();
    	this.readPossible = monitor.newCondition();
    	this.takePossible = monitor.newCondition();
    	
    	this.TSpaces = new ArrayList<Tuple>();
    }

    public void write(Tuple t) {
    	if (! ((nbReaders == 0) && (! taker) && (((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writer))) {
    		try {
				writePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	writer = true;
    	TSpaces.add(t);
    	writer = false;
    }
    
    public Tuple Read(Tuple t) {
    	if (! ((! writer) && (! taker))) {
    		try {
				readPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	nbReaders += 1;

    	Tuple t_read = null;
    	
    	while (t_read == null) {	
	    	for(Tuple tuple : TSpaces) {
	    		if (tuple.matches(t)) {
	    			t_read = t.deepclone();
	    			break;
	    		}
	    	}
    	}
    	
    	nbReaders -= 1;
    	return t_read;
    }
  
    
    public Tuple tryRead(Tuple t) {
    	if (! ((! writer) && (! taker))) {
    		try {
				readPossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	nbReaders += 1;
    	
    	Tuple t_read;
    	
    	for(Tuple tuple : TSpaces) {
    		if(tuple.matches(t)) {
    			t_read = t.deepclone();
    			return t_read;
    		}
    	}
    	
    	t_read = null;
    	
    	nbReaders -= 1;
    	return t_read;
    }
     
    
    public Tuple take(Tuple t) {
    	
    	if (! ((nbReaders == 0) && (! this.taker) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.writePossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writer))) {
    		try {
				takePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	this.taker =  true;
    	Tuple t_take = null;
    	while (t_take == null) {
	    	for(Tuple tuple : TSpaces) {
	    		if(tuple.matches(t)) {
	    			t_take = t.deepclone();
	    			boolean b = TSpaces.remove(tuple);
	    			break;
	    		}
	    	}
    	}
    	return t_take;
    }
    
    public Tuple tryTake(Tuple t) {
    	
    	if (! ((nbReaders == 0) && (! this.taker) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.writePossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.readPossible) == 0) && (! writer)) ) {
    		try {
				takePossible.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	Tuple t_take;
    	
    	for(Tuple tuple : TSpaces) {
    		if(tuple.matches(t)) {
    			t_take = t.deepclone();
    			boolean b = TSpaces.remove(tuple);
    			return t_take;
    		}
    	}
    	t_take = null;
    	return t_take;
    }

	@Override
	public Tuple read(Tuple template) {
		// TODO Auto-generated method stub
		return null;
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
