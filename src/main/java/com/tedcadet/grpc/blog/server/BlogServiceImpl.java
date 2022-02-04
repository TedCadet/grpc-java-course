package com.tedcadet.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    // mongo configs
    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("blog-grpc-db");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

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
        responseObserver.onNext(CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder()
                        .setId(id)
                        .build())
                .build());

        // close the call
        logger.info("request completed");
        responseObserver.onCompleted();
    }
}
