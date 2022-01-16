package linda.test.shm.synchronous.concurrent;

import linda.*;

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
    	new Thread( new Runnable() {
			@Override
			public void run() {
		    	Tuple result = linda.read(new Tuple(4, 5));
		        linda.debug( result.toString()  );
		        linda.stop();
			}
    	}).start();
    }
}
