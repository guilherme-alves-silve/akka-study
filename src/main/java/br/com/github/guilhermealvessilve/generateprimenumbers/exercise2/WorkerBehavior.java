package br.com.github.guilhermealvessilve.generateprimenumbers.exercise2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<String> {

    public static Behavior<String> newInstance() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    private WorkerBehavior(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("start", () -> {
                    final var bigInteger = new BigInteger(2000, new Random());
                    System.out.println(bigInteger.nextProbablePrime());
                    return this;
                })
                .build();
    }
}
