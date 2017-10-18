package ch.ethz.matsim.av.electric.tracker;

public class NullConsumptionTracker implements ConsumptionTracker {
    @Override
    public void addDistanceBasedConsumption(double start, double end, double consumption) {

    }

    @Override
    public void addTimeBasedConsumption(double start, double end, double consumption) {

    }

	@Override
	public void addRecharge(double start, double end, double amount) {

	}
}
