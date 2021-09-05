package br.com.github.guilhermealvessilve.generateprimenumbers.exercise6;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    private WorkerBehavior(final ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return handleMessagesWhenWeDontHaveYetAPrimeNumber();
    }

    private Receive<Command> handleMessagesWhenWeDontHaveYetAPrimeNumber() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    final var bigInteger = new BigInteger(2000, new Random());
                    final var prime = bigInteger.nextProbablePrime();
                    message.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    return handleMessagesWhenWeHaveAPrimeNumber(prime);
                })
                .build();
    }

    private Receive<Command> handleMessagesWhenWeHaveAPrimeNumber(final BigInteger prime) {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    message.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    return Behaviors.same();
                })
                .build();
    }

    public static class Command implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String message;
        private final ActorRef<ManagerBehavior.Command> sender;

        public Command(final String message,
                       final ActorRef<ManagerBehavior.Command> sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<ManagerBehavior.Command> getSender() {
            return sender;
        }
    }
}
