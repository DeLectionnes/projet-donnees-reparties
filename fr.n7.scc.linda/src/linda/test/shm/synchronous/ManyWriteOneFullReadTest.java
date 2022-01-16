package linda.test.shm.synchronous;

import java.util.Collection;

import linda.*;

public class ManyWriteOneFullReadTest {
	
	final static int N = 16;

    public static void main(String[] a) {
                
        // final ExtendedLinda linda = new linda.shm.CentralizedLinda();
    	final ExtendedLinda linda = new linda.shm.CentralizedConcurrentLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
        
    	for (int i = 0; i < N; i++ ) {
    		for (int j = 0; j < N; j++) {
    			linda.write(new Tuple(i, j));
    		}
    	}
    	Collection<Tuple> result = linda.readAll(new Tuple(Integer.class, Integer.class));
        linda.debug( result.toString()  );
        linda.stop();
    }
}
