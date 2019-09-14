package ch.ethz.matsim.av.cost;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.data.AVOperator;

public interface PriceCalculator {
	double calculate_MU(Id<AVOperator> operatorId, double travelDistance_m, double traveTime_s);
}
