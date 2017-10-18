package ch.ethz.matsim.av.electric.assets.station;

import java.util.Collection;

import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

public class StationResetter implements IterationEndsListener {
	final private Collection<Station> stations;
	
	public StationResetter(Collection<Station> stations) {
		this.stations = stations;
	}
	
	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		for (Station station : stations) {
			station.reset();
		}
	}
}
