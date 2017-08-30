package creation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a CreationModel which contains Creations
 * of type MathsAidBashCreation. This class is used to provide
 * functionality to a GUI.
 * 
 * @author Nathan Cairns
 *
 */
public class MathsAidCreationModel extends CreationModel{
	
	/**
	 * Constructor.
	 */
	public MathsAidCreationModel() {
		super();
	}
	
	/**
	 * Add a creation to the model.
	 * Creates a creation.
	 * Puts the creation in the hashmap.
	 */
	@Override
	public void addCreation(Creation creation) {
		// Create the creation
		creation.create();
		
		// Store creation in the list.
		_creations.put(creation.name(), creation);
		
	}
	
	/**
	 * Add a creation to the model.
	 * Constructs a new creation from the string it is parsed.
	 * Creates the creations and puts the creation in the hashmap.
	 */
	@Override
	public void addCreation(String creationName){
		if (creationName == null || creationName == "") {
			throw new CreationException("Invalid creation name (" + creationName + ")");
		} else if (_creations.containsKey(creationName)) {
			throw new CreationException("Creation (" + creationName + ") already exists");
		}
		
		// Create a new creation with the given title.
		Creation creation = new MathsAidBashCreation(creationName);
		
		addCreation(creation);
	}
	
	/**
	 * Update the model.
	 * Look in the creation folder and add any .mp4s to
	 * the model.
	 */
	@Override
	public void updateModel() {
		MathsAidBashCreation.createCreationFolder();
		File f = MathsAidBashCreation.CREATIONS_FOLDER;
		
		List<String> names = new ArrayList<String>(Arrays.asList(f.list()));
		
		for (String name : names) {
			if (name.substring(name.lastIndexOf('.'), name.length()).equals(MathsAidBashCreation.MP4)) {
				name = name.substring(0, name.lastIndexOf('.'));
				Creation creation = new MathsAidBashCreation(name);
				_creations.put(name, creation);
			}
		}
	}
}
