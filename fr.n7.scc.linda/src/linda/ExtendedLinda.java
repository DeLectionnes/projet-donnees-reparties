/**
 * 
 */
package linda;

/**
 * @author bgros, cpantel, rmonvill
 * Adding some services required for stopping concurrent engine and measuring performances.
 */
public interface ExtendedLinda extends Linda {
	
	public long getElapsedTime();
	
	public void stop();

}
