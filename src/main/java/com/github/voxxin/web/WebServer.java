package com.github.voxxin.web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
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

    /**
     * Adds a directory path to be served publicly.
     * @param dirPath The path of the directory to be served.
     * @param publicPath The public path where the directory should be accessible.
     * @param pathType The type of path, either INTERNAL or EXTERNAL.
     * @param directoryPosition The position of the directory relative to the publicPath.
     */
    public void addPublicDirPath(String dirPath, String publicPath, PathType pathType, DirectoryPosition directoryPosition) {
        switch (pathType) {
            case INTERNAL:
            case EXTERNAL:
                processDirPath(dirPath, publicPath, pathType.toString(), directoryPosition);
                break;
            default:
                LOGGER.error("Invalid path type: " + pathType);
        }
    }

    /**
     * Adds a single file to be served publicly.
     * @param dirPath The path of the directory to be served.
     * @param publicPath The public path where the directory should be accessible.
     * @param pathType The type of path, either INTERNAL or EXTERNAL.
     */
    public void addPublicFile(String dirPath, String publicPath, PathType pathType) {
        addPublicDirPath(dirPath, publicPath, pathType, DirectoryPosition.NONE);
    }

    /**
     * Adds a single file to be served publicly.
     * @param bytes The bytes of the file to be served.
     * @param publicPath The public path where the directory should be accessible.
     */
    public void addPublicFile(byte[] bytes, String publicPath) {
        FilePathRoute filePathRoute = new FilePathRoute(bytes, publicPath);
        if (!routes.contains(filePathRoute)) routes.add(filePathRoute);
    }

    /**
     * Processes a directory path recursively.
     * @param dirPath The path of the directory to process.
     * @param publicPath The public path corresponding to the directory.
     * @param type The type of path, either "INTERNAL" or "EXTERNAL".
     * @param directoryPosition The position of the directory relative to the publicPath.
     */
    private void processDirPath(String dirPath, String publicPath, String type, DirectoryPosition directoryPosition) {
        try {
            if (dirPath == null || dirPath.isEmpty()) {
                LOGGER.error("Invalid directory path.");
                return;
            }

            URL resourceURL = WebServer.class.getClassLoader().getResource(dirPath);
            if (resourceURL == null) {
                LOGGER.error("{} directory not found: {}", type, dirPath);
                return;
            }

            Path directory;
            if (type.equals("INTERNAL")) {
                try {
                    directory = Paths.get(resourceURL.toURI());
                } catch (URISyntaxException e) {
                    LOGGER.error("Invalid directory path: {}", dirPath, e);
                    return;
                }
            } else {
                directory = Paths.get(dirPath);
            }

            if (!Files.exists(directory)) {
                LOGGER.error("{} directory not found: {}", type, dirPath);
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        String newPath = dirPath + "/" + path.getFileName() + "/";
                        String newPublicPath = "";

                        if (directoryPosition == DirectoryPosition.CURRENT) newPublicPath = publicPath;
                        else if (directoryPosition == DirectoryPosition.SUBDIRECTORY)
                            newPublicPath = publicPath + path.getFileName() + "/";
                        else continue;

                        processDirPath(newPath, newPublicPath, type, directoryPosition);
                    } else if (Files.isRegularFile(path)) {
                        processPublicFile(path.toFile(), publicPath);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error reading files in {} directory: {}", type.toLowerCase(), dirPath, e);
            }
        } catch (Exception e) {
            LOGGER.error("Error accessing directory path: {}", dirPath, e);
        }
    }

    /**
     * Processes a public file and adds it to the routes.
     * @param file The public file to be processed.
     * @param publicPath The public path corresponding to the file.
     */
    private void processPublicFile(File file, String publicPath) {
        FilePathRoute filePathRoute = new FilePathRoute(file, publicPath);
        if (!routes.contains(filePathRoute)) routes.add(filePathRoute);
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