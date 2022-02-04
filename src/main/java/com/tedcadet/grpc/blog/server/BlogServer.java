package com.tedcadet.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args)  throws IOException, InterruptedException {

        final Logger logger = LoggerFactory.getLogger(BlogServer.class);

        // cree un nouveau grpc server et ajoute le blogService
        Server server = ServerBuilder.forPort(50053)
                .addService(new BlogServiceImpl())
                .build();

        // lance le server
        logger.info("Blog Server started");
        server.start();

        // ajout d'un shutdownhook pour fermer le server s'il y a un request de shutdown
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Received shutdown signal");
            server.shutdown();
            logger.info("shutdown completed");
        }));

        // garde le server up tant qu<il n'a pas recu un signal de shutdown
        server.awaitTermination();


    }
}
