import java.util.*;
import java.io.*;

class Oblig2 {

	public static void main(String[] args) {
		Control control = new Control(args);
	}
} // end class Oblig2

class Control {

	GraphAnalyzer ga;
	FileReader fr;
	int workforce;

	// Constructor for class Control
	Control(String[] args) {
		argumentTester(args);
	} // end constructor Control

	void argumentTester(String[] args) {

		if (args.length == 0) {
			fr = new FileReader(specifyFilename());
			workforce = specifyWorkforce();
		} else if (args.length == 1) {
			if (testInput(args[0]) == true) {
				fr = new FileReader(specifyFilename());
				workforce = Integer.parseInt(args[0]);
			} else {
				fr = new FileReader(args[0]);
				workforce = specifyWorkforce();
			}
		} else if (args.length == 2) {
			if (testInput(args[1]) == true) {
				fr = new FileReader(args[0]);
				workforce = Integer.parseInt(args[1]);
			} else {
				fr = new FileReader(args[0]);
				workforce = specifyWorkforce();
			}
		} else {
			terminateProgram("Only two arguments (filename / workforce) is allowed.");
		} // end if/else

		ga = new GraphAnalyzer(fr.getTasks(), workforce);
	} // end method argumentTester

	boolean testInput(String args) {
		try {
			Integer.parseInt(args);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	} // end method testInput

	String specifyFilename() {

		Scanner scanner = new Scanner(System.in);
		System.out.print("\nPlease specify filename: ");
		String filename = scanner.nextLine();
		return filename;
	} // end method specifyFilename

	int specifyWorkforce() {

		Scanner scanner = new Scanner(System.in);
		System.out.print("\nPlease speficy workforce: ");
		int workforce = scanner.nextInt();
		return workforce;
	} // end method specifyWorkforce

	void terminateProgram(String exitCode) {

		System.out.println("\nTERMINATED: " + exitCode + "\n");
		System.exit(0);
	} // end method terminateProgram
} // end class Control

class GraphAnalyzer {

	ArrayList<Task> tasks;
	int workforce;
	ArrayList<Task> sortedTasks;
	ArrayList<Task> startPoints = new ArrayList<Task>();
	ArrayList<Task> endPoints = new ArrayList<Task>();

	GraphAnalyzer(ArrayList<Task> tasks, int workforce) {
		this.tasks = tasks;
		this.workforce = workforce;
		addEdges();
		test();
	} // end constructor GraphAnalyzer

	void test() {

		boolean cycle = false;

		if (topologicalSort() == false) {
			setEST();
			setEFT();
			setLST();
			findCritical();
			printWorkPlan();
			//printSortedTasks();
		} else {
			findFirstTask();
			System.out.println("\n-------------------------------------------------------------------------------");
			System.out.println("Cycle found in the following tasks:");
			foundCycle();
			System.out.println("-------------------------------------------------------------------------------\n");
		} // end if/else
	} // end method test

	void addEdges() {

		// Temp pekere
		Task tmp;
		Task test;
		Task temp;
		int[] a;

		for (int i = 0; i < tasks.size(); i++) {
			tmp = tasks.get(i);
			a = tmp.getPredecessors();
		
			for (int j = 0; j < a.length; j++) {

				if (a[j] != 0) {
					temp = tasks.get(a[j]-1);
					Edge out = new Edge(temp, tmp);
					temp.addOutEdge(out);

					for (int k = 0; k < tasks.size(); k++) {
						test = tasks.get(k);
						if (test.getTaskNumber() == a[j]) {
							// HER LEGGER VI TIL TEST SOM EN EDGE TIL TMP
							Edge in = new Edge(test, tmp);
							tmp.addInEdge(in);
						} // end if
					} // end for
				} // end if
			} // end for
		} // end for
	} // end method addEdges

	void printTasksAndEdges() {

		Task tmp;
		Edge in;
		Edge out;

		System.out.println("\n-------------------------------------------------------------------------------");
		for (int i = 0; i < tasks.size(); i++) {
			tmp = tasks.get(i);
			System.out.print("\nProcessing task " + tmp.getTaskNumber() + " '" + tmp.getTaskName() + "'.");
			System.out.println("This task has " + tmp.currentPredecessors + " predecessor(s).");
			//System.out.println(" Visited = " + tmp.getVisited());
			for (int j = 0; j < tmp.inEdges.size(); j++) {
				in = tmp.inEdges.get(j);
				System.out.print("INEDGE: - ");
				in.printEdgeTasks();
			} // end for

			for (int k = 0; k < tmp.outEdges.size(); k++) {
				out = tmp.outEdges.get(k);
				System.out.print("OUTEDGE - ");
				out.printEdgeTasks();
			} // end for
		} // end for
		System.out.println("\n-------------------------------------------------------------------------------\n");
	} // end method printTasksAndEdges

	void printSortedTasks() {
		
		System.out.println("\n-------------------------------------------------------------------------------");
		System.out.println("Tasks can be performed in the following order:");
		Task tmp;
		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);
			if (tmp.getTaskName() != null) {
				System.out.println(i+1 + ". " + tmp.getTaskName() + " (Task nr. " + tmp.getTaskNumber() + ")");
			} // end if
		} // end for
		System.out.println("-------------------------------------------------------------------------------\n");
	} // end method printSortedTasks

	void findCritical() {

		Task tmp;
		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);

			if (tmp.lst > tmp.est) {
				tmp.slack = tmp.lst - tmp.est; 
			} else {
				tmp.critical = true;
			}
		}
	}

	// This is the algorithm from lecture05.pdf, page 23.
	boolean topologicalSort() {

		Stack<Task> stack = new Stack<Task>();
		sortedTasks = new ArrayList<Task>();
		Task t;
		int counter = 0;
		boolean cycle = false;

		for (int i = 0; i < tasks.size(); i++) {
			t = tasks.get(i);

			// Find root
			if (t.inEdges.size() == 0) {
				// Root found - push to stack
				stack.push(t);
			} // end if

			while (!stack.isEmpty()) {
				t = stack.pop();

				// Insert task t to sorted tasks list
				sortedTasks.add(t);
				counter++;

				Task tmp;
				for (int j = 0; j < t.outEdges.size(); j++) {
					tmp = t.outEdges.get(j).to;
					tmp.currentPredecessors--;
					if (tmp.currentPredecessors == 0) {
						stack.push(tmp);
					} // end if
				} // end for

				if (counter < tasks.size()) {
					cycle = true;
				} else {
					cycle = false;
				} // end if/else
			} // end while
		} // end for
		return cycle;
	} // end method topologicalSort

	// Find cycle - returns true if found
	boolean foundCycle() {

		for (Task t : startPoints) {
			if (findCycle(t, null)) {
				return true;
			} // end if
		} // end for
		return false;
	} // end method foundCycle

	// Checks and sets visited on task
	boolean findCycle(Task t, Task prev) {
		if (t.cycleNum == 0) {
			t.cycleNum = 1;

			Task v;
			for (int i = 0; i < t.outEdges.size(); i++) {
				v = t.outEdges.get(i).to;
				if (findCycle(v, t)) {
					System.out.println(v.getTaskNumber() + ". " + v.getTaskName());
					return true;
				} // end if
			} // end for
		} else if (t.cycleNum == 1 && t.outEdges.size() != 0) {
			return true;
		} // end if/else

		t.cycleNum = 2;
		return false;
	} // end method findCycle

	boolean findFirstTask() {
		for (Task t : tasks) {
			if (t.inEdges.size() == 0) {
				startPoints.add(t);
			} else if (t.outEdges.size() == 0) {
				endPoints.add(t);
			} // end if/else
		} // end for

		if (startPoints.size() != 0) {
			return true;
		} // end for
		return false;
	} // end method findFirstTask

	void setEST() {
		// Ha en verdi som hele tiden holder på maks completion time

		Task tmp;
		Task previous;
		int est = 0;
		int temp = 0;
		boolean update = false;

		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);

			// Fint first task
			if (tmp.inEdges.size() == 0) {
				//System.out.println("Task " + tmp.taskNumber + ". " + tmp.taskName);
				tmp.est = est;
				//est = tmp.completionTime;
			} else {
				//System.out.println("Task " + tmp.taskNumber + ". " + tmp.taskName);
				// For every other task
				// Iterate over inedges
				for (int j = 0; j < tmp.inEdges.size(); j++) {
					// If this task has inedges that point to previous task
					previous = tmp.inEdges.get(j).from;
					//System.out.println("-> " + previous.taskNumber + " - TTC " + previous.completionTime);

					if (temp < previous.completionTime) {
						temp = previous.completionTime;
					}

					if (previous.taskNumber == sortedTasks.get(i-1).taskNumber) {
						// Oppdater EST
						update = true;
					}


				}
				if (update) {
					est+=temp;
				}
				  //System.out.println(temp);
				  //System.out.println("EST IS: " + est);
				update = false;
				temp = 0;
			}

			// For each task, print info
			tmp.est = est;
			//System.out.println(" EST " + est);
			//System.out.println("Task " + tmp.taskNumber + ". " + tmp.taskName + " CT=" + tmp.completionTime + " / EST=" + tmp.est);
		}
	}

	void setEFT() {

		Task tmp;
		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);
			tmp.eft = tmp.est + tmp.completionTime;
		}
	}

	void setLST() {

		Task tmp;
		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);

			if (i == 0) {
				// This is the start task, LST = 0
				tmp.lst = 0;
			} else if (i == sortedTasks.size()-1) {
				// This is the end task, LST = EST
				tmp.lst = tmp.est;
			} else {
				// All other tasks
				// Current task LST = next task EST - current task completion time
				//tmp.lst = sortedTasks.get(i+1).completionTime - tmp.completionTime;
				for (int j = 0; j < sortedTasks.size(); j++) {
					if (sortedTasks.get(j).est > tmp.est) {
						tmp.lst = sortedTasks.get(j).est - tmp.completionTime;
						break;
					}
				}
			}
		}
	}

	void printWorkPlan() {
		
		int time = 0;
		int currentTime = 0;
		Task tmp;
		int latest = 0;
		
		//System.out.format("%32s%10d%16s", string1, int1, string2);
		//System.out.format("%4s%2d%n", "Time", currentTime);
		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.format("%-2s%-5s%-2s%-12s%-2s%-35s%-2s%-9s%2s%4s%2s%4s%2s%9s%2s %n", "|", "Time", "|", "Task Number", "|", "Task Name", "|", "Task Time", "|", "LST", "|", "EFT", "|", "Critical", "|");
		System.out.println("----------------------------------------------------------------------------------------------");

		for (int i = 0; i < sortedTasks.size(); i++) {
			tmp = sortedTasks.get(i);
			currentTime = tmp.est;

			// Her finner vi alle forskjellige tider
			if (time == 0) {
				//System.out.println("----------------------------------------------------------------------------------------------");
				time = currentTime;

			}

			if (currentTime > time) {
				//System.out.println("----------------------------------------------------------------------------------------------");
			}

			if (currentTime >= time) {
				System.out.format("%-2s%4d", "|", currentTime);
				time = currentTime;
			}
			//System.out.println("- Starting task nr. " + tmp.taskNumber + ". " + tmp.taskName);
			System.out.format("%2s%1s%11d%1s%-2s%-34s%2s%10d%2s%4d%2s%4d%2s%9s%2s %n", "|", " ", tmp.taskNumber, " ", "|", tmp.taskName, "|", tmp.completionTime, "|", tmp.lst, "|", tmp.eft, "|", tmp.getCritical(), "|");
			latest = tmp.completionTime;
		}
		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.format("%-2s%86s%4d%2s %n", "|", "Total Project Runtime:", currentTime+latest, "|");
		System.out.println("----------------------------------------------------------------------------------------------");
	}

} // end class GraphAnalyzer

class FileReader {

	String filename;
	ArrayList<Task> tasks;

	// Constructor for class FileReader
	FileReader(String filename) {
		this.filename = filename;
		File file = new File(filename);
		fileReader(file);
	} // end contructor FileReader

	/**
	* Reads from file
	* Creates new objects of class Task
	* Inserts new objects of class Task to SKRIV HVILKEN DATASTRUKTUR SOM VELGES HER 
	* @param f the file to read from
	*/
	void fileReader(File f) {
		
		int lineCounter = 0;
		int numberOfTasks;
		int taskNumber;
		String taskName;
		int completionTime;
		int manpowerRequired;
		int[] taskSequence = new int[4];
		tasks = new ArrayList<Task>();

		try {
			Scanner fileScanner = new Scanner(f);
			numberOfTasks = fileScanner.nextInt();

			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine();
				Scanner lineScanner = new Scanner(line);

				while (lineScanner.hasNext()) {
					taskNumber = Integer.parseInt(lineScanner.next());
					taskName = lineScanner.next();
					completionTime = Integer.parseInt(lineScanner.next());
					manpowerRequired = Integer.parseInt(lineScanner.next());
					int counter = 0;

					while (lineScanner.hasNext() && counter < taskSequence.length) {
						taskSequence[counter] = Integer.parseInt(lineScanner.next());
						counter++;
					} // end while

					
					Task task = new Task(taskNumber, taskName, completionTime, manpowerRequired, taskSequence);
					tasks.add(task);
				} // end while
			} // end while
		} catch (FileNotFoundException e) {
			fileError(f.getName());
		} // end try/catch
	} // end method fileReader

	public ArrayList<Task> getTasks() {

		return this.tasks;
	} // end method getTasks

	/**
	* Printing error message if either file does not exist or filename is wrong
	* Terminates the program
	* @param filename the filename specified by the user
	*/
	void fileError(String filename) {

		System.out.println("\nFILE ERROR: The file (" + filename.toUpperCase() + ") does not exist in the directory, or the filename is incorrect!\n");
		System.exit(0);
	} // end method fileError
} // end class FileReader

class Task {

	int taskNumber;
	String taskName;
	int completionTime;
	int manpowerRequired;
	int[] taskSequence;
	int est;
	int eft;
	int lst;
	int slack;
	boolean critical = false;
	boolean visited;
	public int currentPredecessors = 0;
	public int cycleNum = 0;
	ArrayList<Edge> inEdges = new ArrayList<Edge>();
	ArrayList<Edge> outEdges = new ArrayList<Edge>();

	// Constructor for class Task
	Task(int i, String s, int j, int k, int[] l) {
		this.taskNumber = i;
		this.taskName = s;
		this.completionTime = j;
		this.manpowerRequired = k;
		int tmp = l.length;
		taskSequence = new int[tmp];
		System.arraycopy(l, 0, taskSequence, 0, l.length);
		setPredecessors();
	} // end constructor Task

	void setPredecessors() {
		for (int i = 0; i < taskSequence.length; i++) {
			if (taskSequence[i] != 0) {
				currentPredecessors++;
			}
		}
	} // end method setPredecessors

	public int getTaskNumber() {

		return taskNumber;
	} // end method getTaskNumber

	public String getTaskName() {

		return taskName;
	} // end method getTaskName

	public int getCompletionTime() {

		return completionTime;
	}

	public int getManpowerRequired() {

		return manpowerRequired;
	}

	public int getEST() {

		return est;
	}

	public int[] getPredecessors() {

		return taskSequence;
	} // end method getPredecessors

	public boolean getVisited() {

		return visited;
	} // end method getVisited

	public char getCritical() {
		if (!critical) {
			return ' ';
		} return '*';
	}

	public void addInEdge(Edge e) {

		inEdges.add(e);
		//System.out.println("ADDED INEDGE: Inbound on: " + this.getTaskNumber() + " from " + e.getFromTasks().getTaskNumber());
	} // end method addInEdge

	public void addOutEdge(Edge e) {

		outEdges.add(e);
		//System.out.println("ADDED OUTEDGE: Outbound from: " + this.getTaskNumber() + " to " + e.getToTask().getTaskNumber());
	} // end method addOutEdge
}

class Edge {
	Task from;
	Task to;

	Edge(Task p, Task n) {
		this.from = p;
		this.to = n;
	} // end constructor Edge 

	public Task getFromTasks() {

		return from;
	} // end method getFromTasks

	public Task getToTask() {

		return to;
	} // end method getToTask

	public void printEdgeTasks() {
		System.out.println("Edge from " + from.getTaskName() + " to " + to.getTaskName());
	} // end method printEdgeTasks
}
