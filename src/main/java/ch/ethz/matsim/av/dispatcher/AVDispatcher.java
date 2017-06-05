package ch.ethz.matsim.av.dispatcher;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface AVDispatcher {
    void onRequestSubmitted(AVRequest request);
    void onNextTaskStarted(AVVehicle vehicle);
    void onNextTimestep(double now);

    void addVehicle(AVVehicle vehicle);

    interface AVDispatcherFactory {
        AVDispatcher createDispatcher(AVDispatcherConfig config);
    }
}
