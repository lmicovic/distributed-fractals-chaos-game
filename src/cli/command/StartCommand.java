package cli.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import app.AppConfig;
import app.Job;
import app.Point;
import app.ScheduleType;
import app.util.JobSchedule;


// to do: finish jobScheduling: AppConfig.lamportMutex.acquireLock();
//	      finish finishJobScheduling: AppConfig.lamportMutex.releaseMyCriticalSection();

public class StartCommand implements CLICommand {

	@Override
	public String commandName() {
		return "start";
	}
	
	// 1) args - naziv posla
	// Ako nismo prosledili argumente onda pitamo na konzoli da se unesu argumenti za novi posao
	@Override
	public void execute(String args) {
		
		try {
			
			int nodeCounts = AppConfig.chordState.getAllNodeIdInfoMap().size();
			int activeJobsCount = AppConfig.chordState.getActiveJobsCount();
			
			if(nodeCounts < 1 || nodeCounts < activeJobsCount + 1) {
				AppConfig.timestampedErrorPrint("Not enough servents to execute job: " + args);
				return;
			}
			
			Job job = null;
			// Ako smo prosledili argumente uzimamo ucitani job
			if(args != null) {
				
				Job tmpJob = new Job(args, -1, -1, -1, -1, null);
				if(AppConfig.chordState.getActiveJobsList().contains(tmpJob)) {
					AppConfig.timestampedErrorPrint("Job with name: " + tmpJob.getName() + " is active.");
					return;
				}
				
				job = AppConfig.myServentInfo.getJobByName(args);
				
				
				
				// Ne postoji job sa tim nazivom
				if(job == null) {
					AppConfig.timestampedErrorPrint("Job with name: " + args + " is not found in list of jobs.");
					return;
				}
			}

			// Ako nismo prosledili argumente onda pitamo na konzoli korisnika da unese podatke za novi posao
			else if(args == null) {
				job = jobInput();
			}
			//------------------------------------------------------------------------------------------------------
			
			
			//------------------------------------------------------------------------------------------------------			
			// Job Scheduling
			//------------------------------------------------------------------------------------------------------

			AppConfig.chordState.addNewJob(job);
			JobSchedule.scheduleJob(nodeCounts, ScheduleType.ADD_JOB);
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
	// Ucitava podatke za novi Job sa komande konzole
	public Job jobInput() {
		
		try {
			
			String jobName = "";
			int pointCount = -1;					// broj tacaka
			double proportion = -1.0;			// canvas width
			int width = -1;				// canvas height
			int height = -1;
			List<Point> points = new ArrayList<Point>();
			
			Scanner sc = new Scanner(System.in);
			
			// Job name
			while(true) {
				
				System.out.println("Enter job name: ");
				jobName = sc.nextLine();
				Job tmpJob = new Job(jobName, pointCount, width, height, proportion, points);
				if(AppConfig.chordState.getActiveJobsList().contains(tmpJob)) {
					System.err.println("Job with name " + jobName + " is already active.\n");
					continue;
				}
				else if(AppConfig.myServentInfo.getJobs().contains(tmpJob)) {
					System.err.println("Job with name " + jobName + " already exist.\n");
					continue;
				}
				else {
					break;
				}
				
			}
			
			// pointCount
			while(true) {
				try {
					System.out.println("Enter number of points: ");
					pointCount = Integer.parseInt(sc.nextLine());
					if(pointCount < 3 || pointCount > 10) {
						System.err.println("Number of points should be number between 3 and 10");
						continue;
					}
					break;
				} catch (NumberFormatException e) {
					System.out.println("Value should be a number");
				}
			}
			
			// Width
			while(true) {
				try {
					System.out.println("Enter canvas width: ");
					width = Integer.parseInt(sc.nextLine());
					break;
				} catch (NumberFormatException e) {
					System.out.println("Value should be a number");
				}
			}
			
			// Height
			while(true) {
				try {
					System.out.println("Enter canvas height: ");
					height = Integer.parseInt(sc.nextLine());
					break;
				} catch (NumberFormatException e) {
					System.out.println("Value should be a number");

				}
			}
			
			// Proportion
			while(true) {
				
				try {
					System.out.println("Enter proportion: ");
					proportion = Double.parseDouble(sc.nextLine());
					if(proportion < 0.0 || proportion > 1.0) {
						System.err.println("Proportion should be number between 0.0 and 1.0");
						continue;
					}
					break;
				} catch (NumberFormatException e) {
					System.out.println("Value should be decimal");
					continue;
				}
				
			}
			
			// Points - format: (x1,y1) (x2,x2) (x3,y3)
			while(true) {
				
				System.out.println("Enter cordinates for points: ");
				String[] cordinates = sc.nextLine().split(" ");
				if(cordinates.length != pointCount) {
					System.out.println("You must have " + pointCount + " cordinates");
					continue;
				}
				
				boolean check = true;
				for(int i = 0; i < cordinates.length; i++) {
					
					try {
						
						int x = Integer.parseInt(cordinates[i].split(",")[0].substring(1));
						int y = Integer.parseInt(cordinates[i].split(",")[1].substring(0, cordinates[i].split(",")[1].length()-1));
						
						Point point = new Point(x, y);
						points.add(point);
						
					} catch (NumberFormatException e) {
						check = false;
						points.clear();
						System.out.println("Wrong cordinate input: (x,y)");
						break;
					}
					
				}
				if(!check ) {
					continue;
				}
				
				break;
			}
			
			Job newJob = new Job(jobName, pointCount, width, height, proportion, points);
			
			System.out.println("Created new Job: " + newJob);
			
			return newJob;
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		return null;
		
	}
	
	
}
