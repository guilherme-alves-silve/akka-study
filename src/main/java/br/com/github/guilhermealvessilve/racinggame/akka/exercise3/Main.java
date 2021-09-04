package br.com.github.guilhermealvessilve.racinggame.akka.exercise3;

import akka.actor.typed.ActorSystem;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<RaceController.Command> raceController = ActorSystem.create(RaceController.newInstance(), "RaceSimulation");
        raceController.tell(new RaceController.StartCommand());
    }
}
