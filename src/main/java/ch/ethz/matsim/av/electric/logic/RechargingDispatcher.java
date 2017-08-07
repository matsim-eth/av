package ch.ethz.matsim.av.electric.logic;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.electric.calculators.ChargeCalculator;
import ch.ethz.matsim.av.electric.tracker.ConsumptionTracker;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import com.google.inject.Inject;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RechargingDispatcher implements AVDispatcher {
    final private Map<AVVehicle, Double> chargeState = new HashMap<>();

    final private AVDispatcher delegate;
    final private ChargeCalculator chargeCalculator;
    final private ConsumptionTracker consumptionTracker;

    final private Set<AVVehicle> ideling = new HashSet<>();

    private double now = Double.NEGATIVE_INFINITY;

    public RechargingDispatcher(ChargeCalculator chargeCalculator, AVDispatcher dispatcher, ConsumptionTracker consumptionTracker) {
        this.delegate = dispatcher;
        this.chargeCalculator = chargeCalculator;
        this.consumptionTracker = consumptionTracker;
    }

    @Override
    public void onRequestSubmitted(AVRequest request) {
        delegate.onRequestSubmitted(request);
    }

    @Override
    public void onNextTaskStarted(AVVehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();

        Task current = schedule.getCurrentTask();
        Task previous = schedule.getTasks().get(current.getTaskIdx() - 1);

        Double currentChargeState = chargeState.get(vehicle);
        double delta;

        if (currentChargeState != null) {
            if (previous instanceof AVDriveTask) {
                double distance = 0.0;
                for (int i = 0; i < ((AVDriveTask) previous).getPath().getLinkCount(); i++) {
                    distance += ((AVDriveTask) previous).getPath().getLink(i).getLength();
                }

                delta = chargeCalculator.calculateConsumption(previous.getBeginTime(), previous.getEndTime(), distance);
                currentChargeState -= delta;
                consumptionTracker.addDistanceBasedConsumption(previous.getBeginTime(), previous.getEndTime(), delta);
            }

            if (!(previous instanceof AVStayTask)) {
                delta = chargeCalculator.calculateConsumption(previous.getBeginTime(), previous.getEndTime());
                currentChargeState -= delta;
                consumptionTracker.addTimeBasedConsumption(previous.getBeginTime(), previous.getEndTime(), delta);
            }

            chargeState.put(vehicle, currentChargeState);
        }

        //makeVehicleRechargeIfPossibleAndNecessary(vehicle);

        Task currentTask = schedule.getCurrentTask();

        if (currentTask instanceof AVStayTask) {
            ideling.add(vehicle);
        } else {
            ideling.remove(vehicle);
        }

        if (!(currentTask instanceof RechargingTask)) {
            delegate.onNextTaskStarted(vehicle);
        }
    }

    private void makeVehicleRechargeIfPossibleAndNecessary(AVVehicle vehicle) {
        Double currentChargeState = chargeState.get(vehicle);

        Schedule schedule = vehicle.getSchedule();
        Task currentTask = schedule.getCurrentTask();

        if (currentChargeState != null && currentTask instanceof AVStayTask && chargeCalculator.isCritical(currentChargeState, now) && currentTask == Schedules.getLastTask(schedule)) {
            double scheduleEndTime = schedule.getEndTime();
            double rechargingEndTime = Math.min(now + chargeCalculator.getRechargeTime(now), scheduleEndTime);

            currentTask.setEndTime(now);

            schedule.addTask(new RechargingTask(now, rechargingEndTime, ((AVStayTask) currentTask).getLink()));
            schedule.addTask(new AVStayTask(rechargingEndTime, scheduleEndTime, ((AVStayTask) currentTask).getLink()));

            chargeState.put(vehicle, chargeCalculator.getMaximumCharge(rechargingEndTime));

            if (delegate.hasVehicle(vehicle)) {
                delegate.removeVehicle(vehicle);
            }
        }
    }

    @Override
    public void onNextTimestep(double now) {
        this.now = now;

        for (AVVehicle vehicle : ideling) {
            makeVehicleRechargeIfPossibleAndNecessary(vehicle);
        }

        delegate.onNextTimestep(now);
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        chargeState.put(vehicle, chargeCalculator.getInitialCharge(now));
        delegate.addVehicle(vehicle);
    }

    @Override
    public void removeVehicle(AVVehicle vehicle) {
        delegate.removeVehicle(vehicle);
    }

    @Override
    public boolean hasVehicle(AVVehicle vehicle) {
        return delegate.hasVehicle(vehicle);
    }

    static public class Factory implements AVDispatcherFactory {
        @Inject
        Map<String, AVDispatcher.AVDispatcherFactory> factories;

        @Inject
        Map<String, ChargeCalculator> chargeCalculators;

        @Inject
        ConsumptionTracker tracker;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig config) {
            if (!config.getParams().containsKey("delegate")) {
                throw new IllegalArgumentException();
            }

            if (!config.getParams().containsKey("charge_calculator")) {
                throw new IllegalArgumentException("No charge_calculator specified for dispatcher of " + config.getParent().getId());
            }

            String delegateDisaptcherName = config.getParams().get("delegate");
            AVDispatcherConfig delegateConfig = new AVDispatcherConfig(config.getParent(), delegateDisaptcherName);

            for (Map.Entry<String, String> entry : config.getParams().entrySet()) {
                delegateConfig.getParams().put(entry.getKey(), entry.getValue());
            }

            String chargeCalculatorName = config.getParams().get("charge_calculator");

            if (!factories.containsKey(delegateDisaptcherName)) {
                throw new IllegalArgumentException("Delegate dispatcher '" + delegateDisaptcherName + "' does not exist!");
            }

            if (!chargeCalculators.containsKey(chargeCalculatorName)) {
                throw new IllegalArgumentException("Charge calculator '" + chargeCalculatorName + "' does not exist!");
            }

            return new RechargingDispatcher(chargeCalculators.get(chargeCalculatorName), factories.get(delegateDisaptcherName).createDispatcher(delegateConfig), tracker);
        }
    }
}
