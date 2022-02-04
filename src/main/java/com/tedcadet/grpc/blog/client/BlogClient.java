package com.tedcadet.grpc.blog.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    
    ManagedChannel channel;
    
    public void run() {
        // cree un channel pour communiquer sur localhost:50053
        channel = ManagedChannelBuilder.forAddress("localhost",50053)
                .usePlaintext()
                .build();

        // do the jobs here

        // shutdown the client
        System.out.println("Shutting down client");
        channel.shutdown();
    }
}
