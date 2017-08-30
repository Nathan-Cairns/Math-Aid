package creation;
/**
 * An exception for errors in creation package
 * 
 * @author Nathan Cairns
 *
 */
@SuppressWarnings("serial")
public class CreationException extends RuntimeException {
	public CreationException(String msg) {
		super(msg);
	}
}