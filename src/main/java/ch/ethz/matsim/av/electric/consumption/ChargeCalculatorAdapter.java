package ch.ethz.matsim.av.electric.consumption;

import org.matsim.contrib.dvrp.path.VrpPath;

import ch.ethz.matsim.av.electric.calculators.ChargeCalculator;

/**
 * This class is there to make the deprecated ChargeCalculators useable by the new implementation
 */
public class ChargeCalculatorAdapter implements ConsumptionCalculator {
	final private ChargeCalculator chargeCalculator;
	
	public ChargeCalculatorAdapter(ChargeCalculator chargeCalculator) {
		this.chargeCalculator = chargeCalculator;
	}
	
	@Override
	public double calculateConsumptionForDuration(double startTime, double endTime) {
		return chargeCalculator.calculateTimeBasedConsumption(startTime, endTime);
	}

	@Override
	public double calculateConsumptionForPath(double startTime, double endTime, VrpPath path) {
		return chargeCalculator.calculateDistanceBasedConsumption(startTime, endTime, calculateDistance(path));
	}
	
	private double calculateDistance(VrpPath path) {
		double distance = 0.0;
		
		for (int i = 0; i < path.getLinkCount(); i++) {
			distance += path.getLink(i).getLength();
		}
		
		return distance;
	}

	@Override
	public double calculateConsumptionForDistance(double startTime, double endTime, double distance) {
		return chargeCalculator.calculateDistanceBasedConsumption(startTime, endTime, distance);
	}
}
