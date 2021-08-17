package br.com.github.guilhermealvessilve.exercise2;

import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<String> actorSystem = ActorSystem.create(ManagerBehavior.newInstance(), "ManagerBehavior");
        actorSystem.tell("start");
    }
}
