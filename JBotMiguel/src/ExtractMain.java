import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;
import evolutionaryrobotics.JBotEvolver;

public class ExtractMain {
	
	public static void main(String[] a) {
		
		int samples = 100;
		int fitnesssamples = 4;
		
		String[] newArgs = new String[] {"bigdisk/tro_journal/room_maze_sensor/solve_maze_arbitrator/2/_showbest_current.conf","--controllers","+printvalues=1"};
		
		try {
			JBotEvolver jBotEvolver = new JBotEvolver(newArgs);
			TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, new Arguments("classname=SequentialTaskExecutor",true));
			taskExecutor.start();
		
			jBotEvolver = new JBotEvolver(newArgs);

			for(int fsample = 0 ; fsample < fitnesssamples ; fsample++) {
				for(int sample = 0 ; sample < samples ; sample++) {
					JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
					SingleSamplePostEvaluationTask t = new SingleSamplePostEvaluationTask(0,newJBot,fsample,newJBot.getPopulation().getBestChromosome(),sample,0);
					taskExecutor.addTask(t);
				}
			}
			
			for(int fsample = 0 ; fsample < fitnesssamples ; fsample++) {
				for(int sample = 0 ; sample < samples ; sample++) {
					taskExecutor.getResult();
				}
			}
			taskExecutor.stopTasks();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
		System.exit(0);
	}
}