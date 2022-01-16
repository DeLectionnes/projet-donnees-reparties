package linda.test.shm.synchronous.concurrent;

import linda.*;

public class OneWriteOneTakeTest {

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedSequentialLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
                
    	new Thread( new Runnable() {
			@Override
			public void run() {
				linda.write(new Tuple(4, 5));
			}
    		
    	}).start();
    	new Thread( new Runnable() {
			@Override
			public void run() {
		    	Tuple result = linda.take(new Tuple(4, 5));
		        linda.debug( result.toString()  );
		        linda.stop();
			}
    	}).start();
    }
}
