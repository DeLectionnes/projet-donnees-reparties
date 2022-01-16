package linda.test.shm.asynchronous;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class OneWriteOneReadTest {

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedConcurrentLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
                
    	Callback action = new PrintCallback();
    	linda.write(new Tuple(4, 5));
    	linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(4, 5), action);
        linda.stop();
    }
}
