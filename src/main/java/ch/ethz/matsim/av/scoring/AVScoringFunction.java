package ch.ethz.matsim.av.scoring;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.core.scoring.SumScoringFunction;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.PricingConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.schedule.AVTransitEvent;

public class AVScoringFunction implements SumScoringFunction.ArbitraryEventScoring {
	final static Logger log = Logger.getLogger(AVScoringFunction.class);

	final private AVConfigGroup config;
	final private double marginalUtilityOfWaiting;
	final private double marginalUtilityOfTraveling;
	final private double marginalUtilityOfMoney;
	final private double stuckUtility;

	final private Set<Id<AVOperator>> subscriptions = new HashSet<>();

	private AVScoringTrip scoringTrip = null;
	private double score = 0.0;

	public AVScoringFunction(AVConfigGroup config, double marginalUtilityOfMoney, double marginalUtilityOfTraveling,
			double marginalUtilityOfWaiting, double stuckUtility) {
		this.marginalUtilityOfWaiting = marginalUtilityOfWaiting;
		this.marginalUtilityOfTraveling = marginalUtilityOfTraveling;
		this.marginalUtilityOfMoney = marginalUtilityOfMoney;
		this.stuckUtility = stuckUtility;
		this.config = config;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof PersonDepartureEvent) {
			if (((PersonDepartureEvent) event).getLegMode().equals(AVModule.AV_MODE)) {
				if (scoringTrip != null) {
					throw new IllegalStateException();
				}

				scoringTrip = new AVScoringTrip();
				scoringTrip.processDeparture((PersonDepartureEvent) event);
			}
		} else if (event instanceof PersonEntersVehicleEvent) {
			if (scoringTrip != null) {
				scoringTrip.processEnterVehicle((PersonEntersVehicleEvent) event);
			}
		} else if (event instanceof AVTransitEvent) {
			if (scoringTrip != null) {
				scoringTrip.processTransit((AVTransitEvent) event);
			}
		}

		if (scoringTrip != null && scoringTrip.isFinished()) {
			handleScoringTrip(scoringTrip);
			scoringTrip = null;
		}
	}

	private PricingConfig getPricingConfig(Id<AVOperator> id) {
		for (OperatorConfig oc : config.getOperators().values()) {
			if (oc.getId().equals(id)) {
				return oc.getPricingConfig();
			}
		}

		throw new IllegalStateException("No pricing found for operator: " + id);
	}

	private void handleScoringTrip(AVScoringTrip trip) {
		score += computeWaitingTimeScoring(trip);
		score += computePricingScoring(trip);
	}

	private double computeWaitingTimeScoring(AVScoringTrip trip) {
		// Compensate for the travel disutility
		return (marginalUtilityOfWaiting - marginalUtilityOfTraveling) * trip.getWaitingTime();
	}

	static int noPricingWarningCount = 100;

	private double computePricingScoring(AVScoringTrip trip) {
		PricingConfig priceStructure = getPricingConfig(trip.getOperatorId());

		double costs = 0.0;

		double billableDistance = Math.max(1,
				Math.ceil(trip.getDistance() / priceStructure.getSpatialBillingInterval()))
				* priceStructure.getSpatialBillingInterval();

		double billableTravelTime = Math.max(1,
				Math.ceil(trip.getInVehicleTravelTime() / priceStructure.getTemporalBillingInterval()))
				* priceStructure.getTemporalBillingInterval();

		costs += (billableDistance / 1000.0) * priceStructure.getPricePerKm();
		costs += (billableTravelTime / 60.0) * priceStructure.getPricePerMin();
		costs += priceStructure.getPricePerTrip();

		if (priceStructure.getPricePerDay() > 0.0) {
			if (!subscriptions.contains(trip.getOperatorId())) {
				costs += priceStructure.getPricePerDay();
				subscriptions.add(trip.getOperatorId());
			}
		}

		return -costs * marginalUtilityOfMoney;
	}

	@Override
	public void finish() {
		if (scoringTrip != null) {
			score += stuckUtility;
		}
	}

	@Override
	public double getScore() {
		return score;
	}
}
