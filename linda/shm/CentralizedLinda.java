package linda.shm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	private boolean writer; 
	private int nbReaders;
	private boolean taker;
	
	private Lock monitor; 
	
	private Condition RPossible;
	private Condition WPossible;
	private Condition TPossible;
	
	private ArrayList<Tuple> TSpaces; 
	
    public CentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	this.writer = false; 
    	this.nbReaders = 0;
    	this.taker = false;
    	
    	this.WPossible = monitor.newCondition();
    	this.RPossible = monitor.newCondition();
    	this.TPossible = monitor.newCondition();
    	
    	this.TSpaces = new ArrayList<Tuple>();
    }

    public void write(Tuple t) {
    	if (! ((nbReaders == 0) && (! taker) && (((ReentrantLock) this.monitor).getWaitQueueLength(this.RPossible) == 0) && (! writer))) {
    		WPossible.await();
    	}
    	writer = true;
    	TSpaces.add(t);
    	writer = false;
    }
    
    public Tuple Read(Tuple t) {
    	if(!(!writer && !taker)) {
    		RPossible.await();
    	}
    	
    	nbReaders += 1;

    	Tuple t_read;
    	
    	while(true) {	
	    	for(tuple:TSpaces) {
	    		if(tuple.matches(t)) {
	    			t_read = t.deepcopy();
	    			break;
	    		}
	    	}
    	}
    	
    	nbReaders -= 1;
    	return t_read;
    }
  
    
    public Tuple tryRead(Tuple t) {
    	if (! ((! writer) && (! taker))) {
    		RPossible.await();
    	}
    	
    	nbReaders += 1;
    	
    	Tuple t_read;
    	
    	for(tuple:TSpaces) {
    		if(tuple.matches(t)) {
    			t_read = t.deepcopy();
    			return t_read;
    		}
    	}
    	
    	t_read = null;
    	
    	nbReaders -= 1;
    	return t_read;
    }
     
    
    public Tuple take(Tuple t) {
    	
    	if (! ((nbReaders == 0) && (! taker) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.WPossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.RPossible) == 0) && (! writer))) {
    		TPossible.await();
    	}
    	take =  true;
    	Tuple t_take;
    	while(true) {
	    	for(tuple:TSpaces) {
	    		if(tuple.matches(t)) {
	    			t_take = t.deepcopy();
	    			boolean b = TSpaces.remove(tuple);
	    			break;
	    		}
	    	}
    	}
    	return t_take;
    }
    
    public Tuple tryTake(Tuple t) {
    	
    	if (! ((nbReaders == 0) && (! taker) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.WPossible) == 0) && 
    			(((ReentrantLock) this.monitor).getWaitQueueLength(this.RPossible) == 0) && (! writer)) ) {
    		TPossible.await();
    	}
    	
    	Tuple t_take;
    	
    	for(tuple:TSpaces) {
    		if(tuple.matches(t)) {
    			t_take = t.deepcopy();
    			boolean b = TSpaces.remove(tuple);
    			return t_take;
    		}
    	}
    	t_take = null;
    	return t_take;
    }
    

}
