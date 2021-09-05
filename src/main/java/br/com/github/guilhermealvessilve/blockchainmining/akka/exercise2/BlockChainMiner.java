package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise2;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.BlockChain;
import br.com.github.guilhermealvessilve.blockchainmining.model.BlockValidationException;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;
import br.com.github.guilhermealvessilve.blockchainmining.utils.BlocksData;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class BlockChainMiner {
	
	private final int difficultyLevel = 5;
	private final long start = System.currentTimeMillis();
	private final BlockChain blocks = new BlockChain();
	private ActorSystem<ManagerBehavior.Command> actorSystem;

	private void mineNextBlock() {
		int nextBlockId = blocks.getSize();
		if (nextBlockId < 10) {
			String lastHash = nextBlockId > 0 ? blocks.getLastHash() : "0";
			Block block = BlocksData.getNextBlock(nextBlockId, lastHash);
			CompletionStage<HashResult> results = AskPattern.ask(actorSystem,
					me -> new ManagerBehavior.MineBlockCommand(difficultyLevel, block, me),
					Duration.ofSeconds(30),
					actorSystem.scheduler());
				
			results.whenComplete( (reply,failure) -> {
				
				if (reply == null || reply.isRunning()) {
					System.out.println("ERROR: No valid hash was found for a block");
					return;
				}
				
				block.setHash(reply.getHash());
				block.setNonce(reply.getNonce());
				
				try {
					blocks.addBlock(block);
					System.out.println("Block added with hash : " + block.getHash());
					System.out.println("Block added with nonce: " + block.getNonce());
					mineNextBlock();
				} catch (BlockValidationException e) {
					System.out.println("ERROR: No valid hash was found for a block");
				}
			});
			
		}
		else {
			long end = System.currentTimeMillis();
			actorSystem.terminate();
			blocks.printAndValidate();
			System.out.println("Time taken " + (end - start) + " ms.");
		}
	}
	
	public void mineBlocks() {
		actorSystem = ActorSystem.create(ManagerBehavior.newInstance(), "BlockChainMiner");
		mineNextBlock();
	}
}
