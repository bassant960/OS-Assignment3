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

        while (completed < n) {
            Process candidate = selectBestProcess(currentTime);

            if (candidate == null) {
                currentTime++;
                continue;
            }

            if (currentProcess != candidate) {
                // Pre-switch: Load candidate into execution order
                executionOrder.add(candidate.getName());

                // Context Switch Loop
                for (int i = 0; i < contextSwitch; i++) {
                    currentTime++;
                    applyAging(currentTime, null);
                }

                // Re-check if candidate is still the best after switch duration
                Process postSwitchBest = selectBestProcess(currentTime);
                if (postSwitchBest != candidate) {
                    // If a better process arrived during CS, restart loop to pick it
                    currentProcess = null;
                    continue;
                }
                currentProcess = candidate;
            }

            // Execute one time unit
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
            currentTime++;
            applyAging(currentTime, currentProcess);

            if (currentProcess.getRemainingTime() <= 0) {
                currentProcess.setFinished(true);
                currentProcess.setCompletionTime(currentTime);
                currentProcess.setTurnaroundTime(currentProcess.getCompletionTime() - currentProcess.getArrivalTime());
                currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getBurstTime());
                completed++;
                currentProcess = null;
            } else {
                // Preemption check for next iteration
                Process nextBest = selectBestProcess(currentTime);
                if (nextBest != currentProcess) {
                    currentProcess = null;
                }
            }
        }
    }

    private Process selectBestProcess(int currentTime) {
        Process best = null;
        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            if (p.getArrivalTime() <= currentTime && !p.isFinished()) {
                if (best == null || isHigherPriority(p, best, i)) {
                    best = p;
                }
            }
        }
        return best;
    }

    private boolean isHigherPriority(Process p, Process best, int pIndex) {
        if (p.getPriority() != best.getPriority()) {
            return p.getPriority() < best.getPriority();
        }
        if (p.getArrivalTime() != best.getArrivalTime()) {
            return p.getArrivalTime() < best.getArrivalTime();
        }
        return pIndex < processes.indexOf(best);
    }

    private void applyAging(int currentTime, Process runningProcess) {
        if (agingInterval <= 0) return;
        for (Process p : processes) {
            if (p.getArrivalTime() <= currentTime && !p.isFinished() && p != runningProcess) {
                p.setWaitCounter(p.getWaitCounter() + 1);
                if (p.getWaitCounter() >= agingInterval) {
                    p.setPriority(Math.max(1, p.getPriority() - 1));
                    p.setWaitCounter(0);
                }
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
