package com.github.voxxin.web;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Objects;

import static com.github.voxxin.web.FilePathRoute.LOGGER;

public class PublicFileHandling {
    private final Thread webServerThread;
    private final ArrayList<AbstractRoute> routes;
    public final WebServer.PathType pathType;
    public final WebServer.DirectoryPosition directoryPosition;

    public PublicFileHandling(Thread webServerThread, ArrayList<AbstractRoute> routes, byte[] bytes, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.webServerThread = webServerThread;
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;

        addPublicFile(bytes, publicPath);
    }

    public PublicFileHandling(Thread webServerThread, ArrayList<AbstractRoute> routes, String filePath, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        System.out.println("Constructor called with filePath: " + filePath);
        this.webServerThread = webServerThread;
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;

        handleFile(filePath, publicPath);
    }

    public void handleFile(String filePath, String publicPath) {
        System.out.println("handleFile called with filePath: " + filePath + " and publicPath: " + publicPath);
        try {
            Path tempDirPath = Files.createTempDirectory("temporaryDirectoryWebConfig");
            System.out.println("Temporary directory created at: " + tempDirPath.toString());
            if (pathType == WebServer.PathType.INTERNAL) {
                handleDirectoryStructure(filePath, tempDirPath.toFile());
            } else {
                handleDirectory(Paths.get(filePath).toFile(), publicPath);
            }
            handleDirectory(tempDirPath.toFile(), publicPath);
        } catch (IOException e) {
            LOGGER.error("Error handling file: {}", filePath, e);
            System.err.println("Error handling file: " + filePath + ", exception: " + e.getMessage());
            throw new RuntimeException("Error handling file: " + e.getMessage(), e);
        }
    }

    private void handleDirectoryStructure(String pathStart, File outputFileDir) throws IOException {
        System.out.println("handleDirectoryStructure called with pathStart: " + pathStart + " and outputFileDir: " + outputFileDir.getPath());

        // Get resource URL
        URL url = Thread.currentThread().getContextClassLoader().getResource(pathStart);
        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + pathStart);
        }

        File file = new File(url.getPath());
        for (File files : file.listFiles()) {
            System.out.println("File: " + files.getName());
        }

//        try {
//            Path sourcePath = Paths.get(url.toURI());
//            Files.walk(sourcePath)
//                    .forEach(source -> {
//                        try {
//                            Path target = outputFileDir.toPath().resolve(sourcePath.relativize(source));
//                            if (Files.isDirectory(source)) {
//                                Files.createDirectories(target);
//                            } else {
//                                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
//                                System.out.println("File copied from: " + source + " to: " + target);
//                            }
//                        } catch (IOException e) {
//                            System.err.println("Error copying file: " + e.getMessage());
//                        }
//                    });
//        } catch (URISyntaxException e) {
//            throw new IOException("Invalid URI: " + e.getMessage());
//        }



//        try (InputStream inputStream = url.openStream()) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//
//            // Read each line from the resource
//            while ((line = reader.readLine()) != null) {
//                System.out.println("Reading line: " + line);
//                Path targetPath = outputFileDir.toPath().resolve(line);
//
//                if (line.contains(".")) {
//                    // Create file
//                    try (InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(pathStart + line)) {
//                        if (fileInputStream == null) {
//                            System.err.println("File not found in resource: " + pathStart + line);
//                            continue;
//                        }
//                        Files.copy(fileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
//                        System.out.println("File created at: " + targetPath.toString());
//                    }
//                } else {
//                    // Create directory
//                    Files.createDirectories(targetPath);
//                    System.out.println("Directory created at: " + targetPath.toString());
//                    // Recursively handle the directory structure
//                    handleDirectoryStructure(pathStart + line + "/", targetPath.toFile());
//                }
//            }
//        }
    }


    private void handleDirectory(File file, String publicPath) {
        System.out.println("handleDirectory called with file: " + file.getPath() + " and publicPath: " + publicPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.toPath())) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    String newPath = file.toPath() + "/" + path.getFileName() + "/";
                    String newPublicPath = "";

                    if (directoryPosition == WebServer.DirectoryPosition.CURRENT) {
                        newPublicPath = publicPath;
                    } else if (directoryPosition == WebServer.DirectoryPosition.SUBDIRECTORY) {
                        newPublicPath = publicPath + path.getFileName() + "/";
                    } else {
                        continue;
                    }

                    System.out.println("Recursing into directory: " + newPath);
                    handleDirectory(Paths.get(newPath).toFile(), newPublicPath);
                } else if (Files.isRegularFile(path)) {
                    byte[] fileBytes = Files.readAllBytes(path);
                    System.out.println("Adding public file: " + publicPath + path.toFile().getName());
                    addPublicFile(fileBytes, publicPath + path.toFile().getName());
                    Files.delete(path);
                    System.out.println("File deleted: " + path.toString());
                }
            }
            Files.delete(file.toPath());
            System.out.println("Directory deleted: " + file.toPath().toString());
        } catch (IOException e) {
            LOGGER.error("Error processing directory: {}", file.getPath(), e);
            System.err.println("Error processing directory: " + file.getPath() + ", exception: " + e.getMessage());
        }
    }

    private void addPublicFile(byte[] bytes, String publicPath) {
        System.out.println("addPublicFile called with publicPath: " + publicPath);
        FilePathRoute filePathRoute = new FilePathRoute(bytes, publicPath);
        if (!routes.contains(filePathRoute)) {
            routes.add(filePathRoute);
            System.out.println("FilePathRoute added: " + publicPath);
        }
    }
}

