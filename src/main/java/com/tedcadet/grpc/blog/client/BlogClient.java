package com.tedcadet.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlogClient {

    Logger logger = LoggerFactory.getLogger(BlogClient.class);
    ManagedChannel channel;
    
    public void run() {
        // cree un channel pour communiquer sur localhost:50053
        channel = ManagedChannelBuilder.forAddress("localhost",50053)
                .usePlaintext()
                .build();

        // do the jobs here
//        createBlog();

        readBlog();

        // shutdown the client
        System.out.println("Shutting down client");
        channel.shutdown();
    }

    private void readBlog() {

        // create a new blocking client
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // create a request
        ObjectId id = new ObjectId("62048a137f4e141bdeb8bf76");
        String blogId = id.toString();

        ReadBlogRequest request = ReadBlogRequest.newBuilder().setBlogId(blogId).build();

        // call the service
        logger.info("sending a readBlog request...");
        BlogResponse response = blogClient.readBlog(request);

        Blog blog = response.getBlog();

        logger.info(blog.toString());

    }

    private void createBlog() {

        // create a new blocking client (stub)
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // create a request
        Blog blog = Blog.newBuilder()
                .setAuthorId("Edward")
                .setTitle("First Blog")
                .setContent("We're creating a grpc Service/Client blog!")
                .build();

        BlogRequest request = BlogRequest.newBuilder().setBlog(blog).build();

        // call the service
        BlogResponse response = blogClient.createBlog(request);

        logger.info(response.getBlog().toString());
    }
}
