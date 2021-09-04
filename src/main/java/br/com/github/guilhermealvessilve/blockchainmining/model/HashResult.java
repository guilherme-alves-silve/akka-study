package br.com.github.guilhermealvessilve.blockchainmining.model;

public class HashResult {

	private int nonce;
	private String hash;
	private boolean complete;
	
	public HashResult() {}
	
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
	
}
