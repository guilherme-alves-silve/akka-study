package br.com.github.guilhermealvessilve.generateprimenumbers.exercise4;

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

    private BigInteger prime;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    if (message.getMessage().equals("start")) {
                        if (null == prime) {
                            final var bigInteger = new BigInteger(2000, new Random());
                            prime = bigInteger.nextProbablePrime();
                        }

                        message.getSender().tell(new ManagerBehavior.ResultCommand(prime));
                    }

                    return this;
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
