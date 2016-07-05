package utils;

import java.io.File;

public abstract class TraverseFolders {
	
	protected String baseFolder;
	protected String[] setups;
	
	public TraverseFolders(String baseFolder, String[] setups) {
		this.baseFolder = baseFolder;
		this.setups = setups;
	}
	
	public void traverse() {
		for(String setup : setups) {
			traverse(new File(baseFolder+setup));
		}
		
		if(setups.length == 0)
			traverse(new File(baseFolder));
		
	}
	
	private void traverse(File currentFolder) {
		
		for(String subFolder : currentFolder.list()) {
			
			File target = new File(currentFolder.getPath()+subFolder);
			
			//ignore empty folders
			if(target.isDirectory() && target.list() != null) {
				act(target);
				traverse(target);
			}
		}
	}
	
	protected abstract void act(File folder);

}
