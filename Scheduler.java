import java.util.List;

public interface Scheduler {

    void schedule(List<Process> processes);

    void printExecutionOrder();

    void printWaitingTime();

    void printTurnaroundTime();

    double getAverageWaitingTime();

    double getAverageTurnaroundTime();
}
