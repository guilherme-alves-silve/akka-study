package br.com.github.guilhermealvessilve.racinggame.traditional;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Code from the course.
 */
public class Main {

	private static final int RACE_LENGTH = 100;
	private static final int DISPLAY_LENGTH = 160;
	private static long start;
	
	private static void displayRace(final Map<Integer, Integer> currentPositions) {
		for (int i = 0; i < 50; ++i) System.out.println();
		System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		System.out.println("    " + new String (new char[DISPLAY_LENGTH]).replace('\0', '='));
		for (int i = 0; i < 10; i++) {
			System.out.println(i + " : "  + new String (new char[currentPositions.get(i) * DISPLAY_LENGTH / 100]).replace('\0', '*'));
		}
	}

	public static void main(String[] args) throws InterruptedException {
		
		final var currentPositions = new ConcurrentHashMap<Integer, Integer>();
		final var results = new ConcurrentHashMap<Integer, Long>();
		
		start = System.currentTimeMillis();
		
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		
		for (int i = 0; i <10; i++) {
			final var racer = new Racer(i, RACE_LENGTH, currentPositions, results);
			currentPositions.put(i, 0);
			threadPool.execute(racer);
		}
		
		boolean finished = false;
		while (!finished) {
			Thread.sleep(1000);
			displayRace(currentPositions);
			finished = results.size() == 10;
		}
		
		threadPool.shutdownNow();
				
		System.out.println("Results");
		results.values()
				.stream()
				.sorted()
				.forEach(it -> {
			for (Integer key : results.keySet()) {
				if (Objects.equals(results.get(key), it)) {
					System.out.println("Racer " + key + " finished in " + ( (double)it - start ) / 1000 + " seconds.");
				}
			}
		});
	}
}
