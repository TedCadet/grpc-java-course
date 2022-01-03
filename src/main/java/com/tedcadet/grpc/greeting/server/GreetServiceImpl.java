package com.tedcadet.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
//        super.greet(request, responseObserver);
        // get greeting from request
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        // set response
        String result = "Hello " + firstName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send response
        responseObserver.onNext(response);

        // complete the rpc call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        // get greeting from request
        String firstName = request.getGreeting().getFirstName();
//        String lastName = request.getGreeting().getLastName();

        // set responses
        try {
            for(int i = 0; i < 10; i++){
                String result = "Hello " + firstName + ", response number: " + i;

                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();

                // send response
                responseObserver.onNext(response);

                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        StreamObserver<LongGreetRequest> requestObserver = new StreamObserver<LongGreetRequest>() {

            String result = "";

            @Override
            public void onNext(LongGreetRequest value) {
                // client sends a message
                result += "Hello " + value.getGreeting().getFirstName() + "!\n";
            }

            @Override
            public void onError(Throwable t) {
                // sends an error
            }

            @Override
            public void onCompleted() {
                // client is done
                responseObserver.onNext(LongGreetResponse
                        .newBuilder()
                        .setResult(result)
                        .build());

                // this is where we want to return a response
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {

        StreamObserver<GreetEveryoneRequest> requestObserver = new StreamObserver<GreetEveryoneRequest>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {

                String response = "Hello " + value.getGreeting().getFirstName();

                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse
                        .newBuilder()
                        .setResult(response)
                        .build();

                responseObserver.onNext(greetEveryoneResponse);

            }

            @Override
            public void onError(Throwable t) {
                // do nothing for now
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };

        return requestObserver;
    }
}
