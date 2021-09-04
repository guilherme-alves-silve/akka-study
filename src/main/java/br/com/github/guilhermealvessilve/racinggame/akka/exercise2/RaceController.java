package br.com.github.guilhermealvessilve.racinggame.akka.exercise2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    private static final int DISPLAY_LENGTH = 160;
    private static final int DEFAULT_CURRENT_POSITION = 100;

    private int raceLength;
    private long start;
    private Map<ActorRef<Racer.Command>, Integer> racerAndCurrentPosition;
    private Map<ActorRef<Racer.Command>, Long> racerAndFinishedTime;
    private Object timerKey;

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, message -> {
                    this.raceLength = DEFAULT_CURRENT_POSITION;
                    this.start = System.currentTimeMillis();
                    this.racerAndCurrentPosition = new HashMap<>();
                    this.racerAndFinishedTime = new HashMap<>();
                    for (int i = 0; i < 10; i++) {
                        final ActorRef<Racer.Command> racer = getContext().spawn(Racer.newInstance(), "racer-" + i);
                        racerAndCurrentPosition.put(racer, 0);
                        racerAndFinishedTime.put(racer, -1L);
                        racer.tell(new Racer.StartCommand(raceLength));
                    }

                    return Behaviors.withTimers(timer -> {
                        timer.startTimerAtFixedRate(timerKey, new GetPositionCommand(), Duration.ofSeconds(1));
                        return Behaviors.same();
                    });
                })
                .onMessage(GetPositionCommand.class, message -> {
                    racerAndCurrentPosition.keySet()
                            .forEach(racer -> racer.tell(new Racer.PositionCommand(getContext().getSelf())));
                    displayRace();
                    return Behaviors.same();
                })
                .onMessage(RacerUpdateCommand.class, message -> {
                    racerAndCurrentPosition.put(message.getRacer(), message.getPosition());
                    return Behaviors.same();
                })
                .onMessage(RacerFinishCommand.class, message -> {
                    racerAndFinishedTime.put(message.getRacer(), System.currentTimeMillis());
                    boolean raceIsOver = racerAndFinishedTime.values()
                            .stream()
                            .allMatch(time -> time > 0);
                    if (raceIsOver) {
                        return raceCompleteMessageHandler();
                    }

                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> raceCompleteMessageHandler() {
        return newReceiveBuilder()
                .onMessage(GetPositionCommand.class, message -> {
                    racerAndCurrentPosition.keySet()
                            .forEach(getContext()::stop);

                    return Behaviors.withTimers(timers -> {
                        displayResults();
                        timers.cancelAll();
                        return Behaviors.stopped();
                    });
                })
                .build();
    }

    private void displayRace() {
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
        System.out.println("    " + "=".repeat(DISPLAY_LENGTH));

        racerAndCurrentPosition.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> System.out.println(getRacersNumber(entry.getKey()) + " : "  + "*".repeat(entry.getValue() * DISPLAY_LENGTH / 100)));
    }

    private void displayResults() {
        System.out.println("Results");

        racerAndFinishedTime.entrySet()
                .stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .forEach(entry -> System.out.println("Racer " + getRacersNumber(entry.getKey()) + " finished in " + ((double) entry.getValue() - start) / 1000 + " seconds."));
    }

    private String getRacersNumber(final ActorRef<Racer.Command> actorRef) {
        final var withoutAddress = actorRef.path().toStringWithoutAddress();
        return withoutAddress.substring(withoutAddress.length() - 1);
    }

    public static Behavior<Command> newInstance() {
        return Behaviors.setup(RaceController::new);
    }

    public interface Command extends Serializable {

    }

    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
    }

    public static class RacerUpdateCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final int position;
        private final ActorRef<Racer.Command> racer;

        public RacerUpdateCommand(final int position,
                                  final ActorRef<Racer.Command> racer) {
            this.position = position;
            this.racer = racer;
        }

        public int getPosition() {
            return position;
        }

        public ActorRef<Racer.Command> getRacer() {
            return racer;
        }
    }

    private static class GetPositionCommand implements Command {

        private static final long serialVersionUID = 1L;
    }

    public static class RacerFinishCommand implements Command {

        private static final long serialVersionUID = 1L;

        private final ActorRef<Racer.Command> racer;

        public RacerFinishCommand(final ActorRef<Racer.Command> racer) {
            this.racer = racer;
        }

        public ActorRef<Racer.Command> getRacer() {
            return racer;
        }
    }
}
