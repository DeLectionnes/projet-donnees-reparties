package linda.test.synchronous;

import linda.*;

public class OneWriteOneTakeTest {

    public static void main(String[] a) {
                
        // final Linda linda = new linda.shm.CentralizedLinda();
    	final Linda linda = new linda.shm.CentralizedConcurrentLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
                
    	linda.write(new Tuple(4, 5));
    	Tuple result = linda.take(new Tuple(4, 5));
        linda.debug( result.toString()  );
    }
}
