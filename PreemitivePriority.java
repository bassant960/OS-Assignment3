import java.util.*;

public class PriorityPreemptive implements Scheduler {

    private List<Process> processes;
    private List<String> executionOrder;
    private List<Integer> executionTime;
    private double avgWaitingTime;
    private double avgTurnaroundTime;

    public PriorityPreemptive() {
        executionOrder = new ArrayList<>();
        executionTime = new ArrayList<>();
    }

    @Override
    public void schedule(List<Process> inputProcesses) {

        processes = new ArrayList<>(inputProcesses);

        for (Process p : processes) {
            p.remainingTime = p.burstTime;
            p.startTime = -1;
            p.age = 0;
        }

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt((Process p) -> p.priority)
                        .thenComparingInt(p -> p.arrivalTime)
                        .thenComparingInt(p -> p.pid)
        );

        int currentTime = 0;
        int completed = 0;
        int index = 0;
        Process running = null;
        int lastPid = -1;

        while (completed < processes.size()) {

            // Add arrived processes
            while (index < processes.size() &&
                    processes.get(index).arrivalTime == currentTime) {
                readyQueue.add(processes.get(index));
                index++;
            }

            // Aging
            applyAging(readyQueue);

            // Preemption check
            if (running != null && !readyQueue.isEmpty()) {
                Process top = readyQueue.peek();
                if (top.priority < running.priority) {
                    readyQueue.add(running);
                    running = readyQueue.poll();
                }
            }

            if (running == null && !readyQueue.isEmpty()) {
                running = readyQueue.poll();
                if (running.startTime == -1)
                    running.startTime = currentTime;
            }

            if (running != null) {

                if (lastPid != running.pid) {
                    executionOrder.add("P" + running.pid);
                    executionTime.add(currentTime);
                    lastPid = running.pid;
                }

                running.remainingTime--;

                if (running.remainingTime == 0) {
                    running.completionTime = currentTime + 1;
                    running.turnaroundTime =
                            running.completionTime - running.arrivalTime;
                    running.waitingTime =
                            running.turnaroundTime - running.burstTime;
                    completed++;
                    running = null;
                }

            } else {
                if (executionOrder.isEmpty() ||
                        !executionOrder.get(executionOrder.size() - 1).equals("IDLE")) {
                    executionOrder.add("IDLE");
                    executionTime.add(currentTime);
                }
            }

            currentTime++;
        }

        executionTime.add(currentTime);
        calculateAverages();
    }

    private void applyAging(PriorityQueue<Process> readyQueue) {
        List<Process> temp = new ArrayList<>();

        for (Process p : readyQueue) {
            p.age++;
            if (p.age >= 5 && p.priority > 0) {
                p.priority--;
                p.age = 0;
            }
            temp.add(p);
        }

        readyQueue.clear();
        readyQueue.addAll(temp);
    }

    private void calculateAverages() {
        double w = 0, t = 0;
        for (Process p : processes) {
            w += p.waitingTime;
            t += p.turnaroundTime;
        }
        avgWaitingTime = w / processes.size();
        avgTurnaroundTime = t / processes.size();
    }

    @Override
    public double getAverageWaitingTime() {
        return avgWaitingTime;
    }

    @Override
    public double getAverageTurnaroundTime() {
        return avgTurnaroundTime;
    }

    @Override
    public void printExecutionOrder() {
        System.out.println("\nExecution Order:");
        for (int i = 0; i < executionOrder.size(); i++) {
            int start = executionTime.get(i);
            int end = executionTime.get(i + 1);
            System.out.println(start + " -> " + end + " : " + executionOrder.get(i));
        }
    }
}
