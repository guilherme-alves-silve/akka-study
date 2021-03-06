package br.com.github.guilhermealvessilve.racinggame.akka.exercise2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {

    private static final double DEFAULT_AVERAGE_SPEED = 48.2;

    private int averageSpeedAdjustmentFactor;
    private Random random;

    private double currentSpeed = 0;

    private Racer(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }

    private Receive<Command> notYetStarted() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message -> {
                    this.random = new Random();
                    this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
                    return running(message.getRaceLength(), 0);
                })
                .onMessage(PositionCommand.class, message -> {
                    message.getController().tell(new RaceController.RacerUpdateCommand(0, getContext().getSelf()));
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> running(int raceLength, int currentPosition) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message -> {
                    determineNextSpeed(raceLength, currentPosition);
                    int newPosition = (int) (currentPosition + getDistanceMovedPerSecond());
                    if (newPosition > raceLength) {
                        newPosition = raceLength;
                    }

                    message.getController().tell(new RaceController.RacerUpdateCommand(newPosition, getContext().getSelf()));
                    if (newPosition == raceLength) {
                        return completed(raceLength);
                    }

                    return running(raceLength, newPosition);
                })
                .build();
    }

    private Receive<Command> completed(int raceLength) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, message -> {
                    message.getController().tell(new RaceController.RacerUpdateCommand(raceLength, getContext().getSelf()));
                    message.getController().tell(new RaceController.RacerFinishCommand(getContext().getSelf()));
                    return waitingToStop();
                })
                .build();
    }

    private Receive<Command> waitingToStop() {
        return newReceiveBuilder()
                .onAnyMessage(message -> Behaviors.same())
                .onSignal(PostStop.class, signal -> {
                    System.out.println("I'm about to terminate!");
                    return Behaviors.same();
                })
                .build();
    }

    private double getMaxSpeed() {
        return DEFAULT_AVERAGE_SPEED * (1 + ((double) averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed(int raceLength, int currentPosition) {
        if (currentPosition < (raceLength / 4)) {
            currentSpeed = currentSpeed  + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        } else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed()) {
            currentSpeed = getMaxSpeed();
        }

        if (currentSpeed < 5) {
            currentSpeed = 5;
        }

        if (currentPosition > (raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }

    public interface Command extends Serializable {

    }

    public static class StartCommand implements Command {

        private static final long serialVersionUID = 1L;
        private final int raceLength;

        public StartCommand(int raceLength) {
            this.raceLength = raceLength;
        }

        public int getRaceLength() {
            return raceLength;
        }
    }

    public static class PositionCommand implements Command {

        private static final long serialVersionUID = 1L;
        private final ActorRef<RaceController.Command> controller;

        public PositionCommand(ActorRef<RaceController.Command> controller) {
            this.controller = controller;
        }

        public ActorRef<RaceController.Command> getController() {
            return controller;
        }
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(Racer::new);
    }
}
