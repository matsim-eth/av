package ch.ethz.matsim.av.electric.tracker;

import ch.ethz.matsim.av.extern.BinCalculator;

import org.matsim.core.controler.corelisteners.DumpDataAtEnd;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.utils.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVConsumptionTracker implements ConsumptionTracker, IterationStartsListener, IterationEndsListener, ShutdownListener {
    final private BinCalculator binCalculator;

    final private List<Double> timeBasedConsumption;
    final private List<Double> distanceBasedConsumption;
    final private List<Double> rechargeEnergy;

    public CSVConsumptionTracker(BinCalculator binCalculator) {
        this.binCalculator = binCalculator;

        timeBasedConsumption = new ArrayList<>(binCalculator.getBins());
        distanceBasedConsumption = new ArrayList<>(binCalculator.getBins());
        rechargeEnergy = new ArrayList<>(binCalculator.getBins());

        for (int i = 0; i < binCalculator.getBins(); i++) {
            timeBasedConsumption.add(0.0);
            distanceBasedConsumption.add(0.0);
            rechargeEnergy.add(0.0);
        }
    }

    private void addConsumption(List<Double> consumptionData, double start, double end, double consumption) {
        double consumptionPerSecond = consumption / (end - start);
        if (Double.isNaN(consumptionPerSecond)) consumptionPerSecond = 0.0;

        for (BinCalculator.BinEntry entry : binCalculator.getBinEntriesNormalized(start, end)) {
            consumptionData.set(entry.getIndex(), consumptionData.get(entry.getIndex()) + entry.getWeight() * binCalculator.getInterval() * consumptionPerSecond);
        }
    }

    @Override
    public void addDistanceBasedConsumption(double start, double end, double consumption) {
        addConsumption(distanceBasedConsumption, start, end, consumption);
    }

    @Override
    public void addTimeBasedConsumption(double start, double end, double consumption) {
        addConsumption(timeBasedConsumption, start, end, consumption);
    }

    private void clearConsumption(List<Double> consumption) {
        for (int i = 0; i < binCalculator.getBins(); i++) {
            consumption.set(i, 0.0);
        }
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        clearConsumption(distanceBasedConsumption);
        clearConsumption(timeBasedConsumption);
        clearConsumption(rechargeEnergy);
    }
    
    private void writeOutput(String path) {
        try {
            OutputStream stream = IOUtils.getOutputStream(path);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(stream)));

            writer.write("TIME;DISTANCE_BASED;TIME_BASED;RECHARGE\n");

            for (int i = 0; i < binCalculator.getBins(); i++) {
                writer.write(String.format("%d;%f;%f;%f\n", (int) binCalculator.getStart(i), distanceBasedConsumption.get(i), timeBasedConsumption.get(i), rechargeEnergy.get(i)));
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
    	writeOutput(event.getServices().getControlerIO().getIterationFilename(event.getIteration(), "consumption.csv"));
    }
    

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		writeOutput(event.getServices().getControlerIO().getOutputFilename("output_consumption.csv"));
	}

	@Override
	public void addRecharge(double start, double end, double amount) {
		addConsumption(rechargeEnergy, start, end, amount);
	}
}
