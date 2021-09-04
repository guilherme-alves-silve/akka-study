package br.com.github.guilhermealvessilve.blockchainmining.model;

import java.util.Objects;

public class HashResult {

	private int nonce;
	private String hash;
	private boolean complete;

	public HashResult() {

	}

	public HashResult(int nonce, String hash) {
		this.nonce = nonce;
		this.hash = hash;
	}

	public int getNonce() {
		return nonce;
	}

	public String getHash() {
		return hash;
	}
	
	public boolean isRunning() {
		return !complete;
	}
	
	public synchronized void foundAHash(String hash, int nonce) {
		this.hash = hash;
		this.nonce = nonce;
		this.complete = true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof HashResult)) return false;
		HashResult that = (HashResult) o;
		return nonce == that.nonce &&
				Objects.equals(hash, that.hash);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nonce, hash);
	}
}
