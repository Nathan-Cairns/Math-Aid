package creation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathsAidCreationModel extends CreationModel{
	
	public MathsAidCreationModel() {
		super();
	}

	@Override
	public void addCreation(Creation creation) {
		creation.create();
		
		// Store creation in the list.
		_creations.put(creation.name(), creation);
		
	}
	
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
