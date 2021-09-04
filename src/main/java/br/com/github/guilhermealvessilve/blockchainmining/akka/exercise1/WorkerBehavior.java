package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise1;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    int difficultyLevel = message.getDifficulty();
                    int startNonce = message.getStartNonce();
                    int endNonce = startNonce + 1000;
                    final var block = message.getBlock();
                    var hash = new String(new char[difficultyLevel]).replace("\0", "X");
                    final var target = new String(new char[difficultyLevel]).replace("\0", "0");

                    int nonce = startNonce;
                    while (!hash.substring(0,difficultyLevel).equals(target) && nonce < endNonce) {
                        nonce++;
                        String dataToEncode = block.getPreviousHash() + block.getTransaction().getTimestamp() + nonce + block.getTransaction();
                        hash = calculateHash(dataToEncode);
                    }

                    if (hash.substring(0, difficultyLevel).equals(target)) {
                        HashResult hashResult = new HashResult();
                        hashResult.foundAHash(hash, nonce);
                        //send the hashResult to the caller;
                        getContext().getLog().debug(String.format("%d : %s", hashResult.getNonce(), hashResult.getHash()));
                        return Behaviors.same();
                    }

                    getContext().getLog().debug("null");
                    return Behaviors.same();
                })
                .build();
    }

    private String calculateHash(String data) {
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

    public static class Command implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int startNonce;
        private final int difficulty;
        private final Block block;

        public Command(int startNonce, int difficulty, Block block) {
            this.startNonce = startNonce;
            this.difficulty = difficulty;
            this.block = block;
        }

        public int getStartNonce() {
            return startNonce;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public Block getBlock() {
            return block;
        }
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(WorkerBehavior::new);
    }
}
