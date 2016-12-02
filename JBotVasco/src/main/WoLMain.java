package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import evolutionaryrobotics.JBotEvolver;
import simulation.util.Arguments;
import taskexecutor.ConillonTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.tasks.WoLTask;

public class WoLMain {
	private final static String HOSTS_MACS_FILE = "../../SecTools/WoLTools/hostnamesMACs.txt";
	private final int REPEAT = 30;
	private final String arguments = "LS1yb2JvdHMKIAljbGFzc25hbWU9c2ltdWxhdGlvbi5yb2JvdC5BcXVhdGljRHJvbmUsCglydWRkZXI9MSwKCWRpc3RhbmNld2hlZWxzPTAuMiwKCWVuYWJsZVJhbmRvbWl6ZW51bWJlcj0xLAoJbnVtYmVyb2Zyb2JvdHM9MiwKCXJhbmRvbWl6ZW51bWJlcj0oMyw0LDUpLAoJcmFkaXVzPTAuNSwKCWRpYW1ldGVyPTEsCgltYXhzcGVlZD0wLjMsCgljb21tcmFuZ2U9NDAsCglncHNlcnJvcj0xLjgsCgljb21wYXNzb2Zmc2V0PTAsCgljb21wYXNzZXJyb3I9MTAsCgloZWFkaW5nb2Zmc2V0PTAuMDUsCglzcGVlZG9mZnNldD0wLjEsCglhdm9pZGRyb25lcz0wLAoJc2Vuc29ycz0oCgkJQ0lTZW5zb3JXcmFwcGVyXzE9KAoJCQljbGFzc25hbWU9c2ltdWxhdGlvbi5yb2JvdC5DSVNlbnNvcldyYXBwZXIsCgkJCWNpPSgKCQkJCWNsYXNzbmFtZT1jb21tb25pbnRlcmZhY2Uuc2Vuc29ycy5UYXJnZXRDb21ib0NJU2Vuc29yLAoJCQkJaWQ9MSwKCQkJCXJhbmdlPTQwLAoJCQkJZGlzdGFuY2VUb0NvbW11dGU9Mi41LAoJCQkpLAoJCQlpZD0xCgkJKSwKCQlDSVNlbnNvcldyYXBwZXJfMj0oCgkJCWNsYXNzbmFtZT1zaW11bGF0aW9uLnJvYm90LkNJU2Vuc29yV3JhcHBlciwKCQkJY2k9KAoJCQkJY2xhc3NuYW1lPWNvbW1vbmludGVyZmFjZS5zZW5zb3JzLkRyb25lQ0lTZW5zb3IsCgkJCQlpZD0yLAoJCQkJcmFuZ2U9NDAsCgkJCQludW1iZXJzZW5zb3JzPTQKCQkJKSwKCQkJaWQ9MgoJCSkKCSkKCQotLWNvbnRyb2xsZXJzCgluZXR3b3JrPSgKCQljbGFzc25hbWU9Y29tbW9uaW50ZXJmYWNlLm5ldXJhbG5ldHdvcmsuQ0lORUFUTmV0d29yaywKCQlpbnB1dHM9KAoJCQlUYXJnZXRMb2NhdGlvbj0oCgkJCQljbGFzc25hbWU9Y29tbW9uaW50ZXJmYWNlLm5ldXJhbG5ldHdvcmsuaW5wdXRzLkdlbmVyaWNDSU5OSW5wdXQsCgkJCQlsYWJlbD1UYXJnZXRMb2NhdGlvbiwKCQkJCWlkPTEKCQkJKSwKCQkJRHJvbmU9KAoJCQkJY2xhc3NuYW1lPWNvbW1vbmludGVyZmFjZS5uZXVyYWxuZXR3b3JrLmlucHV0cy5HZW5lcmljQ0lOTklucHV0LAoJCQkJbGFiZWw9RHJvbmVTZW5zb3IsCgkJCQlpZD0yCgkJCSkKICAgICAgICApLAoJCW91dHB1dHM9KAoJCQlSdWRkZXI9KAoJCQkJY2xhc3NuYW1lPWNvbW1vbmludGVyZmFjZS5uZXVyYWxuZXR3b3JrLm91dHB1dHMuUnVkZGVyQ0lOTk91dHB1dCwKCQkJCWxhYmVsPVJ1ZGRlciwKCQkJCWZvcndhcmRvbmx5PTEsCgkJCQlib3R0b21MaW1pdD0wLAoJCQkJaWQ9MQoJCQkpCgkJKQoJKQoJCgotLXNpbXVsYXRvciBuZXR3b3JrPShjbGFzc25hbWU9bmV0d29yay5TaW11bGF0aW9uTmV0d29yaykKLS1leGVjdXRvciBjbGFzc25hbWU9dGFza2V4ZWN1dG9yLkNvbmlsbG9uVGFza0V4ZWN1dG9yCi0tZXZvbHV0aW9uIAoJY2xhc3NuYW1lPWV2b2x1dGlvbmFyeXJvYm90aWNzLmV2b2x1dGlvbi5ORUFURXZvbHV0aW9uLAoJdGFzaz0oCgkJY2xhc3NuYW1lPXRhc2tleGVjdXRvci50YXNrcy5UYXJnZXRHZW5lcmF0aW9uYWxUYXNrCgkpLAoJaGFsZmhhbGZGYXVsdHM9MAoKLS1ldmFsdWF0aW9uCiAJY2xhc3NuYW1lPWV2b2x1dGlvbmFyeXJvYm90aWNzLmV2YWx1YXRpb25mdW5jdGlvbnMuS2VlcFBvc2l0aW9uSW5UYXJnZXRFdmFsdWF0aW9uRnVuY3Rpb24sCglzYWZldHlGYWN0b3JFbmFibGU9MCwKCXNhZmV0eURpc3RhbmNlPTMsCglzYWZldHlGYWN0b3JWYWx1ZT0wLjUsCglraWxsT25Db2xsaXNpb249MCwKCQoJZW5lcmd5RmFjdG9yRW5hYmxlPTAsCglvcmllbnRhdGlvbkZhY3RvckVuYWJsZT0xLAoJZGlzdGFuY2VCb290c3RyYXBGYWN0b3JFbmFibGU9MCwKCWluVGFyZ2V0UmFkaXVzPTEKCi0tZW52aXJvbm1lbnQKIAljbGFzc25hbWU9ZW52aXJvbm1lbnQudGFyZ2V0LkZvcm1hdGlvbk11bHRpVGFyZ2V0RW52aXJvbm1lbnQsCiAJCgl3aWR0aD03NSwKCWhlaWdodD03NSwKCXN0ZXBzPTIwMDAsCgoJcmFkaXVzT2ZPYmplY3RQb3NpdGlvbmluZz0xNSwKCW9uZVBlck9uZVJvYm90VGFyZ2V0PTEsCgl2YXJpYXRlVGFyZ2V0c1FudD0wLAoJdGFyZ2V0UmFkaXVzPTEuNSwKCXNhZmV0eVJhbmRvbVBvc2l0aW9uRGlzdGFuY2U9My41LAoJZmF1bHREdXJhdGlvbj01MDAsCglpbmplY3RGYXVsdHM9MCwKCglmb3JtYXRpb25TaGFwZT1yYW5kb20sCglsaW5lRm9ybWF0aW9uX3hEZWx0YT02LjAsCglhcnJvd0Zvcm1hdGlvbl94RGVsdGE9Ni4wLAoJYXJyb3dGb3JtYXRpb25feURlbHRhPTYuMCwKCWNpcmNsZUZvcm1hdGlvbl9yYWRpdXM9MTAsCgl2YXJpYXRlRm9ybWF0aW9uUGFyYW1ldGVycz0wLAoKCW1vdmVUYXJnZXQ9MSwKCXRhcmdldE1vdmVtZW50VmVsb2NpdHk9MC4yLAoJdmFyaWF0ZVRhcmdldHNTcGVlZD0xLAoJdGFyZ2V0TW92ZW1lbnRBemltdXRoPTAsCgl2YXJpYXRlVGFyZ2V0c0F6aW11dGg9MSwKCQoJcm90YXRlRm9ybWF0aW9uPTEsCglyb3RhdGlvblZlbG9jaXR5PTAuMDE1LAoJcm90YXRpb25EaXJlY3Rpb249MCwKCXZhcmlhdGVSb3RhdGlvblZlbG9jaXR5PTEsCgl2YXJpYXRlUm90YXRpb25EaXJlY3Rpb249MQoJCi0tdXBkYXRhYmxlcyAKCWN1cnJlbnRzPSgKCQljbGFzc25hbWU9dXBkYXRhYmxlcy5XYXRlckN1cnJlbnQsCgkJbWF4c3BlZWQ9MC4xCgkpCgkJCi0tcG9wdWxhdGlvbgogCQljbGFzc25hbWU9ZXZvbHV0aW9uYXJ5cm9ib3RpY3MucG9wdWxhdGlvbnMuTkVBVFBvcHVsYXRpb24sCgkJc2FtcGxlcz0xLAoJCWdlbmVyYXRpb25zPTQwMCwKCQlzaXplPTE1MAoJCQotLW91dHB1dCBTb2NpYWxfc2VuZGluZwo=";

	public static void main(String[] args) {
		new WoLMain();
	}

	public WoLMain() {
		// Get MAC addresses
		Map<String, String> macAddresses = getMacAddresses();

		// Initialize task executor
		JBotEvolver jBotEvolver;
		try {
			String[] args = Arguments.readOptionsFromString(new String(Base64.getDecoder().decode(arguments)));
			jBotEvolver = new JBotEvolver(args);

			TaskExecutor taskExecutor = new ConillonTaskExecutor(jBotEvolver,
					new Arguments("server=evolve.dcti.iscte.pt"));
			// TaskExecutor taskExecutor = new
			// SequentialTaskExecutor(jBotEvolver, null);
			taskExecutor.start();
			taskExecutor.setTotalNumberOfTasks(macAddresses.keySet().size());

			for (int i = 0; i < REPEAT; i++) {
				ArrayList<String> hwAddresses = new ArrayList<String>();
				hwAddresses
						.addAll(Arrays.asList(macAddresses.values().toArray(new String[macAddresses.values().size()])));

				taskExecutor.addTask(new WoLTask(hwAddresses));
				System.out.print(".");
				taskExecutor.setDescription("Social sending " + macAddresses.size() * (i + 1) + " out of "
						+ (macAddresses.size() * REPEAT));
			}
			System.out.println();

			for (int i = 0; i < REPEAT; i++) {
				taskExecutor.getResult();
				System.out.println("!");
			}

			if (taskExecutor != null) {
				taskExecutor.stopTasks();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> getMacAddresses() {
		FileReader hostnamesFileReader = null;
		BufferedReader hostamesBReader = null;
		HashMap<String, String> macAddresses = new HashMap<String, String>();

		try {
			hostnamesFileReader = new FileReader(new File(HOSTS_MACS_FILE));
			hostamesBReader = new BufferedReader(hostnamesFileReader);

			String line = hostamesBReader.readLine();

			// Discard CSV titles line
			if (line != null) {
				line = hostamesBReader.readLine();
			}

			int count = 0;
			while (line != null) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					String[] elements = line.split(",");

					if (elements.length == 2) {
						macAddresses.put(elements[0], elements[1]);
						count++;
					}
				}

				line = hostamesBReader.readLine();
			}

			System.out.printf("[%s] Loaded %d hostnames and hostnames%n", getClass().getSimpleName(), count);
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
		} catch (IOException e) {
			System.err.printf("[%s] Error reading from file! %s%n", getClass().getSimpleName(), e.getMessage());
		} finally {
			if (hostnamesFileReader != null) {
				try {
					hostnamesFileReader.close();
				} catch (IOException e) {
				}
			}

			if (hostamesBReader != null) {
				try {
					hostamesBReader.close();
				} catch (IOException e) {
				}
			}
		}

		return macAddresses;
	}
}
