package ch.ethz.matsim.av.dispatcher;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface AVDispatcher {
    void onRequestSubmitted(AVRequest request);
    void onNextTaskStarted(AVVehicle vehicle);
    void onNextTimestep(double now);

    void addVehicle(AVVehicle vehicle);

    // TODO: Temporary change for recharging. Eventually, even addVehicle should be replaced .
    void removeVehicle(AVVehicle vehicle);
    boolean hasVehicle(AVVehicle vehicle);

    interface AVDispatcherFactory {
        AVDispatcher createDispatcher(AVDispatcherConfig config);
    }
}
