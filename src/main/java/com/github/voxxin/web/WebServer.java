package com.github.voxxin.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.github.voxxin.web.request.FormattedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private int port;
    private AbstractRoute errorRoute = null;
    public ArrayList<AbstractRoute> routes = new ArrayList<>();
    private ServerSocket webServer;

    public WebServer(int port) {
        this.port = port;
    }

    public WebServer(int port, AbstractRoute... routes) {
        this.port = port;
        this.routes.addAll(Arrays.asList(routes));
    }

    /**
     * Start the web server.
     */
    public void start() {
        try {
            if (this.webServer == null) this.webServer = new ServerSocket(port);
            this.LOGGER.info("Started listening on port: " + port);

            while (!this.webServer.isClosed()) {
                try (Socket clientSocket = this.webServer.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {

                    List<String> headers = new ArrayList<>();
                    String line;
                    int contentLength = -1;
                    while ((line = in.readLine()) != null && !line.isEmpty()) {
                        headers.add(line);
                        if (line.startsWith("Content-Length: ")) {
                            contentLength = Integer.parseInt(line.substring(16).trim());
                        }
                    }

                    if (contentLength > 0) {
                        char[] buffer = new char[contentLength];
                        in.read(buffer);
                        headers.add(0, String.valueOf(buffer));
                    }

                    FormattedRequest formattedRequest = !headers.isEmpty() ? new FormattedRequest(headers) : null;

                    this.routes.stream()
                            .filter(r -> formattedRequest != null && r.route.equals(formattedRequest.getPath()))
                            .findAny()
                            .ifPresentOrElse(
                                    r -> r.handleRequest(formattedRequest, clientSocket),
                                    () -> {
                                        if (errorRoute != null) {
                                            errorRoute.handleRequest(formattedRequest, clientSocket);
                                        }
                                    }
                            );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the web server.
     */
    public void close() {
        try {
            if (webServer != null && !webServer.isClosed()) {
                webServer.close();
                webServer = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the port number the server is listening on.
     *
     * @return The port number.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Change the port number the server is listening on.
     *
     * @param newPort The new port number.
     */
    public void changePort(int newPort) {
        if (this.port != newPort) {
            port = newPort;
            this.close();
            this.start();
        }
    }

    /**
     * Set the error route for the server.
     *
     * @param errorRoute The error route.
     */
    public void errorPage(AbstractRoute errorRoute) {
        this.errorRoute = errorRoute;
    }
}