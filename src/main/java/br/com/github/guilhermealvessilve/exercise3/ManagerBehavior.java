package br.com.github.guilhermealvessilve.exercise3;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {

    }

    public static class InstructionCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final String message;

        public InstructionCommand(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ResultCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final BigInteger result;

        public ResultCommand(BigInteger result) {
            this.result = result;
        }

        public BigInteger getResult() {
            return result;
        }
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    private final SortedSet<BigInteger> primes = new TreeSet<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, message -> {

                    if (message.getMessage().equals("start")) {
                        for (int i = 0; i < 20; i++) {
                            final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(WorkerBehavior.newInstance(), "worker-" + i);
                            worker.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
                        }
                    }

                    return this;
                })
                .onMessage(ResultCommand.class, message -> {
                    primes.add(message.getResult());

                    System.out.format("Generated %d prime number(s).%n", primes.size());
                    if (primes.size() == 20) {
                        primes.forEach(prime -> System.out.format("[%s]%n", prime.toString()));
                    }

                    return this;
                })
                .build();
    }
}
