package applications;

import java.util.ArrayList;
import java.util.List;
import linda.Linda;
import linda.Tuple;

public class CribleEratosthene {
	static int limite;
	static int nbThreads;
	static double sqrt;
	static List<Integer> resultat = new ArrayList<Integer>();
	// final static Linda linda = new linda.shm.CentralizedLinda();
	final static Linda linda = new linda.shm.CentralizedConcurrentLinda( 16 );
	// final static Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");
	public static void main(String args[]) throws InterruptedException {
		System.err.close();
		limite = Integer.parseInt(args[0]);
		nbThreads = Integer.parseInt(args[1]);
		sqrt = Math.sqrt((double) limite);
		for (int depot = 2; depot<= limite; depot++) {
			linda.write(new Tuple(depot));
		}
		
		for(double i=0; i<nbThreads; i++) {
			double iterateur = 2+i*(sqrt-2)/nbThreads;
			new Thread() {
				public void run() {
					
					cribler(iterateur);
				}
			}.start();
		}
		
		// AFAIRE : Utiliser une barrière pour attendre la fin de processus de criblage avant d'afficher les nombres premiers. Sinon, l'attente
		// peut ne pas être suffisante.
		Thread.sleep((long) 10000);
		int count = 0;
		for (int depot = 2; depot<= limite; depot++) {
			Tuple recup = linda.tryTake(new Tuple(depot));
			
			if(recup != null) {
				System.out.println(depot);
				count++;
			}
		}
		System.out.println("Nombre total de premiers : ");
		System.out.print(count);
		
	}

	public static void cribler(double debut) {
		for (double i = debut; i<= sqrt; i++) {
			Tuple prime = linda.tryRead(new Tuple((int)i));
			if (prime!=null) {
				for (int j=2; j<= 1 + limite/i; j++) {
					Tuple multiple = linda.tryTake(new Tuple((int)i*j));
				}
			}
		}
	}
}
