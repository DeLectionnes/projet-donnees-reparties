package linda.test.shm.asynchronous;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class OneTakeOneWriteTest {

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedSequentialLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
    	
    	Callback action = new PrintCallback();
    	linda.eventRegister( eventMode.READ, eventTiming.FUTURE, new Tuple(4, 5), action);
    	linda.write(new Tuple(4, 5));
    	
    	linda.eventRegister( eventMode.READ, eventTiming.IMMEDIATE, new Tuple(4, 5), action);        
    	linda.write(new Tuple(4, 5));
        linda.stop();
    }
}
