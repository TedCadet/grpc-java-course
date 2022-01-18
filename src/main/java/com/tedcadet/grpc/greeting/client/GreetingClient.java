package com.tedcadet.grpc.greeting.client;

import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GreetingClient {

    ManagedChannel channel;
    GreetServiceGrpc.GreetServiceStub greetAsyncClient;
    GreetServiceGrpc.GreetServiceBlockingStub greetBlockingClient;

    public void run() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

//        doUnaryCall();

//        doServerStreamingCall();
        
//        doClientStreamingCall();
        
//        doBiDiStreamingCall();

        doDeadline();

        // shutdown the client
        System.out.println("Shutting down client");
        channel.shutdown();
    }

    private void doDeadline() {

        greetBlockingClient = GreetServiceGrpc.newBlockingStub(channel);

        System.out.println("add a 3000ms deadline to client");
        // get response
        try {
            GreetWithDeadlineResponse response = greetBlockingClient.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder().setFirstName("Edward").build())
                                    .build());

            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceed");
            } else {
                e.printStackTrace();
            }
        }

        System.out.println("add a 100ms deadline to client");
        // get response
        try {
            GreetWithDeadlineResponse response = greetBlockingClient.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(
                            GreetWithDeadlineRequest.newBuilder()
                                    .setGreeting(Greeting.newBuilder().setFirstName("Edward").build())
                                    .build());

            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline exceed");
            } else {
                e.printStackTrace();
            }
        }

    }

    private void doBiDiStreamingCall() {
        System.out.println("Client Bi-Directional call / greet everyone");
        
        // create a new async/non-blocking client
        greetAsyncClient = GreetServiceGrpc.newStub(channel);

        // create a latch for asynchronous
        CountDownLatch latch = new CountDownLatch(1);

        // create a requestObserver
        StreamObserver<GreetEveryoneRequest> requestObserver = greetAsyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                // everytime we receive a response
                System.out.println("response from server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        // create requests
        Arrays.asList("Edward", "Andy", "Dina").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(
                            GreetEveryoneRequest.newBuilder()
                                    .setGreeting(
                                            Greeting.newBuilder()
                                                    .setFirstName(name)
                                                    .build())
                                    .build());

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        // complete the call on the requestObserver
        requestObserver.onCompleted();

        // wait for 3 seconds that the latch get to 0
        try {
            latch.await(3L,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doClientStreamingCall() {

        System.out.println("Client Streaming call / Sends many requests and receive one response");

        // create a new async/non-blocking client
        greetAsyncClient = GreetServiceGrpc.newStub(channel);

        // create a latch for asynchronous
        CountDownLatch latch = new CountDownLatch(1);

        // create a requestObserver
        StreamObserver<LongGreetRequest> requestObserver = greetAsyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get a response from the server
                // onNext will be called only once
                System.out.println("received a response from server");
                System.out.println("received a response:\n" + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                // we get an error from server
            }

            @Override
            public void onCompleted() {
                // server is done and send us data
                // onCompleted will be called right after onNext
                System.out.println("Server has completed the call");
                latch.countDown();
            }
        });


        List<List<String>> names = Stream.of(Arrays.asList("Edward","Cadet"),
                Arrays.asList("Andy","Cadet"),
                Arrays.asList("Dina","Cadet"),
                Arrays.asList("Eddy","Cadet"),
                Arrays.asList("Sainte-Anne","Cadet"),
                Arrays.asList("Olga","Cadet"))
                .collect(Collectors.toList());

        // make a request for every name in the list of names
        names.forEach(name -> {
            String firstName = name.get(0);
            String lastName = name.get(1);

            System.out.println("Sending name: " + firstName + " " + lastName);
            requestObserver.onNext(LongGreetRequest
                    .newBuilder()
                    .setGreeting(Greeting
                            .newBuilder()
                            .setFirstName(firstName)
                            .setLastName(lastName)
                            .build())
                    .build());
        });

        // we tell the server that we're done
        requestObserver.onCompleted();

        try {
            // va attendre 3 seconds que le countdown du latch soit a zero
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void doServerStreamingCall() {

        System.out.println("Server streaming call / Receives many greets with one call");

        // create a new blocking client
        greetBlockingClient = GreetServiceGrpc.newBlockingStub(channel);

        // Server Streaming
        // create a greeting
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Edward")
                .setLastName("Cadet")
                .build();

        // create a request
        GreetManyTimesRequest gr = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // get the responses and print them
        greetBlockingClient.greetManyTimes(gr).forEachRemaining(response -> {
            System.out.println(response.getResult());
        });
    }

    private void doUnaryCall() {

        System.out.println("Unary Call / single greet");

        // create a new blocking client
        greetBlockingClient = GreetServiceGrpc.newBlockingStub(channel);

        // Unary
        // set a greeting
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Edward")
                .setLastName("Cadet")
                .build();

        // set a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // make the rpc call
        GreetResponse greetResponse = greetBlockingClient.greet(greetRequest);
        System.out.println(greetResponse.getResult());
    }
}
