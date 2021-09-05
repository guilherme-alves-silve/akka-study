package br.com.github.guilhermealvessilve.blockchainmining.traditional.multithreaded;

import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.BlockChain;
import br.com.github.guilhermealvessilve.blockchainmining.model.BlockValidationException;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;
import br.com.github.guilhermealvessilve.blockchainmining.utils.BlocksData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args) throws BlockValidationException {
		
		int difficultyLevel = 5;
		
		Long start = System.currentTimeMillis();
		BlockChain blocks = new BlockChain();
		
		String lastHash = "0";
		for (int i = 0; i < 10; i++) {
			final ExecutorService es = Executors.newFixedThreadPool(Math.max(10, Runtime.getRuntime().availableProcessors()));
			final Block nextBlock = BlocksData.getNextBlock(i, lastHash);
			final HashResult hashResult = new HashResult();
			final Thread resultsThread = new Thread(new CheckForResults(hashResult));
			resultsThread.start();
						
			for (int nonceSeed = 0; nonceSeed < 10000; nonceSeed++) {
				BlockMiner miner = new BlockMiner(nextBlock, nonceSeed * 1000, hashResult, difficultyLevel);
				es.execute(miner);
			}
						
			try {
				resultsThread.join();
				es.shutdownNow();
				if (hashResult.isRunning()) {
					throw new RuntimeException("Failed to find a has for block " + i);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			nextBlock.setHash(hashResult.getHash());
			nextBlock.setNonce(hashResult.getNonce());
						
			blocks.addBlock(nextBlock);
			System.out.println("Block " + i + " hash : " + nextBlock.getHash());
			System.out.println("Block " + i + " nonce: " + nextBlock.getNonce());
			lastHash = nextBlock.getHash();
		}
				
		Long end = System.currentTimeMillis();
		
		blocks.printAndValidate();
		
		System.out.println("Time taken " + (end - start) + " ms.");
	}
}
