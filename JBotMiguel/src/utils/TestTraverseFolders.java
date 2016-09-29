package utils;

import java.io.File;

public class TestTraverseFolders extends TraverseFolders{
	
	public TestTraverseFolders(String baseFolder, String[] setups) {
		super(baseFolder, setups);
	}
	
	protected void act(File folder) {
		System.out.println(folder.getPath());
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		for(String s : folder.list())
			if(s.equals("_showbest_current.conf"))
				return true;
		
		return false;
	}
}