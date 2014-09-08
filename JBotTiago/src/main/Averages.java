package main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Scanner;

import net.jafama.FastMath;

public class Averages {
	
	public static void main(String[] args) {
		
		try {

			LinkedList<String> runs = new LinkedList<String>();
			
			String dir ="/home/tiagor/Documents/workspace/JBotTiago/bigdisk/paper_hugearena/";
			
			File f = new File(dir);
			for(File ff : f.listFiles()) {
				if(ff.isDirectory())
					runs.add(ff.getName());
			}
			
			for(String run : runs) {
	
				Scanner s = new Scanner(new File(dir+run+"/1/_generationnumber"));
				
				f = new File(dir+run);
				
				int number = 0;
				
				for(File ff : f.listFiles())
					if(ff.isDirectory())
						number++;
				
				int generations = s.nextInt()+1;
				
				boolean average = true;
				
				for(int z = 0 ; z < 2 ; z++) {
					
					average = !average;
				
					double values[] = new double[generations];
					double realVals[][] = new double[generations][number];
					
					double maxValue = 0;
					
					for(int i = 1 ; i <= number ; i++ ) {
	
						s = new Scanner(new File(dir+run+"/"+i+"/_fitness.log"));
		
						int gen = 0;
		
						while(s.hasNextLine()) {
							String line = s.nextLine();
		//					System.out.println(line);
		
							Scanner lineScanner = new Scanner(line);
		
							if(!line.contains("#")) {
								if(gen == Integer.parseInt(lineScanner.next())){
									double val = Double.parseDouble(lineScanner.next().trim());
//									System.out.println(run + i +" "+gen+" "+val);
									if(average) {
										values[gen]+= val;
										realVals[gen][i-1] = val;
									}else{
										values[gen] = Math.max(val,values[gen]);
										maxValue = Math.max(maxValue,values[gen]);
									}
									gen++;
								}
							}
						}
					}
					
					PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("plots/"+(average? "a" : "b") + "_"+run+".txt")));
					
					String ss="";
					
					double deviation[] = new double[generations];
					
					for(int i = 0 ; i < values.length ; i++) {
						if(average) {
							values[i]/=number;
							
							for (int j = 0; j < realVals[i].length; j++) {
								deviation[i] += FastMath.powQuick((realVals[i][j]-values[i]),2);
							}
							deviation[i] /= number;
							deviation[i] = FastMath.sqrtQuick(deviation[i]);
							
						}
						if(average)
							ss+=i+" "+values[i]+" "+(values[i] + deviation[i])+" "+(values[i] - deviation[i])+"\n";
						else
							ss+=i+" "+values[i]+"\n";
					}
					out.println(ss);
					out.close();
					
					if(!average)
						createPlotFile(run,generations,maxValue);
				}
			}
			createPlotAllFile(runs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done.");
	}
	
	static void createPlotAllFile(LinkedList<String> strings) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("plots/plot_all.sh")));
			
			out.println("#!/bin/bash");
			for(String s : strings) {
				out.println("./plot_"+s+".sh");
			}
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void createPlotFile(String name, double x, double y) {
		String template = "";
		try {
			Scanner s = new Scanner(new File("template_deviation.sh"));
			
			while(s.hasNextLine()) {
				template+=s.nextLine()+"\n";
			}
			template = template.replaceAll("_NAME_", name);
			template = template.replaceAll("_MAXX_", ""+(int)Math.ceil(x));
			template = template.replaceAll("_MAXY_", ""+(int)Math.ceil(y));
			template = template.replaceAll("_MAX_", "b_"+name+".txt");
			template = template.replaceAll("_AVG_", "a_"+name+".txt");
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("plots/plot_"+name+".sh")));
			out.println(template);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
