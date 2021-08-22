package br.com.github.guilhermealvessilve.racinggame.traditional;

import java.util.Map;
import java.util.Random;

/**
 * Code from the course.
 */
public class Racer implements Runnable {

	private static final double DEFAULT_AVERAGE_SPEED = 48.2;

	private final int id;
	private final int raceLength;
	private final Map<Integer, Integer> currentPositions;
	private final Map<Integer, Long> results;

	private final int averageSpeedAdjustmentFactor;
	private final Random random;
	
	private double currentSpeed = 0;
	private double currentPosition = 0;
	
	public Racer(final int id,
				 final int raceLength,
				 final Map<Integer, Integer> currentPositions,
				 final Map<Integer, Long> results) {
		this.id = id;
		this.raceLength = raceLength;
		this.currentPositions = currentPositions;
		this.results = results;
		random = new Random();
		averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
	}
	
	private double getMaxSpeed() {
		return DEFAULT_AVERAGE_SPEED * (1+((double)averageSpeedAdjustmentFactor / 100));
	}
		
	private double getDistanceMovedPerSecond() {
		return currentSpeed * 1000 / 3600;
	}
	
	private void determineNextSpeed() {
		if (currentPosition < (raceLength / 4)) {
			currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
		} else {
			currentSpeed = currentSpeed * (0.5 + random.nextDouble());
		}
	
		if (currentSpeed > getMaxSpeed()) {
			currentSpeed = getMaxSpeed();
		}
		
		if (currentSpeed < 5) {
			currentSpeed = 5;
		}
		
		if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
			currentSpeed = getMaxSpeed() / 2;
		}
	}

	@Override
	public void run() {
		
		while (currentPosition < raceLength) {
			determineNextSpeed();
			currentPosition += getDistanceMovedPerSecond();
			if (currentPosition > raceLength )
				currentPosition  = raceLength;
			currentPositions.put(id, (int)currentPosition);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		results.put(id, System.currentTimeMillis());
	}
}
