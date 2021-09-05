package br.com.github.guilhermealvessilve.generateprimenumbers.exercise6;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(ManagerBehavior.newInstance(), "ManagerBehavior");
        final CompletionStage<SortedSet<BigInteger>> completionStage = AskPattern.ask(actorSystem,
                (me) -> new ManagerBehavior.InstructionCommand("start", me),
                Duration.ofSeconds(60),
                actorSystem.scheduler());

        completionStage.whenComplete((reply, failure) -> {
            if (reply != null) {
                reply.forEach(System.out::println);
            } else {
                System.out.printf("The system failed to respond in time, error: %s", failure.getMessage());
            }

            actorSystem.terminate();
        });
    }
}
