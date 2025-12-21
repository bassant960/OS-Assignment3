import java.util.*;

public class AGScheduler implements Scheduler {

    private List<Process> readyQueue = new ArrayList<>();
    private List<String> executionOrder = new ArrayList<>();
    private List<Process> processesRef;
    private int time = 0;

    @Override
    public void schedule(List<Process> processes) {

        this.processesRef = processes;
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        int finished = 0;
        int index = 0;

        // initial quantum history
        for (Process p : processes) {
            p.getQuantumHistory().add(p.getQuantum());
        }

        while (finished < processes.size()) {

            while (index < processes.size()
                    && processes.get(index).getArrivalTime() <= time) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }

            Process current = readyQueue.remove(0);
            executionOrder.add(current.getName());

            int q = current.getQuantum();
            int q1 = (int) Math.ceil(0.25 * q);
            int q2 = (int) Math.ceil(0.25 * q);

            int executed = 0;
            boolean preempted = false;

            // ===== Phase 1 (FCFS) =====
            while (executed < q1 && current.getRemainingTime() > 0) {
                tick(current);
                executed++;
                index = updateIndex(processes, index);
            }

            if (current.getRemainingTime() == 0) {
                finish(current);
                finished++;
                continue;
            }

            // ===== Phase 2 (Priority) =====
            Process bestPriority = getBestPriority();

            while (executed < q1 + q2 && current.getRemainingTime() > 0) {

                if (bestPriority != null &&
                        bestPriority.getPriority() < current.getPriority()) {

                    updateQuantum(current, executed, 2);
                    readyQueue.add(current);
                    readyQueue.remove(bestPriority);
                    readyQueue.add(0, bestPriority);
                    preempted = true;
                    break;
                }

                tick(current);
                executed++;
                index = updateIndex(processes, index);
            }

            if (preempted) continue;

            if (current.getRemainingTime() == 0) {
                finish(current);
                finished++;
                continue;
            }

            // ===== Phase 3 (SJF) =====
            Process shortest = getShortestJob();

            while (executed < q && current.getRemainingTime() > 0) {

                if (shortest != null &&
                        shortest.getRemainingTime() < current.getRemainingTime()) {

                    updateQuantum(current, executed, 3);
                    readyQueue.add(current);
                    readyQueue.remove(shortest);
                    readyQueue.add(0, shortest);
                    preempted = true;
                    break;
                }

                tick(current);
                executed++;
                index = updateIndex(processes, index);
            }

            if (preempted) continue;

            if (current.getRemainingTime() == 0) {
                finish(current);
                finished++;
            } else {
                updateQuantum(current, executed, 1);
                readyQueue.add(current);
            }
        }

        // WT & TAT
        for (Process p : processes) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(tat - p.getBurstTime());
        }
    }

    // ================= Helpers =================

    private void tick(Process p) {
        if (p.getRemainingTime() == 1) {
            p.setCompletionTime(time + 1);
        }
        p.setRemainingTime(p.getRemainingTime() - 1);
        time++;
    }

    private int updateIndex(List<Process> processes, int index) {
        while (index < processes.size()
                && processes.get(index).getArrivalTime() <= time) {
            readyQueue.add(processes.get(index));
            index++;
        }
        return index;
    }

    private Process getBestPriority() {
        Process best = null;
        for (Process p : readyQueue) {
            if (best == null || p.getPriority() < best.getPriority())
                best = p;
        }
        return best;
    }

    private Process getShortestJob() {
        Process best = null;
        for (Process p : readyQueue) {
            if (best == null || p.getRemainingTime() < best.getRemainingTime())
                best = p;
        }
        return best;
    }

    private void updateQuantum(Process p, int executed, int scenario) {
        int remaining = p.getQuantum() - executed;
        int newQ;

        switch (scenario) {
            case 1 -> newQ = p.getQuantum() + 2;
            case 2 -> newQ = p.getQuantum() + (int) Math.ceil(remaining / 2.0);
            case 3 -> newQ = p.getQuantum() + remaining;
            case 4 -> newQ = 0;
            default -> newQ = p.getQuantum();
        }

        p.setQuantum(newQ);
        p.getQuantumHistory().add(newQ);
    }

    private void finish(Process p) {
        p.setQuantum(0);
        p.getQuantumHistory().add(0);
    }

    // ================= Output =================

    @Override
    public void printExecutionOrder() {
        System.out.println(String.join(" -> ", executionOrder));
    }

    @Override
    public void printWaitingTime() {
        for (Process p : processesRef)
            System.out.println(p.getName() + " Waiting Time = " + p.getWaitingTime());
    }

    @Override
    public void printTurnaroundTime() {
        for (Process p : processesRef)
            System.out.println(p.getName() + " Turnaround Time = " + p.getTurnaroundTime());
    }

    @Override
    public double getAverageWaitingTime() {
        return processesRef.stream().mapToInt(Process::getWaitingTime).average().orElse(0);
    }

    @Override
    public double getAverageTurnaroundTime() {
        return processesRef.stream().mapToInt(Process::getTurnaroundTime).average().orElse(0);
    }

    public void printQuantumHistory() {
    System.out.println("\nQuantum History:");
    for (Process p : processesRef) {
        System.out.println(
            p.getName() + " = " + p.getQuantumHistory()
        );
    }
}

}
