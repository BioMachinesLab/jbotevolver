package utils;

import java.io.File;

/**
 * This class will traverse a tree of folders and allow you to
 * setup a filter so that you can execute actions on particular folders.
 * This can be useful for tasks that require iterating over a large set
 * of results and performing some type of computing (exporting data,
 * running extra evaluations, etc).
 * 
 * @author miguel
 */
public abstract class TraverseFolders {
	
	protected String baseFolder;
	protected String[] setups = new String[]{};
	
	/**
	 * 
	 * @param baseFolder The starting point for the traversal (relative to the working directory)
	 * @param setups A set sub-folders inside the baseFolder that should be traversed (can be empty if all sub-folders are to be traversed) 
	 */
	public TraverseFolders(String baseFolder, String[] setups) {
		this.baseFolder = baseFolder;
		this.setups = setups;
	}
	
	public TraverseFolders(String baseFolder) {
		this.baseFolder = baseFolder;
	}
	
	/**
	 * The main method, which should be called in order to start the directory traversal
	 */
	public void traverse() {
		
		traverseStarted();
		
		if(setups.length == 0) {
			//if the user did not specify any sub-folders
			traverse(new File(baseFolder));
		}else {
			//if the user specified specific sub-folders to traverse
			for(String setup : setups)
				traverse(new File(baseFolder+setup));
		}
		
		traverseEnded();
	}
	
	/**
	 * Recursive method to traverse the directory tree. For each non-empty directory,
	 * the actFilter method is called to see if the directory qualifies for an action.
	 * @param currentFolder
	 */
	private void traverse(File currentFolder) {

		//check if the folder qualifies for an action
		if(actFilter(currentFolder))
			act(currentFolder);
		
		for(String subFolderName : currentFolder.list()) {
			
			File target = new File(currentFolder.getPath()+"/"+subFolderName);
			
			//ignore regular files and empty folders
			if(target.isDirectory() && target.list() != null) {
				
				//recursive call
				traverse(target);
			}
		}
	}
	
	/**
	 * Filters interesting and non-interesting folders. Should be implemented
	 * by the user based on the path, name or contents of the folder
	 * @param folder
	 * @return
	 */
	protected abstract boolean actFilter(File folder);
	
	/**
	 * Acts on filtered folders. Should be implemented by the user
	 * based on the type of computation that is necessary
	 * @param folder
	 */
	protected abstract void act(File folder);

	/**
	 * Optional overridable function signaling the start of the traversal. Useful
	 * for setting up result files, printing header information, etc
	 */
	protected void traverseStarted(){}
	
	/**
	 * Optional overridable function signaling the end of the traversal. Useful
	 * for closing result files, printing summary information, etc
	 */
	protected void traverseEnded(){}
	
}
