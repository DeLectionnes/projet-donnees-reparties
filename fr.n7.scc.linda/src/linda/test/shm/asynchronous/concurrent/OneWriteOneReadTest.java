package linda.test.shm.asynchronous.concurrent;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;
import linda.test.shm.asynchronous.PrintCallback;

public class OneWriteOneReadTest {

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
    	final Callback action = new PrintCallback();
    	new Thread( new Runnable() {
			@Override
			public void run() {
				linda.eventRegister( eventMode.TAKE, eventTiming.IMMEDIATE, new Tuple(4, 5), action);
			}
    	}).start();
    }
}
