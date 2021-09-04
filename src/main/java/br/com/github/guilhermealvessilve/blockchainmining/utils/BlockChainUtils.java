package br.com.github.guilhermealvessilve.blockchainmining.utils;

import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BlockChainUtils {

	public static String calculateHash(String data) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
			final var hexString = new StringBuilder();
			for (final byte hash : rawHash) {
				final var hex = Integer.toHexString(0xff & hash);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} 
	}
		
		
	public static HashResult mineBlock(Block block, int difficultyLevel, int startNonce, int endNonce) {
		var hash = new String(new char[difficultyLevel]).replace("\0", "X");
		final var target = new String(new char[difficultyLevel]).replace("\0", "0");
				
		int nonce = startNonce;
		while (!hash.substring(0,difficultyLevel).equals(target) && nonce < endNonce) {
			nonce++;
			String dataToEncode = block.getPreviousHash() + block.getTransaction().getTimestamp() + nonce + block.getTransaction();
			hash = calculateHash(dataToEncode);
		}
		if (hash.substring(0,difficultyLevel).equals(target)) {
			HashResult hashResult = new HashResult();
			hashResult.foundAHash(hash, nonce);
			return hashResult;
		}

		return null;
	}
	
	public static boolean validateBlock(Block block) {
		final var dataToEncode = block.getPreviousHash() + block.getTransaction().getTimestamp() + block.getNonce() + block.getTransaction();
		final var checkHash = calculateHash(dataToEncode);
		return checkHash.equals(block.getHash());
	}
}
