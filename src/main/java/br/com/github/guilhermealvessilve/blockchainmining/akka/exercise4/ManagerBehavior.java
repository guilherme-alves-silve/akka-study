package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.*;
import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private final StashBuffer<Command> stashBuffer;

    private ManagerBehavior(final ActorContext<Command> context,
                            final StashBuffer<Command> stashBuffer) {
        super(context);
        this.stashBuffer = stashBuffer;
    }

    @Override
    public Receive<Command> createReceive() {
        return idleMessageHandler();
    }

    private Receive<Command> idleMessageHandler() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, handler -> Behaviors.same())
                .onMessage(MineBlockCommand.class, message -> {
                    this.sender = message.getSender();
                    this.difficulty = message.getDifficulty();
                    this.block = message.getBlock();
                    this.currentlyMining = true;

                    for (int i = 0; i < 10; i++) {
                        startNextWorker();
                    }

                    return activeMessageHandler();
                })
                .build();
    }

    private Receive<Command> activeMessageHandler() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, handler -> {
                    startNextWorker();
                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, message -> {
                    getContext()
                            .getChildren()
                            .forEach(getContext()::stop);
                    this.currentlyMining = false;
                    sender.tell(message.getHashResult());
                    return stashBuffer.unstashAll(idleMessageHandler());
                })
                .onMessage(MineBlockCommand.class, message -> {
                    System.out.println("Delaying a mining request");
                    if (!stashBuffer.isFull()) {
                        stashBuffer.stash(message);
                    }
                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce;
    private boolean currentlyMining;

    private void startNextWorker() {
        if (!currentlyMining) {
            return;
        }

        getContext().getLog().info("About to start mining with nonces starting at " + (currentNonce * 1000));

        final Behavior<WorkerBehavior.Command> workerBehavior = Behaviors.supervise(WorkerBehavior.newInstance())
                .onFailure(SupervisorStrategy.resume());

        final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(workerBehavior, "worker-" + currentNonce);
        getContext().watch(worker);
        worker.tell(new WorkerBehavior.Command(currentNonce * 1000, difficulty, block, getContext().getSelf()));
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

        return Behaviors.withStash(10,
                stash -> Behaviors.setup(ctx -> new ManagerBehavior(ctx, stash)));
    }
}
