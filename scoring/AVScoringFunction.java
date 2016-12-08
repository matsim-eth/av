package playground.sebhoerl.avtaxi.scoring;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.scoring.PersonExperiencedLeg;
import org.matsim.core.scoring.SumScoringFunction;
import org.opengis.filter.capability.Operator;
import playground.sebhoerl.avtaxi.config.AVConfig;
import playground.sebhoerl.avtaxi.config.AVOperatorConfig;
import playground.sebhoerl.avtaxi.config.AVPriceStructureConfig;
import playground.sebhoerl.avtaxi.data.AVOperator;
import playground.sebhoerl.avtaxi.framework.AVModule;
import playground.sebhoerl.avtaxi.routing.AVRoute;

import java.util.*;

public class AVScoringFunction implements SumScoringFunction.ArbitraryEventScoring, SumScoringFunction.LegScoring {
    final static Logger log = Logger.getLogger(AVScoringFunction.class);

    final double NO_DEPARTURE = -1.0;
    final private AVConfig config;

    final private double marginalUtilityOfWaiting;
    final private double marginalUtilityOfTraveling;
    final private double marginalUtilityOfMoney;

    final private Set<Id<AVOperator>> subscriptions = new HashSet<>();
    final private Person person;

    private Iterator<PlanElement> planIterator = null;
    private double departureTime = NO_DEPARTURE;
    private Double waitingTime = null;
    private double score = 0.0;
    
    public AVScoringFunction(AVConfig config, Person person, double marginalUtilityOfMoney, double marginalUtilityOfTraveling) {
        this.marginalUtilityOfWaiting = config.getMarginalUtilityOfWaitingTime() / 3600.0;
        this.marginalUtilityOfTraveling = marginalUtilityOfTraveling;
        this.marginalUtilityOfMoney = marginalUtilityOfMoney;
        this.config = config;
        this.person = person;
    }
    
    @Override
    public void handleEvent(Event event) {
        if (event instanceof PersonDepartureEvent) {
            if (((PersonDepartureEvent) event).getLegMode() == AVModule.AV_MODE) {
                if (departureTime != NO_DEPARTURE) {
                    throw new IllegalStateException();
                }
                
                this.departureTime = event.getTime();
                waitingTime = null;
            }
        } else if (event instanceof PersonEntersVehicleEvent && departureTime != NO_DEPARTURE) {
            if (waitingTime != null) {
                throw new IllegalStateException();
            }

            waitingTime = event.getTime() - departureTime;

            // Compensate for the utility that is generated by the CharyparNagelLegScoring
            score += marginalUtilityOfWaiting * waitingTime - marginalUtilityOfTraveling * waitingTime;
            departureTime = NO_DEPARTURE;
        }
    }

    private AVPriceStructureConfig getPriceStructure(Id<AVOperator> id) {
        for (AVOperatorConfig oc : config.getOperatorConfigs()) {
            if (oc.getId().equals(id)) {
                return oc.getPriceStructureConfig();
            }
        }

        return null;
    }

    @Override
    public void handleLeg(Leg leg) {
        if (leg.getMode().equals(AVModule.AV_MODE)) {
            if (waitingTime == null) {
                throw new IllegalStateException(String.valueOf(waitingTime));
            }

            AVRoute route = getNextRoute();
            route.setTravelTime(leg.getRoute().getTravelTime() - waitingTime);

            if (Double.isNaN(route.getDistance())) {
                System.out.println("Planned Route " + route);
                System.out.println("Recorded Route " + leg.getRoute());
                System.out.println("Recorded Leg " + leg);
                System.out.println(person.getId().toString());

                // TODO: FIX THAT
                route.setDistance(0.0);
            }

            score += getPriceScoringForRoute(route, waitingTime);
        }
    }

    private AVRoute getNextRoute() {
        /*
         * We need to get the route information such as the operator id from the population
         * since the recorded plans do not recover this information.
         */

        if (planIterator == null) {
            planIterator = person.getSelectedPlan().getPlanElements().iterator();
        }

        while (planIterator.hasNext()) {
            PlanElement element = planIterator.next();

            if (element instanceof Leg && ((Leg) element).getMode().equals(AVModule.AV_MODE)) {
                return (AVRoute) ((Leg) element).getRoute();
            }
        }

        throw new IllegalStateException();
    }

    static int noPricingWarningCount = 100;
    static int noDistanceWarningCount = 100;

    private double getPriceScoringForRoute(AVRoute route, double waitingTime) {
        AVPriceStructureConfig priceStructure = getPriceStructure(route.getOperatorId());

        if (route.getDistance() == 0.0 && noDistanceWarningCount > 0) {
            log.warn("Found AV route with zero distance. Has not been set by dispatcher?");
            noDistanceWarningCount--;
        }

        double costs = 0.0;

        if (priceStructure != null) {
            double billableDistance = Math.max(1, Math.ceil(
                    route.getDistance() / priceStructure.getSpatialBillingInterval()))
                    * priceStructure.getSpatialBillingInterval();

            double billableTravelTime = Math.max(1, Math.ceil(
                    route.getTravelTime() / priceStructure.getTemporalBillingInterval()))
                    * priceStructure.getTemporalBillingInterval();

            costs += (billableDistance / 1000.0) * priceStructure.getPricePerKm();
            costs += (billableTravelTime / 60.0) * priceStructure.getPricePerMin();
            costs += priceStructure.getPricePerTrip();

            if(priceStructure.getDailySubscriptionFee() > 0.0) {
                if (!subscriptions.contains(route.getOperatorId())) {
                    costs += priceStructure.getDailySubscriptionFee();
                    subscriptions.add(route.getOperatorId());
                }
            }
        } else if (noPricingWarningCount > 0) {
            log.warn("No pricing strategy defined for operator " + route.getOperatorId().toString());
            noPricingWarningCount--;
        }

        return -costs * marginalUtilityOfMoney;
    }
    
    @Override
    public void finish() {}

    @Override
    public double getScore() {
        return score;
    }
}
