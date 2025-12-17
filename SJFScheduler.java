import java.util.*;

public class SJFScheduler implements Scheduler {

    private List<Process> processes;
    private List<String> executionOrder;
    private int contextSwitch;

    public SJFScheduler(int contextSwitch) {
        this.contextSwitch = contextSwitch;
        this.executionOrder = new ArrayList<>();
    }

    @Override
    public void schedule(List<Process> processes) {
        this.processes = processes;

        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                (a, b) -> {
                    if (a.getRemainingTime() != b.getRemainingTime())
                        return a.getRemainingTime() - b.getRemainingTime();
                    if (a.getArrivalTime() != b.getArrivalTime())
                        return a.getArrivalTime() - b.getArrivalTime();
                    return a.getName().compareTo(b.getName());
                }
        );

        int time = 0;
        int completed = 0;
        int index = 0;
        Process current = null;
        Set<String> started = new HashSet<>();

        while (completed < processes.size()) {

            // add arrived processes
            while (index < processes.size()
                    && processes.get(index).getArrivalTime() <= time) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (current == null) {
                if (readyQueue.isEmpty()) {
                    time = processes.get(index).getArrivalTime();
                    continue;
                } else {
                    current = readyQueue.poll();
                    if (!started.contains(current.getName())) {
                        executionOrder.add(current.getName());
                        started.add(current.getName());
                    }
                }
            }

            int nextArrival = (index < processes.size())
                    ? processes.get(index).getArrivalTime()
                    : Integer.MAX_VALUE;

            int execTime = Math.min(
                    current.getRemainingTime(),
                    nextArrival - time
            );

            if (execTime <= 0) execTime = 1;

            current.setRemainingTime(current.getRemainingTime() - execTime);
            time += execTime;

            // check new arrivals
            while (index < processes.size()
                    && processes.get(index).getArrivalTime() <= time) {
                readyQueue.add(processes.get(index));
                index++;
            }

            // preemption check
            if (!readyQueue.isEmpty()
                    && current.getRemainingTime() > 0
                    && readyQueue.peek().getRemainingTime() < current.getRemainingTime()) {

                readyQueue.add(current);
                current = null;
                time += contextSwitch;
                continue;
            }

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                completed++;
                current = null;
                time += contextSwitch;
            }
        }

        calculateTimes();
    }

    private void calculateTimes() {
        for (Process p : processes) {
            int turnaround = p.getCompletionTime() - p.getArrivalTime();
            int waiting = turnaround - p.getBurstTime();
            p.setTurnaroundTime(turnaround);
            p.setWaitingTime(waiting);
        }
    }

    @Override
    public void printExecutionOrder() {
        System.out.println("Execution Order: " + executionOrder);
    }

    @Override
    public void printWaitingTime() {
        for (Process p : processes) {
            System.out.println(p.getName() + " Waiting Time = " + p.getWaitingTime());
        }
    }

    @Override
    public void printTurnaroundTime() {
        for (Process p : processes) {
            System.out.println(p.getName() + " Turnaround Time = " + p.getTurnaroundTime());
        }
    }

    @Override
    public double getAverageWaitingTime() {
        return processes.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0);
    }

    @Override
    public double getAverageTurnaroundTime() {
        return processes.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0);
    }
}
