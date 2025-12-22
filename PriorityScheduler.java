import java.util.*;

public class PriorityScheduler implements Scheduler {
    private final int contextSwitch;
    private final int agingInterval;
    private List<String> executionOrder = new ArrayList<>();
    private List<Process> processes;

    public PriorityScheduler(int contextSwitch, int agingInterval) {
        this.contextSwitch = contextSwitch;
        this.agingInterval = agingInterval;
    }

    @Override
    public void schedule(List<Process> processes) {
        this.processes = processes;
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();
        Process currentProcess = null;
        int lastAgingTime = 0;

        while (completed < n) {
            if (currentTime > 0 && currentTime - lastAgingTime >= agingInterval) {
                for (Process p : processes) {
                    if (p.getArrivalTime() <= currentTime && !p.isFinished() && p != currentProcess) {
                        if (p.getPriority() > 1) {
                            p.setPriority(p.getPriority() - 1);
                        }
                    }
                }
                lastAgingTime = currentTime;
            }

            Process bestProcess = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !p.isFinished()) {
                    if (bestProcess == null || p.getPriority() < bestProcess.getPriority() ||
                            (p.getPriority() == bestProcess.getPriority() && p.getArrivalTime() < bestProcess.getArrivalTime())) {
                        bestProcess = p;
                    }
                }
            }

            if (bestProcess == null) {
                currentTime++;
                continue;
            }

            if (currentProcess != bestProcess) {
                currentProcess = bestProcess;
                currentTime += contextSwitch;
                continue;
            }

            if (executionOrder.isEmpty() || !executionOrder.get(executionOrder.size() - 1).equals(currentProcess.getName())) {
                executionOrder.add(currentProcess.getName());
            }

            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
            currentTime++;

            Process nextProcess = null;
            for (Process p : processes) {
                if (p.getArrivalTime() <= currentTime && !p.isFinished()) {
                    if (nextProcess == null || p.getPriority() < nextProcess.getPriority() ||
                            (p.getPriority() == nextProcess.getPriority() && p.getArrivalTime() < nextProcess.getArrivalTime())) {
                        nextProcess = p;
                    }
                }
            }
            if (nextProcess != currentProcess) {
                currentProcess = null;
            }

            if (currentProcess.isFinished()) {
                currentProcess.setCompletionTime(currentTime);
                currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                completed++;
                currentProcess = null;
            }
        }
    }

    @Override
    public void printExecutionOrder() {
        System.out.println("Execution Order: " + executionOrder);
    }

    @Override
    public void printWaitingTime() {
        System.out.println("\nProcess Results:");
        for (Process p : processes) {
            System.out.println(p.getName() + ": WaitingTime = " + p.getWaitingTime()
                    + ", TurnaroundTime = " + p.getTurnaroundTime());
        }
        System.out.printf("Average Waiting Time: %.1f\n", getAverageWaitingTime());
        System.out.printf("Average Turnaround Time: %.1f\n", getAverageTurnaroundTime());
    }

    @Override
    public void printTurnaroundTime() {}

    @Override
    public double getAverageWaitingTime() {
        return processes.stream().mapToInt(Process::getWaitingTime).average().orElse(0.0);
    }

    @Override
    public double getAverageTurnaroundTime() {
        return processes.stream().mapToInt(Process::getTurnaroundTime).average().orElse(0.0);
    }
}
