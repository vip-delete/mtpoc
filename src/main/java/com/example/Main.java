package com.example;

import com.example.dao.DataStore;
import com.example.dao.DataStoreImpl;
import com.example.web.AccountControllerV1;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.ja
     */
    private static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        ResourceConfig rc = new ResourceConfig()
                .register(AccountControllerV1.class)
                .register(JacksonJsonProvider.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(DataStoreImpl.class).to(DataStore.class);
                    }
                });

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl%nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();
    }
}

