package com.tedcadet.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;


public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    // mongo configs
    //TODO: externalize the credentials
    private MongoClient mongoClient = MongoClients.create("mongodb://rootuser:rootpass@localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("blog-grpc-db");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {

        // login to db

        // get the request
        logger.info("Received a create Blog request");

        Blog blog = request.getBlog();

        // create a blog in mongodb

        Document blogDoc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());


        logger.info("Inserting blog...");
        collection.insertOne(blogDoc);
        logger.info("blog inserted");

        // we retrieve the MongoDB genrated ID
        String id = blogDoc.getObjectId("_id").toString();

        // return the blog created in mongodb
        logger.info("sending response");
        responseObserver.onNext(BlogResponse.newBuilder()
                .setBlog(blog.toBuilder()
                        .setId(id)
                        .build())
                .build());

        // close the call
        logger.info("request completed");
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<BlogResponse> responseObserver) {

        // get the request
        logger.info("Received a create Blog request");
        String blogId = request.getBlogId();

        // find the blog in the db
        logger.info("Searching in db for the blog...");
        Document searchResult = collection.find(eq("_id", new ObjectId(blogId)))
                .first();

        if(searchResult == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .asRuntimeException());
        } else {

            Blog blogFounded = Blog.newBuilder()
                    .setId(searchResult.getObjectId("_id").toString())
                    .setAuthorId(searchResult.getString("author_id"))
                    .setTitle(searchResult.getString("title"))
                    .setContent(searchResult.getString("title"))
                    .build();

            // create a response
            BlogResponse response = BlogResponse.newBuilder()
                    .setBlog(blogFounded)
                    .build();

            // return the response
            responseObserver.onNext(response);

            // close the call
            responseObserver.onCompleted();
        }
    }
}
