package utils;

public class OptionsResult {
	
	private String result; 
	private String classname;
	private String attributes;
	
	public OptionsResult(String argumentName) {
		result = argumentName;
		attributes = "";
	}
	
	public String getClassname() {
		if(classname == null)
			return "";
		return "\n\t" + classname;
	}
	
	public String getAttributes() {
		return attributes;
	}
	
	public void addClassname(String text){
		classname = text;
		attributes = "";
	}
	
	public void addAttribute(String text){
		attributes += ",\n\t" + text;
	}
	
	public void clear(){
		classname = null;
		attributes = "";
	}
	
	public boolean isFilled(){
		return classname != null;
	}

	@Override
	public String toString() {
		return result + getClassname() + getAttributes();
	}
	
}
