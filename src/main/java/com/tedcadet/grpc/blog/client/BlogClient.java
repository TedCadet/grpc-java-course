package com.tedcadet.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class BlogClient {

    Logger logger = LoggerFactory.getLogger(BlogClient.class);
    ManagedChannel channel;

    public void run() {
        // cree un channel pour communiquer sur localhost:50053
        channel = ManagedChannelBuilder.forAddress("localhost", 50053)
                .usePlaintext()
                .build();

        //TODO: Remplace les blockingStubs par des Stubs

        // do the jobs here
        String authorId = "Edward";
        String title = "First Blog";
        String content = "We're creating a grpc Service/Client blog! Part 4";
//        createBlog(authorId, title, content);

//        String id = "62048a137f4e141bdeb8bf76";

//        readBlog(id);

//        updateBlog(id);

//        deleteBlog(id);

        listBlogs();

        // shutdown the client
        System.out.println("Shutting down client");
        channel.shutdown();
    }

    private void listBlogs() {

        // Create a Client
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // call the service, get the response and print them
        ListBlogRequest request = ListBlogRequest.newBuilder().build();
        blogClient.listBlog(request).forEachRemaining(printBlog);

    }

    private Consumer<ListBlogResponse> printBlog = blog -> logger.info("blog: " + blog);

    private void deleteBlog(String id) {

        // create a Client
        BlogServiceGrpc.BlogServiceFutureStub blogClient = BlogServiceGrpc.newFutureStub(channel);

        // create a request
        logger.info("Creating the delete request");

        ObjectId objectId = new ObjectId(id);
        String blogId = objectId.toString();

        DeleteBlogRequest request = DeleteBlogRequest
                .newBuilder()
                .setBlogId(blogId)
                .build();

        // call the service and get the response
        logger.info("calling the service");
        DeleteBlogResponse response = null;
        try {
            response = blogClient.deleteBlog(request).get();
            logger.info("blog deleted. id: " + response.getBlogId());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    private void updateBlog(String id) {

        // create a new blocking client
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // create a request
        ObjectId objectId = new ObjectId(id);
        String blogId = objectId.toString();

        Blog blogUpdated = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Edward")
                .setTitle("Lets update a blog!")
                .setContent("we're updating a blog. Oh yeah! Part 2")
                .build();

        BlogRequest request = BlogRequest.newBuilder().setBlog(blogUpdated).build();

        // call the service
        logger.info("calling updateBlog...");
        BlogResponse response = blogClient.updateBlog(request);

        logger.info(response.getBlog().toString());
    }

    private void readBlog(String id) {

        // create a new blocking client
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // create a request
        ObjectId objectId = new ObjectId(id);
        String blogId = objectId.toString();

        ReadBlogRequest request = ReadBlogRequest.newBuilder().setBlogId(blogId).build();

        // call the service
        logger.info("sending a readBlog request...");
        BlogResponse response = blogClient.readBlog(request);

        Blog blog = response.getBlog();

        logger.info(blog.toString());

    }

    private void createBlog(String authorId, String title, String content) {

        // create a new blocking client (stub)
        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        // create a request
        Blog blog = Blog.newBuilder()
                .setAuthorId(authorId)
                .setTitle(title)
                .setContent(content)
                .build();

        BlogRequest request = BlogRequest.newBuilder().setBlog(blog).build();

        // call the service
        BlogResponse response = blogClient.createBlog(request);

        logger.info(response.getBlog().toString());
    }
}
