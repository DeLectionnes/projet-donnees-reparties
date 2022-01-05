package linda.server;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import linda.server.LindaServer;
import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements Linda {
	
	private RemoteLinda server;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
    	try {
    		this.debug( "Connecting to remote Linda server at URL: " + serverURI);
    		Remote proxy = Naming.lookup(serverURI);
    		for (Class<?> c : proxy.getClass().getInterfaces()) {
    			this.debug("Proxy : " + c.getName());
    		}
			this.server = (RemoteLinda) proxy;
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
		try {
			this.server.write(tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Tuple take(Tuple template) {
		try {
			return this.server.take(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple read(Tuple template) {
		try {
			return this.server.read(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryTake(Tuple template) {
		try {
			return this.server.tryTake(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryRead(Tuple template) {
		try {
			return this.server.tryRead(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		try {
			return this.server.takeAll(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<Tuple>();
		}
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		try {
			return this.server.readAll(template);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<Tuple>();
		}
	}

	@Override
	/*
	 * TODO : Il faut soit créer un callback qui est accessible à distance, soit créer un callback serialisable qui sera exécuté sur le serveur.
	 * Nous n'avons pas le doit de modifier le callback existant, donc il n'est pas possible d'en faire une version sérialisable...
	 * Il faut donc créer un objet local accessible à distance qui encapsule le callback local.
	 */
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		RemoteCallback proxy;
		try {
			proxy = new RemoteCallbackImpl( callback);
			this.server.eventRegister(mode, timing, template, proxy);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		int port = LindaServer.DEFAULT_SERVER_PORT;
		String serverURI = null;
		try {
			if (args.length > 0) {
				serverURI =  args[0];
			} else {
				serverURI = "rmi://" + InetAddress.getLocalHost().getHostName() + ":" + port + "/LindaServer";
			}
			LindaClient me = new LindaClient( serverURI );
			me.debug("Initialisation completed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
