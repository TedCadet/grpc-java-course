package com.tedcadet.grpc.blog.server;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.conversions.Bson;
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
        try {
            Document searchResult = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();

            Blog blogFounded = DocumentToBlog(searchResult);

            // create a response
            BlogResponse response = BlogResponse.newBuilder()
                    .setBlog(blogFounded)
                    .build();

            // return the response
            responseObserver.onNext(response);

            // close the call
            responseObserver.onCompleted();
        } catch (Exception e) {
            //TODO: diversifier les differrents erreur possible avec leur status code a eux
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void updateBlog(BlogRequest request, StreamObserver<BlogResponse> responseObserver) {

        // get the request
        logger.info("request received..");
        Blog blogToUpdate = request.getBlog();

        String blogId = blogToUpdate.getId();


        // update the blog
        logger.info("Searching in db for the blog...");
        try {

            logger.info("updating the blog...");
            Bson filter = eq("_id", new ObjectId(blogId));

            Bson updates = Updates.combine(
                    Updates.set("author_id", blogToUpdate.getAuthorId()),
                    Updates.set("title", blogToUpdate.getTitle()),
                    Updates.set("content", blogToUpdate.getContent())
            );

            UpdateResult updateResult = collection.updateOne(filter,updates);
            Document blogDoc = collection.find(filter).first();

            // create and send the response
            logger.info("sending the updated blog...");
            Blog blogUpdated = DocumentToBlog(blogDoc);

            BlogResponse response = BlogResponse.newBuilder().setBlog(blogUpdated).build();
            responseObserver.onNext(response);

            // close the call
            logger.info("call done...");
            responseObserver.onCompleted();

        } catch (Exception e) {
            //TODO: diversifier les differents erreur possible avec leur status code a eux
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id was not found")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException());
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        try {
            // get the blogId from the request
            logger.info("received a delete request");
            String blogId = request.getBlogId();

            // delete the blog if it exist
            logger.info("deleting blog...");

            Bson query = eq("_id", new ObjectId(blogId));

            DeleteResult result = collection.deleteOne(query);

            // create and send a response
            DeleteBlogResponse response = DeleteBlogResponse.newBuilder().setBlogId(blogId).build();
            responseObserver.onNext(response);

            // close the call
            responseObserver.onCompleted();

        } catch(MongoException e) {
            // send a status not found
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Unable to delete the blog")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }
    }

    private Blog DocumentToBlog(Document doc) {
        return Blog.newBuilder()
                .setId(doc.getObjectId("_id").toString())
                .setAuthorId(doc.getString("author_id"))
                .setTitle(doc.getString("title"))
                .setContent(doc.getString("content"))
                .build();
    }


}
