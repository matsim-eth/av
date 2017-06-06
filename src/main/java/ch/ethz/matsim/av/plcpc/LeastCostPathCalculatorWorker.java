package ch.ethz.matsim.av.plcpc;

public interface LeastCostPathCalculatorWorker extends Runnable {
    void addTask(ParallelLeastCostPathCalculatorTask task);
}
