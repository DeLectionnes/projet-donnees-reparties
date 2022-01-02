package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import linda.server.LindaServer;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	
	private Linda server;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
        // TO BE COMPLETED
    	try {
			this.server = (Linda) Naming.lookup(serverURI);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void write(Tuple tuple) {
		// TODO Auto-generated method stub
		this.server.write(tuple);
	}

	@Override
	public Tuple take(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.take(template);
	}

	@Override
	public Tuple read(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.read(template);
	}

	@Override
	public Tuple tryTake(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.tryTake(template);
	}

	@Override
	public Tuple tryRead(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.tryRead(template);
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.takeAll(template);
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		// TODO Auto-generated method stub
		return this.server.readAll(template);
	}

	@Override
	/*
	 * TODO : Il faut soit créer un callback qui est accessible à distance, soit créer un callback serialisable qui sera exécuté sur le serveur.
	 * Nous n'avons pas le doit de modifier le callback existant, donc il n'est pas possible d'en faire une version sérialisable...
	 * Il faut donc créer un objet local accessible à distance qui encapsule le callback local.
	 */
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		// TODO Auto-generated method stub
		this.server.eventRegister(mode, timing, template, callback);
	}
	
    private String getThreadId() {
    	return Thread.currentThread().getName();
    }

	@Override
	public void debug(String message) {
		// TODO Auto-generated method stub
		System.err.println(this.getThreadId() + " " + message);
	}
    
    // TO BE COMPLETED
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String servername = null;
		int port = -1;
		try {
			if (args.length == 0) {
				servername = LindaServer.DEFAULT_SERVER_NAME;
				port = LindaServer.DEFAULT_SERVER_PORT;
			} else {
				if (args.length == 1) {
					servername = args[0];
				} else {
					servername = args[0];
					port = Integer.parseInt(args[1]);
				}
			}
			LindaClient me = new LindaClient( servername );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
