package br.com.github.guilhermealvessilve.exercise3;

import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(ManagerBehavior.newInstance(), "ManagerBehavior");
        actorSystem.tell(new ManagerBehavior.InstructionCommand("start"));
    }
}
