package linda.test;

import linda.*;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class CentralizedTestSimple {
	private static int i;
	// final static Linda linda = new linda.shm.CentralizedLinda();
	final static Linda linda = new linda.server.LindaClient("//localhost:4000/LindaServer");
	
	
	private static class TestCallback implements Callback {
		
		public Tuple testTuple;
		
		public TestCallback(Tuple t) {
			this.testTuple = t;
		}
		
        public void call(Tuple t) {
            assert(this.testTuple.matches(t));
        }
    }
	
	
	private static class Slave1 {
		public Slave1() {
			new Thread() {
				public void run() {
					linda.write(new Tuple(i,i-1, i+1));
					assert(linda.read(new Tuple(i, Integer.class, Integer.class)).matches(new Tuple(i,i-1, i+1)));
					try {
				        Thread.sleep(500);
				    } catch (InterruptedException e) {
				        e.printStackTrace();
				    }
					
					assert(linda.take(new Tuple(Integer.class, i-1, Integer.class)).matches(new Tuple(i,i-1, i+1)));
					try {
				        Thread.sleep(500);
				    } catch (InterruptedException e) {
				        e.printStackTrace();
				    }
					assert(!(linda.read(new Tuple(Integer.class, Integer.class, Integer.class)).matches(new Tuple(i,i-1, i+1))));
					assert(linda.tryTake(new Tuple(Integer.class, Integer.class, Integer.class)).matches(null));
				}
			}.start();
		}
	}
	
	private static class Slave2 {
		public Slave2() {
			new Thread() {
				public void run() {
					Tuple voulu = new Tuple(i,i-1, 'a');
					Tuple expected = new Tuple(Integer.class, i-1, Character.class);
				linda.eventRegister(eventMode.READ, eventTiming.IMMEDIATE, expected, new AsynchronousCallback(new TestCallback(voulu)));
				try {
			        Thread.sleep(500);
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }
				linda.eventRegister(eventMode.TAKE, eventTiming.IMMEDIATE, expected, new AsynchronousCallback(new TestCallback(voulu)));
				
				}
			}.start();
		}
	}
	
	private static class Slave3 {
		public Slave3() {
			new Thread() {
				public void run() {
					Tuple voulu = new Tuple(i,i-1, 'a');
					Tuple expected = new Tuple(Integer.class, i-1, Character.class);
					linda.write(voulu);
					assert(linda.tryTake(expected) == null);
				}
			}.start();
		}
	}
	
	private static class Slave4 {
		public Slave4() {
			new Thread() {
				public void run() {
					Tuple voulu = new Tuple(i,i-1, 'b');
					Tuple expected = new Tuple(i, Integer.class, Character.class);
					linda.eventRegister(eventMode.READ, eventTiming.FUTURE, expected, new AsynchronousCallback(new TestCallback(voulu)));
					try {
				        Thread.sleep(500);
				    } catch (InterruptedException e) {
				        e.printStackTrace();
				    }
					linda.eventRegister(eventMode.TAKE, eventTiming.FUTURE, expected, new AsynchronousCallback(new TestCallback(voulu)));
				}
			}.start();
		}
	}
	
	private static class Slave5 {
		public Slave5() {
			new Thread() {
				public void run() {
					linda.take(new Tuple(i));
					linda.write(new Tuple(i+1));
				}
			}.start();
		}
	}
	
	
	
	public static void main(String args[]) {
		/*Premier test sur les matchs à longueur de tuple constant*/
		System.out.println("Initialisation");
		linda.write(new Tuple(0,0,0));
		System.out.println("Tests simples");
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
				
				assert(linda.read(motif5).matches(motif5));
				assert(linda.read(motif6).matches(motif6));
				assert(linda.read(motif7).matches(motif7));
				assert(linda.read(motif8).matches(motif8));
				assert(linda.read(motif9).matches(motif9));
				
				assert(linda.read(motif1).matches(t1) || linda.read(motif1).matches(t2) || linda.read(motif1).matches(t3));
				linda.take(t1);
				linda.take(t2);
				linda.take(t3);
				linda.take(t4);
				linda.take(t5);
				linda.take(t6);
				linda.take(t7);
				linda.take(t8);
				linda.take(t9);
				linda.take(t10);
				
			}
	}.start();
	
		System.out.println("Tests Callback 1");
		try {
	        Thread.sleep(1000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
		
		for (i = 0; i < 30; i++ ) {
			new Slave1();
		}
		
		for (i = 0; i < 40; i++) {
			new Slave2();
		}
		
		try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		System.out.println("Tests Callback 2");
		for (i = 0; i < 40; i++) {
			new Slave3();
		}
		
		try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
		for (i=0; i< 30; i++) {
			linda.write(new Tuple(i, i+1, 'a'));
		}
		
		for (i=0; i< 30; i++) {
			new Slave4();
		}
		
		try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
		for (i=0; i< 30; i++) {
			linda.write(new Tuple(i, i+1, 'b'));
		}
		
		for (i=0; i< 30; i++) {
			assert (linda.tryTake(new Tuple(i, Integer.class, Character.class)).matches(new Tuple(i, i+1, 'a')));
		}
		
		try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
		System.out.println("Tests Final Callback");
		assert (linda.tryTake(new Tuple(Object.class, Object.class, Object.class)).matches(new Tuple(i, i+1, 'a')));
		assert (linda.tryTake(new Tuple(Object.class, Object.class)) == null);
		System.out.println(Thread.activeCount());
		
		
		System.out.println("Tests attente");
		linda.write(new Tuple(0));
		
		for (i = 1; i < 30; i++) {
			new Slave5();
		}
		try {
	        Thread.sleep(500);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		
		linda.write(new Tuple(1));
		assert(linda.tryTake(new Tuple(Integer.class)).matches(new Tuple(31)));
		try {
	        Thread.sleep(5000);
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
		System.out.println("Tests Final attente");
		assert (linda.tryTake(new Tuple(Object.class)).matches(new Tuple(0)));
		System.out.println(Thread.activeCount());
		
	}
	
}
