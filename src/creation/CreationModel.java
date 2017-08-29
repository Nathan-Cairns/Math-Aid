package creation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CreationModel {
	
	protected Map<String, Creation> _creations;
	
	public CreationModel() {
		_creations = new HashMap<String, Creation>();
		
		updateModel();
	}
	
	public abstract void addCreation(Creation creation);
	
	public abstract void addCreation(String creationName);
	
	protected abstract void updateModel();
	
	public File getCreationFile(String creationName) {
		Creation creation = getCreation(creationName);
		
		return creation.file();
	}
	
	public void deleteCreation(String creationName) {
		Creation creationToDelete = getCreation(creationName);
		creationToDelete.delete();
		_creations.remove(creationName);
	}
	
	public List<String> listCreations() {
		List<String> creationList = new ArrayList<String>(_creations.keySet());
		
		return creationList;
	}
	
	public void playCreation(String creationName) { 
		Creation creationToPlay = getCreation(creationName);
		creationToPlay.play();
	}
	
	/**
	 * 
	 * @param creationName
	 * @return
	 */
	protected Creation getCreation(String creationName) {
		if (creationName == null || creationName == "") {
			throw new CreationException("Invalid creation name (" + creationName + ")");
		} else if (!_creations.containsKey(creationName)){ 
			throw new CreationException("Creation (" + creationName + ") does not exist");
		}
		return _creations.get(creationName);
	}
	
	public boolean containsCreation(String creationName) {
		if (creationName != null && !creationName.equals(null) && _creations.containsKey(creationName)) {
			return true;
		}
		return false;
	}
}
