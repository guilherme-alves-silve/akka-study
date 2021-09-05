package br.com.github.guilhermealvessilve.blockchainmining.traditional.multithreaded;

import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;
import br.com.github.guilhermealvessilve.blockchainmining.utils.BlockChainUtils;

public class BlockMiner implements Runnable {

	private final Block block;
	private final int firstNonce;
	private final HashResult hashResult;
	private final int difficultyLevel;
	
	public BlockMiner(Block block, int firstNonce, HashResult hashResult, int difficultyLevel) {
		this.block = block;
		this.firstNonce = firstNonce;
		this.hashResult = hashResult;
		this.difficultyLevel = difficultyLevel;
	}
	
	@Override
	public void run() {
		final HashResult hashResult = BlockChainUtils.mineBlock(block, difficultyLevel, firstNonce, firstNonce * 1000);
		if (hashResult != null) {
			this.hashResult.foundAHash(hashResult.getHash(), hashResult.getNonce());
		}
	}
}
