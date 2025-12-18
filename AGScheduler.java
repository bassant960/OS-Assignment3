import java.util.*;

public class AGScheduler implements Scheduler {

    private Queue<Process> readyQueue = new LinkedList<>();
    private List<String> executionOrder = new ArrayList<>();
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    private int currentTime = 0;
    private int contextSwitch;

    public AGScheduler(int contextSwitch) {
        this.contextSwitch = contextSwitch;
    }

    @Override
    public void schedule(List<Process> processes) {

        // sort by arrival time
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        List<Process> allProcesses = new ArrayList<>(processes);

        while (!readyQueue.isEmpty() || !processes.isEmpty()) {

            addArrivedProcesses(processes);

            if (readyQueue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = readyQueue.poll();
            executionOrder.add(p.getName());

            quantumHistory.putIfAbsent(
                    p.getName(),
                    new ArrayList<>(List.of(p.getQuantum()))
            );

            int originalQuantum = p.getQuantum();
            int q1 = (int) Math.ceil(originalQuantum * 0.25);
            int q2 = (int) Math.ceil(originalQuantum * 0.25);

            //FCFS
            runForTime(p, q1);
            if (p.isFinished()) {
                finishProcess(p);
                continue;
            }

            //Non-Preemptive Priority
            boolean preempted = false;
            for (int i = 0; i < q2; i++) {
                addArrivedProcesses(processes);

                if (existsHigherPriority(p)) {
                    int remaining = q2 - i;
                    int inc = (int) Math.ceil(remaining / 2.0);
                    p.setQuantum(p.getQuantum() + inc);     // scenario ii
                    readyQueue.add(p);
                    quantumHistory.get(p.getName()).add(p.getQuantum());
                    preempted = true;
                    break;
                }

                runOneUnit(p);
                if (p.isFinished()) break;
            }

            if (p.isFinished()) {
                finishProcess(p);
                continue;
            }
            if (preempted) {
                currentTime += contextSwitch;
                continue;
            }

            //Preemptive SJF
            int used = q1 + q2;
            int remainingQuantum = originalQuantum - used;

            for (int i = 0; i < remainingQuantum; i++) {
                addArrivedProcesses(processes);

                if (existsShorterJob(p)) {
                    int rem = remainingQuantum - i;
                    p.setQuantum(p.getQuantum() + rem);     // scenario iii
                    readyQueue.add(p);
                    quantumHistory.get(p.getName()).add(p.getQuantum());
                    preempted = true;
                    break;
                }

                runOneUnit(p);
                if (p.isFinished()) break;
            }

            if (p.isFinished()) {
                p.setQuantum(0);                            // scenario iv
                finishProcess(p);
                quantumHistory.get(p.getName()).add(0);
            } else if (!preempted) {
                p.setQuantum(p.getQuantum() + 2);           // scenario i
                readyQueue.add(p);
                quantumHistory.get(p.getName()).add(p.getQuantum());
            }

            currentTime += contextSwitch;
        }

        // calculate waiting & turnaround
        for (Process p : allProcesses) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(tat - p.getBurstTime());
        }
    }

    //helper functions
    private void addArrivedProcesses(List<Process> processes) {
        while (!processes.isEmpty() &&
                processes.get(0).getArrivalTime() <= currentTime) {
            readyQueue.add(processes.remove(0));
        }
    }

    private void runForTime(Process p, int time) {
        for (int i = 0; i < time; i++) {
            runOneUnit(p);
            if (p.isFinished()) break;
        }
    }

    private void runOneUnit(Process p) {
        p.setRemainingTime(p.getRemainingTime() - 1);
        currentTime++;
    }

    private boolean existsHigherPriority(Process current) {
        for (Process p : readyQueue) {
            if (p.getPriority() < current.getPriority()) {
                return true;
            }
        }
        return false;
    }

    private boolean existsShorterJob(Process current) {
        for (Process p : readyQueue) {
            if (p.getRemainingTime() < current.getRemainingTime()) {
                return true;
            }
        }
        return false;
    }

    private void finishProcess(Process p) {
        p.setCompletionTime(currentTime);
    }

    //Output
    @Override
    public void printExecutionOrder() {
        System.out.println("Execution Order:");
        System.out.println(String.join(" -> ", executionOrder));
    }

    @Override
    public void printWaitingTime() {
        System.out.println("\nWaiting Time:");
    }

    @Override
    public void printTurnaroundTime() {
        System.out.println("\nTurnaround Time:");
    }

    @Override
    public double getAverageWaitingTime() {
        return 0;
    }

    @Override
    public double getAverageTurnaroundTime() {
        return 0;
    }

    public void printQuantumHistory() {
        System.out.println("\nQuantum History:");
        for (String p : quantumHistory.keySet()) {
            System.out.println(p + " : " + quantumHistory.get(p));
        }
    }
}
