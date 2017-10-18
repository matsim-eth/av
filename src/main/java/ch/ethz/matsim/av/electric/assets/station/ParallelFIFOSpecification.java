package ch.ethz.matsim.av.electric.assets.station;

public class ParallelFIFOSpecification implements StationSpecification {
	final private long numberOfQueues;
	final private double rechargeRate;
	
	public ParallelFIFOSpecification(long numberOfQueues, double rechargeRate) {
		this.numberOfQueues = numberOfQueues;
		this.rechargeRate = rechargeRate;
	}
	
	public long getNumberOfQueues() {
		return numberOfQueues;
	}

	@Override
	public double getRechargeRate(double temperature) {
		return rechargeRate;
	}

}
