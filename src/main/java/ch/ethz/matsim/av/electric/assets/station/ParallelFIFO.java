package ch.ethz.matsim.av.electric.assets.station;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.utils.misc.Time;

import ch.ethz.matsim.av.electric.assets.battery.Battery;
import ch.ethz.matsim.av.electric.assets.station.events.RechargeEndEvent;
import ch.ethz.matsim.av.electric.assets.station.events.RechargeStartEvent;
import ch.ethz.matsim.av.electric.assets.station.events.VehicleArrivalEvent;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;
import ch.ethz.matsim.av.electric.tracker.ConsumptionTracker;

public class ParallelFIFO implements Station {
	static final public double TEMPERATURE = 22;

	final private Id<Station> id;
	final private Link link;
	final private ParallelFIFOSpecification specification;

	final private Queue<BatteryVehicle> waitingVehicles = new LinkedList<>();
	final private Map<BatteryVehicle, Double> rechargingVehicles = new HashMap<>();
	final private Set<BatteryVehicle> finishedVehicles = new HashSet<>();
	
	final private Set<BatteryVehicle> readyToRecharge = new HashSet<>();
	
	final private ConsumptionTracker tracker;
	final private EventsManager events;
	
	public ParallelFIFO(Id<Station> id, Link link, ParallelFIFOSpecification specification, ConsumptionTracker tracker, EventsManager events) {
		this.id = id;
		this.link = link;
		this.specification = specification;
		this.tracker = tracker;
		this.events = events;
	}

	private double calculateRechargeTime(Battery battery, double initialChargeState, double targetChargeState) {
		double rechargeRate = Math.min(battery.getSpecification().getRechargeRate(TEMPERATURE),
				specification.getRechargeRate(TEMPERATURE));
		targetChargeState = Math.min(targetChargeState, battery.getSpecification().getMaximumEnergy());
		return (targetChargeState - initialChargeState) / rechargeRate;
	}

	@Override
	public boolean proceedToRecharge(BatteryVehicle vehicle) {
		if (!waitingVehicles.contains(vehicle)) {
			throw new IllegalStateException();
		}

		if (rechargingVehicles.size() < specification.getNumberOfQueues()) {
			waitingVehicles.remove(vehicle);
			readyToRecharge.add(vehicle);
			return true;
		}

		return false;
	}

	@Override
	public boolean finishRecharging(BatteryVehicle vehicle) {
		if (finishedVehicles.contains(vehicle)) {
			finishedVehicles.remove(vehicle);
			return true;
		}

		if (!rechargingVehicles.containsKey(vehicle)) {
			throw new IllegalStateException();
		}

		return false;
	}

	@Override
	public StationSpecification getSpecification() {
		return specification;
	}

	@Override
	public Link getLink() {
		return link;
	}
	
	@Override
	public void simulate(double time) {
		Iterator<Map.Entry<BatteryVehicle, Double>> iterator = rechargingVehicles.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry<BatteryVehicle, Double> entry = iterator.next();
			
			if (entry.getValue() <= time) {
				finishedVehicles.add(entry.getKey());
				events.processEvent(new RechargeEndEvent(time, id, entry.getKey().getId(), entry.getKey().getBattery().getState()));
				iterator.remove();
			}
		}
		
		for (BatteryVehicle vehicle : readyToRecharge) {
			events.processEvent(new RechargeStartEvent(time, id, vehicle.getId(), vehicle.getBattery().getState()));
			
			double endTime = time + calculateRechargeTime(vehicle.getBattery(), vehicle.getBattery().getState(),
					vehicle.getBattery().getSpecification().getMaximumEnergy());
			
			double amount = vehicle.getBattery().getSpecification().getMaximumEnergy() - vehicle.getBattery().getState();
			tracker.addRecharge(time, endTime, amount);
			
			vehicle.getBattery().setState(vehicle.getBattery().getSpecification().getMaximumEnergy());
			rechargingVehicles.put(vehicle, endTime);
		}
		
		readyToRecharge.clear();
	}

	@Override
	public void reset() {
		readyToRecharge.clear();
		waitingVehicles.clear();
		rechargingVehicles.clear();
		finishedVehicles.clear();
	}

	@Override
	public void notifyRegistration(BatteryVehicle vehicle, double now) {
		
	}

	@Override
	public void notifyArrival(BatteryVehicle vehicle, double now) {
		events.processEvent(new VehicleArrivalEvent(now, id, vehicle.getId(), vehicle.getBattery().getState()));
		waitingVehicles.add(vehicle);
	}

	@Override
	public void notifyStartRecharge(BatteryVehicle vehicle, double now) {
	}

	@Override
	public Id<Station> getId() {
		return id;
	}
}
