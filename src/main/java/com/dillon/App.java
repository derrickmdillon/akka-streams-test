package com.dillon;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws InterruptedException {

        ActorSystem<Void> actorSystem = ActorSystem.create(App.create(), "ActorSystem");

        Logger logger = actorSystem.log();
        var fileSource = FileIO.fromPath(Path.of("src\\main\\resources\\test.txt"));
        
        RunnableGraph<CompletableFuture<String>> fileStream = fileSource
        .via(Framing.delimiter(ByteString.fromString("\r\n"), 1000, FramingTruncation.ALLOW).named("Split"))
        .via(Flow.of(ByteString.class).map(i -> {
            logger.info(i.utf8String());
            return i;
        }))
        .toMat(Sink.fold(0, (agg, i) -> agg + 1), Keep.right())
        .mapMaterializedValue((matResults) -> {
            CompletableFuture<String> stringResult = new CompletableFuture<>();
            matResults.whenComplete((result, error) -> {
                var count = Integer.toString(result);
                stringResult.complete("Count: " + count);
            });
            return stringResult;
        });

        var fileResult = fileStream.run(actorSystem);

        fileResult.whenComplete((result, error) -> {
            logger.info("Result: {}", result);
        });

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
