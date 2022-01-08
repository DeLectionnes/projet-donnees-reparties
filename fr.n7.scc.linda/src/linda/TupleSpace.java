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

/**
 * @author cpantel
 * This class allows to store tuples in files.
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
	 * @throws IOException 
	 * 
	 */
	/**
	 * @param name: File name which contains a serialized tuple space.
	 * @throws IOException: An issue occurred during the reading of the file.
	 */
	public TupleSpace(String name) throws IOException {
		this.load(name);
	}
	
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
	
	public void store(String name) throws IOException {
		FileOutputStream file = new FileOutputStream( new File(name) );
		ObjectOutputStream stream = new ObjectOutputStream( file );
		stream.writeObject(this.tuples);
	}
	
	public Tuple readOnce(Tuple template) {
    	Tuple t_read = null;
		for(Tuple tuple : this.tuples) {
			if (tuple.matches(template)) {
				t_read = tuple.deepclone();
				break;
			}
		}
		return t_read;
	}
	
	public Collection<Tuple> readMany(Tuple template) {
    	Collection<Tuple> t_read = new ArrayList<Tuple>();
		for(Tuple tuple : this.tuples) {
			if (tuple.matches(template)) {
				t_read.add(tuple.deepclone());
				break;
			}
		}
		return t_read;
	}
	
	public Tuple takeOnce(Tuple template) {
		Tuple t_take = null;
		for(Tuple tuple : this.tuples) {
			if(tuple.matches(template)) {
				// TODO : est il utile de cloner s'il s'agit d'une prise ?
				t_take = tuple.deepclone();
				boolean b = this.tuples.remove(tuple);
				break;
			}
		}
		return t_take;
	}
	
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
	
	public void writeOnce(Tuple tuple) {
		this.tuples.add(tuple);
	}

}
