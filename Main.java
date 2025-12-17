/*
* Take care that u need to download external json library to can use json inside IntelliJ
* 1) Open this link:  https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/
* 2) Download: gson-2.10.1.jar
* 3) open IntelliJ
* 4) open from above: File → Project Structure
* 5) Modules
* 6) Dependencies
* 7) Click + sign
* 8) Select JARs or Directories
* 9) choose gson-2.10.1.jar u just download
* 10) Enter OK
* 11) Congrats Now the Main file will work with u without problems ^^
* */
import com.google.gson.*;
import java.io.FileReader;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String jsonFile = "test_2.json";        // Dear team please change this according to the name of the JSON file u use while testing ur class.
        if (args.length > 0) jsonFile = args[0];

        try {
            JsonObject root = JsonParser.parseReader(new FileReader(jsonFile)).getAsJsonObject();
            JsonObject input = root.getAsJsonObject("input");

            int contextSwitch = input.has("contextSwitch") ? input.get("contextSwitch").getAsInt() : 0;
            int rrQuantum = input.has("rrQuantum") ? input.get("rrQuantum").getAsInt() : 0;
            int agingInterval = input.has("agingInterval") ? input.get("agingInterval").getAsInt() : 0;

            // Read original processes from JSON
            List<Process> originalProcesses = new ArrayList<>();
            JsonArray procs = input.getAsJsonArray("processes");
            for (JsonElement e : procs) {
                JsonObject p = e.getAsJsonObject();
                String name = p.get("name").getAsString();
                int arrival = p.get("arrival").getAsInt();
                int burst = p.get("burst").getAsInt();
                int priority = p.has("priority") ? p.get("priority").getAsInt() : 0;
                int quantum = p.has("quantum") ? p.get("quantum").getAsInt() : 0;

                originalProcesses.add(new Process(name, arrival, burst, priority, quantum));
            }

            // Scheduler types we want to run on the same testcase
            String[] schedulerTypes = {"SJF", "RR", "PRIORITY", "AG"};

            for (String schedulerType : schedulerTypes) {
                // Make a fresh deep copy of processes for each scheduler run
                List<Process> processesCopy = deepCopyProcesses(originalProcesses);

                Scheduler scheduler = createScheduler(
                        schedulerType,
                        contextSwitch,
                        rrQuantum,
                        agingInterval
                );

                System.out.println("==============================================");
                System.out.println("Running Scheduler: " + schedulerType);
                System.out.println("==============================================");

                if (scheduler == null) {
                    System.out.println(">> " + schedulerType + " not implemented yet. (See TODO in createScheduler)");
                    System.out.println();
                    continue;
                }

                // Run scheduling
                scheduler.schedule(processesCopy);

                // Print results
                scheduler.printExecutionOrder();
                System.out.println();
                scheduler.printWaitingTime();
                System.out.println();
                scheduler.printTurnaroundTime();
                System.out.println();
                System.out.printf("Average Waiting Time = %.2f%n", scheduler.getAverageWaitingTime());
                System.out.printf("Average Turnaround Time = %.2f%n", scheduler.getAverageTurnaroundTime());
                System.out.println();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Deep copy processes because Process objects are mutated by schedulers.
     */
    private static List<Process> deepCopyProcesses(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process p : original) {
            // Use the constructor: Process(String name, int arrivalTime, int burstTime, int priority, int quantum)
            Process q = new Process(p.getName(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(), p.getQuantum());
            copy.add(q);
        }
        return copy;
    }

    /**
     * Factory to create schedulers.
     * TODO: When you implement RRScheduler, PriorityScheduler, AGScheduler add cases below.
     *
     * Notes / suggestions:
     * - SJFScheduler has constructor: new SJFScheduler(contextSwitch)
     * - RRScheduler suggestion: new RRScheduler(contextSwitch, rrQuantum)
     * - PriorityScheduler suggestion: new PriorityScheduler(contextSwitch, agingInterval)
     * - AGScheduler suggestion: new AGScheduler(contextSwitch) or pass extra params as needed
     */
    private static Scheduler createScheduler(
            String schedulerType,
            int contextSwitch,
            int rrQuantum,
            int agingInterval
    ) {
        switch (schedulerType.toUpperCase()) {
            case "SJF":
                return new SJFScheduler(contextSwitch);

            case "RR":
                // TODO: Implement RRScheduler (implements Scheduler) and uncomment below. DONE
                return new RRScheduler(contextSwitch, rrQuantum);
               // return null;

            case "PRIORITY":
                // TODO: Implement PriorityScheduler (implements Scheduler) and uncomment below.
                // return new PriorityScheduler(contextSwitch, agingInterval);
                return null;

            case "AG":
                // TODO: Implement AGScheduler (implements Scheduler) and uncomment below.
                // AG may need extra params (e.g. defaultQuantum). Read them from JSON and pass here.
                // return new AGScheduler(contextSwitch);
                return null;

            default:
                return null;
        }
    }
}
