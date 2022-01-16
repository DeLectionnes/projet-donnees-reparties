package linda.search.student;

import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Manager implements Runnable {

    private Linda linda;

    private UUID reqUUID;
    private String pathname;
    private String search;
    private int bestvalue = 10; // Arbitrary value in Searcher
    private String bestresult;
    private Object waiter = new Object(); //The object on which wait is called
    
    private static AtomicInteger nbChercheur = new AtomicInteger();

    public Manager(Linda linda, String pathname, String search) {
        this.linda = linda;
        this.pathname = pathname;
        this.search = search;
        this.reqUUID = UUID.randomUUID();
    }

    private void addSearch(String search) {
        this.search = search;
        
        System.out.println("Search " + this.reqUUID + " for " + this.search);
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, this.reqUUID, String.class, Integer.class), new CbGetResult());
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Searcher, "done", this.reqUUID), new CbSearchDone());
        linda.write(new Tuple(Code.Request, this.reqUUID, this.search));
    }

    private void loadData(String pathname) {
        try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
            stream.limit(10000).forEach(s -> linda.write(new Tuple(Code.Value, s.trim(), this.reqUUID)));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForEndSearch() {
        
	    synchronized (waiter) {
	    	try {
	    		waiter.wait(5000);
	        } catch(InterruptedException e) {
	        	return;
	        }
	    	System.out.println("Timed-out");
	    }
    }
    
    private void end () {
    	linda.tryTake(new Tuple(Code.Request, this.reqUUID, String.class)); // remove query, on utilise un tryTake si jamais plusieurs conditions de fin sont simultanément appeléees.
    	linda.write(new Tuple(Code.Signal, SignalID.SearchEnd, this.reqUUID)); //signal the end of the query.
        System.out.println("query done");
        
    }

    private class CbGetResult implements linda.Callback {
        public void call(Tuple t) {  // [ Result, ?UUID, ?String, ?Integer ]
            String s = (String) t.get(2);
            Integer v = (Integer) t.get(3);
            if (v < bestvalue) {
                bestvalue = v;
                bestresult = s;
                System.out.println("New best (" + bestvalue + "): \"" + bestresult + "\"");
            }
            linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);
        }
    }
    /*Attraper quand un chercheur termine*/
    private class CbCountDownReader implements linda.Callback {
    	public void call(Tuple t) {
    		int newValue = nbChercheur.addAndGet(-1);
    		if (newValue == 0) {
    			linda.write(new Tuple(Code.Signal, SignalID.SearchEnd));
    		} else {
    			linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearcherEnd), this);
    		}
    	}
    }
    /*Attraper Quand un chercheur commence*/
    private class CbCountUpReader implements linda.Callback {
    	public void call(Tuple t) {
    		nbChercheur.addAndGet(1);
    		linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearcherBegin), this);
    	}
    }
    
    /*Attraper quand un chercheur a terminé la requète ou qu'il n'y a plus de chercheurs.*/
    private class CbSearchDone implements linda.Callback {
    	public void call(Tuple t) {
    		end();
    		synchronized (waiter) {
    			waiter.notify();
    		}
            return;
    	}
    }

    public void run() {
        this.loadData(pathname);
        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearchEnd), new CbCountDownReader());
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearcherEnd), new CbCountDownReader());
        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearcherBegin), new CbCountUpReader());
        this.addSearch(search);
        this.waitForEndSearch();
        end();
    }
}
