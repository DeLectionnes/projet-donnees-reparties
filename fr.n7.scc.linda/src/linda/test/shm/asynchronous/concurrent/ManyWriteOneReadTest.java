package linda.test.shm.asynchronous.concurrent;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.test.shm.asynchronous.PrintCallback;

public class ManyWriteOneReadTest {
	
	final static int N = 256;

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedSequentialLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
 
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			final int fi = i;
    			final int fj = j;
    	    	new Thread( new Runnable() {
    				@Override
    				public void run() {
    					linda.write(new Tuple(fi, fj));
    				}
    	    		
    	    	}).start();
    		}
    	}
    	final Callback action = new PrintCallback();
    	new Thread( new Runnable() {
			@Override
			public void run() {
				linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(4, 5), action);
			}
    	}).start();
    }
}
