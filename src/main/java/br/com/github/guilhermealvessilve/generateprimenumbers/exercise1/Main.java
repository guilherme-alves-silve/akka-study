package br.com.github.guilhermealvessilve.generateprimenumbers.exercise1;

import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<String> actorSystem = ActorSystem.create(FirstSimpleBehavior.newInstance(), "FirstSimpleBehavior");
        actorSystem.tell("hello");
        actorSystem.tell("who are you");
        actorSystem.tell("create a child");
        actorSystem.tell("Hello! Are you there?");
        actorSystem.tell("Teste");
    }
}
