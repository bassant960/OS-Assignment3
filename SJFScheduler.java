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

        // sort by arrival to ease adding arrivals
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
        int index = 0; // index over sorted processes for arrivals
        Process current = null;
        Process prev = null; // previously running process (used to determine if context switch is needed)

        while (completed < processes.size()) {

            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (current == null) {
                if (readyQueue.isEmpty()) {
                    // CPU idle -> jump to next arrival time (no context switch while idle)
                    if (index < processes.size()) {
                        time = Math.max(time, processes.get(index).getArrivalTime());
                        continue;
                    } else {
                        break;
                    }
                } else {
                    Process next = readyQueue.poll();

                    // if switching from a previous process to this new one, add context switch
                    if (prev != null && prev != next && contextSwitch > 0) {
                        time += contextSwitch;
                        // add arrivals that happened during the context switch
                        while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                            readyQueue.add(processes.get(index));
                            index++;
                        }
                    }

                    current = next;

                    // record every dispatch (not only first start)
                    executionOrder.add(current.getName());
                }
            }

            current.setRemainingTime(current.getRemainingTime() - 1);
            time += 1;

            while (index < processes.size() && processes.get(index).getArrivalTime() <= time) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (!readyQueue.isEmpty() && current.getRemainingTime() > 0
                    && readyQueue.peek().getRemainingTime() < current.getRemainingTime()) {
                prev = current; // mark this as previous so next selection triggers context switch
                readyQueue.add(current);
                current = null;
                continue;
            }

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                completed++;
                prev = current; 
                current = null;
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
