package com.github.voxxin.web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.github.voxxin.web.request.FormattedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.voxxin.web.WebServer.PathType.INTERNAL;

public class WebServer {
    protected final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private int port;
    private AbstractRoute errorRoute = null;
    public ArrayList<AbstractRoute> routes = new ArrayList<>();
    private ServerSocket webServer;
    public Thread webServerThread;

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
        webServerThread = new Thread(() -> {
            try {
                if (this.webServer == null) this.webServer = new ServerSocket(port);
                this.LOGGER.info("Started listening on port: {}", port);
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
        });

        webServerThread.start();
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

    public void addPublicDirPath(Class<?> callingClass, String dirPath, String publicPath, PathType pathType, DirectoryPosition directoryPosition) {
        switch (pathType) {
            case INTERNAL:
            case EXTERNAL:
                new PublicFileHandling(callingClass, routes, dirPath, publicPath, pathType, directoryPosition);
                break;
            default:
                LOGGER.error("Invalid path type: {}", pathType);
        }
    }

    public void addPublicDirPath(byte[] bytes, String publicPath) {
        new PublicFileHandling(routes, bytes, publicPath, INTERNAL, DirectoryPosition.CURRENT);
    }

    /**
     * Enum representing the type of path.
     */
    public enum PathType {
        /**
         * Represents an internal path, typically referring to resources within the application.
         */
        INTERNAL,

        /**
         * Represents an external path, typically referring to resources outside the application.
         */
        EXTERNAL;
    }

    /**
     * Enum representing the position of the directory relative to the publicPath.
     */
    public enum DirectoryPosition {
        /**
         * Indicates that the directory should not be included as a subsidiary div.
         */
        NONE,

        /**
         * Indicates that the directory should be included as a subsidiary div at the same level as the publicPath.
         */
        CURRENT,

        /**
         * Indicates that the directory should be included as a subsidiary div within the publicPath.
         */
        SUBDIRECTORY;
    }
}