package linda.test;

import linda.*;
import linda.Tuple;

public class CentralizedTestSimple {

	
	public static void main() {
		final Linda linda = new linda.shm.CentralizedLinda();
		/*Premier test sur les matchs à longueur de tuple constant*/
		Tuple t1 = new Tuple(1,2);
		Tuple t2 = new Tuple(1,3);
		Tuple t3 = new Tuple(3,1);
		Tuple t4 = new Tuple("un",2);
		Tuple t5 = new Tuple(1,"deux");
		Tuple t6 = new Tuple("un","deux");
		Tuple t7 = new Tuple("42",true);
		Tuple t8 = new Tuple(Object.class,t7);
		Tuple t9 = new Tuple(t1,t4);
		Tuple t10 = new Tuple(t1.getClass(),8);
		
		Tuple motif1 = new Tuple(Integer.class, Integer.class);
		Tuple motif2 = new Tuple(Integer.class, 2);
		Tuple motif3 = new Tuple(String.class, Integer.class);
		Tuple motif4 = new Tuple(Integer.class, String.class);
		Tuple motif5 = new Tuple(String.class, Boolean.class);
		Tuple motif6 = new Tuple((Object.class).getClass(), t7.getClass());
		Tuple motif7 = new Tuple(t1.getClass(), t9);
		Tuple motif8 = new Tuple(t1.getClass().getClass(), Integer.class);
		Tuple motif9 = new Tuple(3, Object.class);
		
		new Thread() {
			public void run() {
				try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
				linda.write(t1);
				linda.write(t2);
				linda.write(t3);
				linda.write(t4);
				linda.write(t5);
				linda.write(t6);
				linda.write(t7);
				linda.write(t8);
				linda.write(t9);
				linda.write(t10);
				
			}
		}.start();
		
		new Thread() {
			public void run() {
				try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
				assert(linda.read(motif1).matches(motif1));
				assert(linda.read(motif2).matches(motif5));
				assert(linda.read(motif3).matches(motif3));
				assert(linda.read(motif4).matches(motif4));
				assert(linda.read(motif5).matches(motif5));
				assert(linda.read(motif6).matches(motif6));
				assert(linda.read(motif7).matches(motif7));
				assert(linda.read(motif8).matches(motif8));
				assert(linda.read(motif9).matches(motif9));
				
				assert(linda.read(motif1).matches(t1) || linda.read(motif1).matches(t2) || linda.read(motif1).matches(t3));
		}
	}.start();
		
		
		
	}
	
}
