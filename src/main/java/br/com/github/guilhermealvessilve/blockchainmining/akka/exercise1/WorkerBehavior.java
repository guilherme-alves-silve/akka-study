package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import br.com.github.guilhermealvessilve.blockchainmining.model.Block;
import br.com.github.guilhermealvessilve.blockchainmining.model.HashResult;
import br.com.github.guilhermealvessilve.blockchainmining.utils.BlockChainUtils;

import java.io.Serializable;
import java.util.function.Supplier;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    private Supplier<HashResult> calculateHashFunction;

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    WorkerBehavior(ActorContext<Command> context, Supplier<HashResult> calculateHashFunction) {
        super(context);
        this.calculateHashFunction = calculateHashFunction;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {

                    int difficultyLevel = message.getDifficulty();
                    int startNonce = message.getStartNonce();
                    int endNonce = startNonce * 1000;
                    final var hashResult = calculateHashFunction != null
                            ? calculateHashFunction.get()
                            : BlockChainUtils.mineBlock(message.getBlock(), difficultyLevel, startNonce, endNonce);

                    if (hashResult != null) {
                        message.getController().tell(hashResult);
                        getContext().getLog().debug(String.format("%d : %s", hashResult.getNonce(), hashResult.getHash()));
                        return Behaviors.same();
                    }

                    getContext().getLog().debug("null");
                    return Behaviors.same();
                })
                .build();
    }

    public static class Command implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int startNonce;
        private final int difficulty;
        private final Block block;
        private final ActorRef<HashResult> controller;

        public Command(int startNonce, int difficulty, Block block, ActorRef<HashResult> controller) {
            this.startNonce = startNonce;
            this.difficulty = difficulty;
            this.block = block;
            this.controller = controller;
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

        public ActorRef<HashResult> getController() {
            return controller;
        }
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    static Behavior<Command> newInstance(final Supplier<HashResult> calculateHashFunction) {
        return Behaviors.setup(ctx -> new WorkerBehavior(ctx, calculateHashFunction));
    }
}
