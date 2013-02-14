package tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class teste {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String arg = "--robots "
				+ "    classname=Epuck,"
				+ "    sensors=("
				+ "		EpuckIRSensor=(classname=EpuckIRSensor,id=1,angle=90,numberofsensors=4,offsetnoise=0.05,fixedsensor=1)"
				+ "	 ),"
				+ "	 actuators=(TwoWheelActuator=(name=TwoWheelActuator,id=1,maxspeed=0.1)"
				+ "	 ),";
		Pattern p = Pattern.compile("classname=(\\w[\\.\\w]*)");
		Matcher m = p.matcher(arg);
		while (m.find()) {

			String className = m.group().split("=")[1];
			arg = arg.replaceAll(className, "AAA." + className);

		}
		System.out.println(arg);
	}

}
