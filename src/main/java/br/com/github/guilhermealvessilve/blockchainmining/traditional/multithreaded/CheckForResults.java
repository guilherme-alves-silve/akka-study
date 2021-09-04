package br.com.github.guilhermealvessilve.blockchainmining.traditional.multithreaded;


import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;

public class CheckForResults implements Runnable {
	
	private final HashResult hashResult;
	
	public CheckForResults(HashResult hashResult) {
		this.hashResult = hashResult;
	}

	@Override
	public void run() {
		while (hashResult.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
