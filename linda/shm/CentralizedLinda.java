package linda.shm;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
	private boolean writer; 
	private int nbLecters;
	private int nbTake;
	
	private Lock monitor; 
	
	private Condition LPossible;
	private Condition WPossible;
	private Condition TPossible;
	
	private ArrayList<Tuple> TSpaces; 
	
    public CentralizedLinda() {
    	this.monitor = new java.util.concurrent.locks.ReentrantLock();
    	
    	this.writer = false; 
    	this.nbLecteur = 0;
    	this.nbTake = 0;
    	
    	this.EPossible = monitor.newCondition();
    	this.LPossible = monitor.newCondition();
    	this.TPossible = monitor.newCondition();
    	
    	this.TSpaces = new ArrayList<Tuple>();
    }

    public void write(Tuple t) {
    	if(!((nbLecters==0)&&(((ReentrantLock) this.monitor).getWaitQueueLength(this.LPossible) == 0)&& !writer) ) {
    		WPossible.await();
    	}
    	writer = true;
    	TSpaces.add(t);
    }

}
