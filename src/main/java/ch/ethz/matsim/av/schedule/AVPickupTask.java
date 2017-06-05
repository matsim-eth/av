package ch.ethz.matsim.av.schedule;

import ch.ethz.matsim.av.passenger.AVRequest;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AVPickupTask extends StayTaskImpl implements AVTaskWithRequests, AVTask {
	final private Set<AVRequest> requests = new HashSet<>();
	
	public AVPickupTask(double beginTime, double endTime, Link link) {
        super(beginTime, endTime, link);
	}

    public AVPickupTask(double beginTime, double endTime, Link link, Collection<AVRequest> requests) {
        this(beginTime, endTime, link);

        this.requests.addAll(requests);
        for (AVRequest request : requests) request.setPickupTask(this);
    }
    
    @Override
    public AVTaskType getAVTaskType() {
    	return AVTaskType.PICKUP;
    }
    
    @Override
    public void addRequest(AVRequest request) {
        requests.add(request);
        request.setPickupTask(this);
    }

    @Override
    public Set<AVRequest> getRequests() {
        return requests;
    }

    @Override
    protected String commonToString()
    {
        return "[" + getAVTaskType().name() + "]" + super.commonToString();
    }
}
