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
import linda.ExtendedLinda;
import linda.Linda;
import linda.Tuple;

/** 
 * @author : bgros, cpantel, rmonvill
 * Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient implements ExtendedLinda {
	
	/**
	 * 
	 */
	private RemoteLinda server;
	
	/**
	 * 
	 */
	protected long startTime;
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) {
    	this.startTime = System.nanoTime();
    	try {
    		this.debug( "Connecting to remote Linda server at URL: " + serverURI);
    		Remote proxy = Naming.lookup(serverURI);
			/*
			 * for (Class<?> c : proxy.getClass().getInterfaces()) { this.debug("Proxy : " +
			 * c.getName()); }
			 */
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
//			this.debug("Entering write: " + tuple);
			this.server.write(tuple);
//	    	this.debug("Exiting write:" + tuple);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Tuple take(Tuple template) {
		try {
//			this.debug("Entering take: " + template);
			Tuple t_taken = this.server.take(template);
//			this.debug("Exiting take: " + template + " -> " + t_taken);
			return t_taken;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple read(Tuple template) {
		try {
//			this.debug("Entering take: " + template);
			Tuple t_read = this.server.read(template);
//			this.debug("Exiting read:" + template + " -> " + t_read);
			return t_read;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryTake(Tuple template) {
		try {
//			this.debug("Entering tryTake: " + template);
			Tuple t_taken = this.server.tryTake(template);
//			this.debug("Exiting tryTake: " + template + " -> " + t_taken);
			return t_taken;
		} catch (RemoteException e) {
			// As this is an asynchronous take, we decided to catch and hide the exception
			// and to return null as the take failed.
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Tuple tryRead(Tuple template) {
		try {
//			this.debug("Entering tryRead: " + template);
			Tuple t_read = this.server.tryRead(template);
//			this.debug("Exiting tryRead: " + template + " -> " + t_read);
			return t_read;
		} catch (RemoteException e) {
			// As this is an asynchronous read, we decided to catch and hide the exception
			// and to return null as the read failed.
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Collection<Tuple> takeAll(Tuple template) {
		try {
//			this.debug("Entering takeAll: " + template);
			Collection<Tuple> t_taken = this.server.takeAll(template);
//			this.debug("Exiting takeAll: " + template + " -> " + t_taken);
			return t_taken;
		} catch (RemoteException e) {
			// As this is an asynchronous take, we decided to catch and hide the exception
			// and to return an empty collection as the take failed.
			e.printStackTrace();
			return new ArrayList<Tuple>();
		}
	}

	@Override
	public Collection<Tuple> readAll(Tuple template) {
		try {
//			this.debug("Entering readAll: " + template);
			Collection<Tuple> t_read = this.server.readAll(template);
//			this.debug("Exiting readAll: " + template + " -> " + t_read);
			return t_read;			
		} catch (RemoteException e) {
			// As this is an asynchronous read, we decided to catch and hide the exception
			// and to return an empty collection as the read failed.
			e.printStackTrace();
			return new ArrayList<Tuple>();
		}
	}

	@Override
	/*
	 * As the client is connected to a remote server, we must allow the remote server to access the
	 * local callback. We decided to create an RMI object that wraps the local callback and thus provide
	 * a remote access to the callback.
	 */
	public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
		RemoteCallback proxy;
		try {
//			this.debug("Registering a " + mode + " " + timing + " callback on " + template);
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
		System.err.println(this.getThreadId() + " " + message);
	}
	
    /**
     * @return
     */
    public long getElapsedTime() {
    	return (System.nanoTime() - this.startTime);
    }
    
	/**
	 *
	 */
	@Override
	public void stop() {
		this.debug("Stopping");
	}
    
    // TO BE COMPLETED
	/**
	 * @param args: URI for the remote LindaServer
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
