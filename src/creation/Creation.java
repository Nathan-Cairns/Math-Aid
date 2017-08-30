package creation;

import java.io.File;
/**
 * Interface to represent a type which can have various file operations executed on it.
 * 
 * @author Nathan Cairns
 *
 */
public interface Creation {
	/**
	 * Creates the file
	 */
	public void create();
	
	/**
	 * Plays the file
	 */
	public void play();
	
	/**
	 * Deletes the file
	 */
	public void delete();
	
	/**
	 * Returns the name of the file
	 * 
	 * @return String : creation name
	 */
	public String name();
	
	/**
	 * Returns the File
	 * 
	 * @return File : the file being referenced.
	 */
	public File file();
}
