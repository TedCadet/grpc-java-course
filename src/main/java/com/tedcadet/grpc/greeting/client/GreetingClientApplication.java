package com.tedcadet.grpc.greeting.client;

public class GreetingClientApplication {
    public static void main(String[] args) {
        System.out.println("client started");

        GreetingClient main = new GreetingClient();

        main.run();
    }
}
