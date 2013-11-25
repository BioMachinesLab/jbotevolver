import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;
import evolutionaryrobotics.JBotEvolver;

public class ExtractMain {
	
	public static void main(String[] a) {
		
		int samples = 100;
		
		String[] newArgs = new String[] {"bigdisk/newconillon/room_maze_sensor/corridor_primitive/2/_showbest_current.conf","--controllers","+printvalues=1"};
		
		try {
			JBotEvolver jBotEvolver = new JBotEvolver(newArgs);
			TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, new Arguments("classname=SequentialTaskExecutor",true));
			taskExecutor.prepareArguments(jBotEvolver.getArguments());
			taskExecutor.start();
		
			jBotEvolver = new JBotEvolver(newArgs);
			taskExecutor.prepareArguments(jBotEvolver.getArguments());

			for(int sample = 0 ; sample < samples ; sample++) {
				JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
				SingleSamplePostEvaluationTask t = new SingleSamplePostEvaluationTask(0,newJBot,0,newJBot.getPopulation().getBestChromosome(),sample,0);
				taskExecutor.addTask(t);
			}
			
			for(int sample = 0 ; sample < samples ; sample++) {
				taskExecutor.getResult();
			}
			taskExecutor.stopTasks();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
		System.exit(0);
	}
}