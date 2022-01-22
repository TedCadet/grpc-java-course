package com.tedcadet.grpc.greeting.client;

import javax.net.ssl.SSLException;

public class GreetingClientApplication {
    public static void main(String[] args) throws SSLException {
        System.out.println("client started");

        GreetingClient main = new GreetingClient();

        main.run();
    }
}
