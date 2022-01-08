package com.dillon;

import org.slf4j.Logger;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.Source;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws InterruptedException {

        ActorSystem<Void> actorSystem = ActorSystem.create(App.create(), "ActorSystem");

        Logger logger = actorSystem.log();
        logger.info("Hello world!");

        Source<Integer, NotUsed> source = Source.range(1, 10);
        source.runForeach(i -> logger.info(i.toString()), actorSystem);

        // Thread.sleep(10000L);

        actorSystem.terminate();

    }

    public static Behavior<Void> create() {
        return Behaviors.setup(
                contex -> {
                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

}
