package ch.ethz.matsim.av.electric.tracker;

public interface ConsumptionTracker {
    void addDistanceBasedConsumption(double start, double end, double consumption);
    void addTimeBasedConsumption(double start, double end, double consumption);
}
