package br.com.github.guilhermealvessilve.generateprimenumbers.exercise1;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FirstSimpleBehavior extends AbstractBehavior<String> {

    public static Behavior<String> newInstance() {
        return Behaviors.setup(FirstSimpleBehavior::new);
    }

    private FirstSimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("hello", () -> {
                    System.out.println("Hello!");
                    return this;
                })
                .onMessageEquals("who are you", () -> {
                    System.out.println("My path is: " + getContext().getSelf().path());
                    return this;
                })
                .onMessageEquals("create a child", () -> {
                    ActorRef<String> secondActor = getContext().spawn(FirstSimpleBehavior.newInstance(), "secondActor");
                    secondActor.tell("who are you");
                    return this;
                })
                .onAnyMessage(message -> {
                    System.out.println("It received the message: " + message);
                    return this;
                })
                .build();
    }
}
