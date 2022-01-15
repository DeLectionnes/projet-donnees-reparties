/**
 * 
 */
package linda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cpantel
 * 
 *
 */
public class TupleSpace {

	private List<Tuple> tuples;

	/**
	 * 
	 */
	public TupleSpace() {
		this.tuples = new ArrayList<Tuple>();
	}
	
	/**
	 * This class allows to load and store tuples in files using serialization. The purpose is to build tests.
	 * This constructor initialize a tuple space with date contained in a file.
	 * @param name: File name which contains a serialized tuple space.
	 * @throws IOException: An issue occurred during the reading of the file.
	 */
	public TupleSpace(String name) throws IOException {
		this.load(name);
	}
	
	/**
	 * This class allows to load and store tuples in files using serialization. The purpose is to build tests.
	 * @param name: Name of the file containing the tuples that must be loaded.
	 * @throws IOException
	 */
	public void load(String name) throws IOException {
		try {
			FileInputStream file = new FileInputStream( new File(name) );
			ObjectInputStream stream = new ObjectInputStream( file );
			Object object = stream.readObject();
			if (object instanceof List<?>) {
				this.tuples = (List<Tuple>) object;
			} 
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param name
	 * @throws IOException
	 */
	public void store(String name) throws IOException {
		FileOutputStream file = new FileOutputStream( new File(name) );
		ObjectOutputStream stream = new ObjectOutputStream( file );
		stream.writeObject(this.tuples);
	}
	
	/**
	 * Read a single tuple matching the template
	 * @param template: Pattern expressing the shape of the read tuple
	 * @return Either the read tuple if a tuple matching the template is present in the tuple space or null
	 */
	public Tuple readOnce(Tuple template) {
    	Tuple t_read = null;
    	Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if (tuple.matches(template)) {
				t_read = tuple.deepclone();
			}
		}
		return t_read;
	}
	
	/**
	 * Read a single tuple matching the template. This operation is cancelled if the AtomicBoolean becomes true.
	 * @param template: Pattern expressing the shape of the read tuple
	 * @param cancel: Use of the Test and Set solution with an AtomicBoolean to stop the operation is others Thread succeeded.
	 * @return Either the read tuple if a tuple matching the template is present in the tuple space or null if it is not present
	 * or if the operation was cancelled.
	 */
	public Tuple readOnce(Tuple template, AtomicBoolean cancel) {
    	Tuple t_read = null;
    	Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext() && (! cancel.get())) {
			Tuple tuple = iterator.next();
			if (tuple.matches(template)) {
				if (! cancel.getAndSet(true)) {
					t_read = tuple.deepclone();
				}
			}
		}
		return t_read;
	}
	
	/**
	 * @param template
	 * @return
	 */
	public Collection<Tuple> readMany(Tuple template) {
    	Collection<Tuple> t_read = new ArrayList<Tuple>();
    	Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if (tuple.matches(template)) {
				t_read.add(tuple.deepclone());
			}
		}
		return t_read;
	}
	
	/**
	 * Take a single tuple matching the template
	 * @param template: Pattern expressing the shape of the taken tuple
	 * @return Either the taken tuple if a tuple matching the template is present in the tuple space or null
	 */
	public Tuple takeOnce(Tuple template) {
		Tuple t_take = null;
    	Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if (tuple.matches(template)) {
				// TODO : est il utile de cloner s'il s'agit d'une prise ?
				t_take = tuple.deepclone();
				iterator.remove();
			}
		}
		return t_take;
	}
	
	/**
	 * Take a single tuple matching the template. This operation is cancelled if the AtomicBoolean becomes true.
	 * The operation removes the tuple from the tuple space.
	 * @param template: Pattern expressing the shape of the taken tuple
	 * @param cancel: Use of the Test and Set solution with an AtomicBoolean to stop the operation is others Thread succeeded.
	 * @return Either the taken tuple if a tuple matching the template is present in the tuple space or null if it is not present
	 * or if the operation was cancelled.
	 */
	public Tuple takeOnce(Tuple template, AtomicBoolean cancel) {
		Tuple t_take = null;
    	Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext() && (! cancel.get())) {
			Tuple tuple = iterator.next();
			if (tuple.matches(template)) {
				if (! cancel.getAndSet(true)) {
					// TODO : est il utile de cloner s'il s'agit d'une prise ?
					t_take = tuple.deepclone();
					iterator.remove();
				}
			}
		}
		return t_take;
	}
	
	/**
	 * @param template
	 * @return
	 */
	public Collection<Tuple> takeMany(Tuple template) {
		Collection<Tuple> t_take = new ArrayList<Tuple>();
		Iterator<Tuple> iterator = this.tuples.iterator();
		while (iterator.hasNext()) {
			Tuple tuple = iterator.next();
			if(tuple.matches(template)) {
				// TODO : est il utile de cloner s'il s'agit d'une prise ?
				t_take.add(tuple.deepclone());
				iterator.remove();
			}
		}
		return t_take;
	}
	
	/**
	 * @param tuple
	 */
	public void writeOnce(Tuple tuple) {
		this.tuples.add(tuple);
	}

}
