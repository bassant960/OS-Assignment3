import java.util.*;


public class RRScheduler implements Scheduler {

    private List<Process> processes;
    private List<String> executionOrder; // to visualize the order
    private int contextSwitch;
    private int quantum;

    public RRScheduler(int contextSwitch, int rrQuantum) {
        this.contextSwitch = contextSwitch;
        this.quantum = rrQuantum;
        this.executionOrder = new ArrayList<>();
    }

    @Override
    public void schedule(List<Process> processes) {

        this.processes = processes;

        // Sort by arrival time
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int index = 0;
        int completed = 0;
        int n = processes.size();

        readyQueue.add(processes.get(index)); //now index=0
        currentTime = processes.get(index).getArrivalTime();// to prevent -ve waiting time
        index++;

        while (!readyQueue.isEmpty()) {
            Process current= readyQueue.poll();
            executionOrder.add(current.getName());

            int executionTime = Math.min(current.getRemainingTime(),quantum); // as if the process end before hte quantum
            current.setRemainingTime(current.getRemainingTime()-executionTime);
            currentTime += executionTime;
                //add the new added processes
            while (index < n && processes.get(index).getArrivalTime() <= currentTime) {
                readyQueue.add(processes.get(index));
                index++;
            }
           // ////////////////////////////////////
            // If process not completed
            if (!current.isFinished()) { // Remaining time > 0
//                if(!readyQueue.isEmpty())
//                {
//                    currentTime += contextSwitch; // add the contextswitching cost
//                }
                    readyQueue.add(current); // add to the end of the queue
            } else {
                completed++;
//
                current.setCompletionTime(currentTime);
                current.setTurnaroundTime(
                        current.getCompletionTime() - current.getArrivalTime());
                current.setWaitingTime(
                        current.getTurnaroundTime() - current.getBurstTime());
            }
            boolean willRunAnotherProcess = !readyQueue.isEmpty() || index < n;

            if (willRunAnotherProcess) //to add context if another process will start
            {
                currentTime += contextSwitch;

            }

        }
    }

    // Output Methods 

    @Override
    public void printExecutionOrder() {
        System.out.println("Execution Order:");
        for (String p : executionOrder)
        {
            System.out.print(p + " -> ");
        }
        System.out.println("The END");
    }

    @Override
    public void printWaitingTime() {
        System.out.println("\n Waiting Time:");
        for (Process p : processes) {
            System.out.println(p.getName() + ": " + p.getWaitingTime());
        }
    }

    @Override
    public void printTurnaroundTime() {
        System.out.println("\nTurnaround Time:");
        for (Process p : processes) {
            System.out.println(p.getName() + ": " + p.getTurnaroundTime());
        }
    }

    @Override
    public double getAverageWaitingTime() {
        double sum = 0;
        for (Process p : processes) {
            sum += p.getWaitingTime();
        }
        return sum / processes.size();
    }

    @Override
    public double getAverageTurnaroundTime() {
        double sum = 0;
        for (Process p : processes) {
            sum += p.getTurnaroundTime();
        }
        return sum / processes.size();
    }
}

