package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MineBlockCommand.class, message -> {
                    this.sender = message.getSender();
                    this.difficulty = message.getDifficulty();
                    this.block = message.getBlock();

                    for (int i = 0; i < 10; i++) {
                        startNextWorker();
                    }

                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, message -> {

                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce;

    private void startNextWorker() {
        final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.newInstance(), "worker-" + currentNonce);
        worker.tell(new WorkerBehavior.Command(currentNonce + 1000, difficulty, block, getContext().getSelf()));
        ++currentNonce;
    }

    public interface Command extends Serializable {

    }

    public static class MineBlockCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final int difficulty;
        private final Block block;
        private final ActorRef<HashResult> sender;

        public MineBlockCommand(final int difficulty,
                                final Block block,
                                final ActorRef<HashResult> sender) {
            this.difficulty = difficulty;
            this.block = block;
            this.sender = sender;
        }

        public int getDifficulty() {
            return difficulty;
        }

        public Block getBlock() {
            return block;
        }

        public ActorRef<HashResult> getSender() {
            return sender;
        }
    }

    public static class HashResultCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final HashResult hashResult;

        public HashResultCommand(final HashResult hashResult) {
            this.hashResult = hashResult;
        }

        public HashResult getHashResult() {
            return hashResult;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HashResultCommand)) return false;
            HashResultCommand that = (HashResultCommand) o;
            return Objects.equals(hashResult, that.hashResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hashResult);
        }
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(ManagerBehavior::new);
    }
}
