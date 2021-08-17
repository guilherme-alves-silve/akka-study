package br.com.github.guilhermealvessilve.exercise2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ManagerBehavior extends AbstractBehavior<String> {

    public static Behavior<String> newInstance() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private ManagerBehavior(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("start", () -> {
                    for (int i = 0; i < 20; i++) {
                        final ActorRef<String> worker = getContext().spawn(WorkerBehavior.newInstance(), "worker-" + i);
                        worker.tell("start");
                    }
                    return this;
                })
                .build();
    }
}
