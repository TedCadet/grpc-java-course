package com.tedcadet.grpc.blog.client;

import com.tedcadet.grpc.calculator.client.CalculatorClient;

public class BlogClientApplication {
    public static void main(String[] args) {

        // instancie un nouveau client
        BlogClient blogClient = new BlogClient();

        blogClient.run();
    }
}
