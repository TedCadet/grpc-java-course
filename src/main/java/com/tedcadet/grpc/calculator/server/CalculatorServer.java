package com.tedcadet.grpc.calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        // cree un nouveau grpc server et ajoute un nouveau greetService sur le port 50052

        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance()) // reflection
                .build();

        // lance le server
        System.out.println("Calculator Server started");
        server.start();

        // ajout d'un shutdownhook pour shutdown le server s'il y a un request de shutdown
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received shutdown signal");
            server.shutdown();
            System.out.println("shutdown completed");
        }));

        // garde le server up tant qu'il n'a pas ete shutdown
        server.awaitTermination();
    }
}
