package com.tedcadet.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        // cree un nouveau grpc server et ajoute un nouveau greetService
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .build();

        // lance le server
        System.out.println("Server started");
        server.start();

        // ajout d'un shutdownhook pour shutdown le server s'il y a un request de shutdown
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            System.out.println("Sucessfully shutown server");
        } ));

        // garde le server up tant qu'il n'a pas ete shutdown
        server.awaitTermination();
    }
}
