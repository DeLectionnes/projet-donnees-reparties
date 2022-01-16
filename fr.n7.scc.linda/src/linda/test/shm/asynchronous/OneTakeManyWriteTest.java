package linda.test.shm.asynchronous;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class OneTakeManyWriteTest {
	
	final static int N = 16;

    public static void main(String[] a) {
                
        // final ExtendedLinda linda = new linda.shm.CentralizedSequentialLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
    	
    	Callback action = new PrintCallback();
    	linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(4, 5), action);
    	
    	linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(5, 4), action);
        
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			linda.write(new Tuple(i, j));
    		}
    	}
    	
    	linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(5, 5), action);

        linda.stop();
    }
}
