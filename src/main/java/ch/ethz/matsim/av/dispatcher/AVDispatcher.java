package ch.ethz.matsim.av.dispatcher;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface AVDispatcher {
    void onRequestSubmitted(AVRequest request);
    void onNextTaskStarted(AVVehicle vehicle);
    void onNextTimestep(double now);

    /**
     * Switch to online vehicle creation... 
     * 
     * In the future dispatchers:
     * - should check themselves for idle vehicles through onNextTaskStarted
     * - should create their own vehicles online and maintain them
     */
    @Deprecated
    void addVehicle(AVVehicle vehicle);
    
    interface AVDispatcherFactory {
        AVDispatcher createDispatcher(AVDispatcherConfig config);
    }
}
