package com.github.voxxin.web;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

import static com.github.voxxin.web.FilePathRoute.LOGGER;
import static com.github.voxxin.web.WebServer.DirectoryPosition.CURRENT;
import static com.github.voxxin.web.WebServer.DirectoryPosition.SUBDIRECTORY;

class PublicFileHandling {
    private final ArrayList<AbstractRoute> routes;
    public final WebServer.PathType pathType;
    public final WebServer.DirectoryPosition directoryPosition;

    public PublicFileHandling(ArrayList<AbstractRoute> routes, byte[] bytes, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;

        addPublicFile(bytes, publicPath);
    }

    public PublicFileHandling(ArrayList<AbstractRoute> routes, String filePath, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;

        handleFile(filePath, publicPath);
    }


    public void handleFile(String filePath, String publicPath) {
        try {
            Path tempDirPath = Files.createTempDirectory("temporaryDirectoryWebConfig");
            if (pathType == WebServer.PathType.INTERNAL) {
                handleDirectoryStructure(filePath, tempDirPath.toFile());
            } else {
                handleDirectory(Paths.get(filePath).toFile(), publicPath);
            }
            handleDirectory(tempDirPath.toFile(), publicPath);
        } catch (IOException e) {
            LOGGER.error("Error handling file: " + filePath, e);
            throw new RuntimeException("Error handling file: " + e.getMessage(), e);
        }
    }

    private void handleDirectoryStructure(String pathStart, File outputFileDir) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathStart)) {
            if (inputStream == null) throw new FileNotFoundException("Resource not found: " + pathStart);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                Path targetPath = outputFileDir.toPath().resolve(line);

                if (line.contains(".")) {
                    // Create file
                    try (InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(pathStart + line)) {
                        if (fileInputStream == null) throw new FileNotFoundException("Resource not found: " + pathStart + line);
                        Files.copy(fileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    // Create directory
                    Files.createDirectories(targetPath);
                    handleDirectoryStructure(pathStart + line + "/", targetPath.toFile());
                }
            }
        }
    }

    private void handleDirectory(File file, String publicPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.toPath())) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    String newPath = file.toPath() + "/" + path.getFileName() + "/";
                    String newPublicPath = "";

                    if (directoryPosition == CURRENT) {
                        newPublicPath = publicPath;
                    } else if (directoryPosition == SUBDIRECTORY) {
                        newPublicPath = publicPath + path.getFileName() + "/";
                    } else {
                        continue;
                    }

                    handleDirectory(Paths.get(newPath).toFile(), newPublicPath);
                } else if (Files.isRegularFile(path)) {
                    byte[] fileBytes = Files.readAllBytes(path);
                    addPublicFile(fileBytes, publicPath + path.toFile().getName());
                    Files.delete(path);
                }
            }
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOGGER.error("Error processing directory: {}", file.getPath(), e);
        }
    }


    private void addPublicFile(byte[] bytes, String publicPath) {
        FilePathRoute filePathRoute = new FilePathRoute(bytes, publicPath);
        if (!routes.contains(filePathRoute)) routes.add(filePathRoute);
    }
}
