package br.com.github.guilhermealvessilve.blockchainmining.akka.exercise4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

public class MiningSystemBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    private final PoolRouter<ManagerBehavior.Command> managerPoolRouter;
    private final ActorRef<ManagerBehavior.Command> managers;

    private MiningSystemBehavior(ActorContext<ManagerBehavior.Command> context) {
        super(context);
        this.managerPoolRouter = Routers.pool(3,
                Behaviors.supervise(ManagerBehavior.newInstance())
                .onFailure(SupervisorStrategy.restart()));
        this.managers = getContext().spawn(managerPoolRouter, "managerPool");
    }

    @Override
    public Receive<ManagerBehavior.Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(message -> {
                    managers.tell(message);
                    return Behaviors.same();
                })
                .build();
    }

    public static Behavior<ManagerBehavior.Command> newInstance() {
        return Behaviors.setup(MiningSystemBehavior::new);
    }
}
