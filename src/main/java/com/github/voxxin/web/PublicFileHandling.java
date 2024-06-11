package com.github.voxxin.web;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.voxxin.web.FilePathRoute.LOGGER;

class PublicFileHandling {
    private final Class<?> enclosingClass;
    private final ArrayList<AbstractRoute> routes;
    public final WebServer.PathType pathType;
    public final WebServer.DirectoryPosition directoryPosition;

    public PublicFileHandling(ArrayList<AbstractRoute> routes, byte[] bytes, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.enclosingClass = this.getClass();
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;

        addPublicFile(bytes, publicPath);
    }

    public PublicFileHandling(Class<?> enclosingClass, ArrayList<AbstractRoute> routes, String filePath, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.enclosingClass = enclosingClass == null ? this.getClass() : enclosingClass;
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
                return;
            }

            handleDirectory(Paths.get(tempDirPath.toFile().getPath() + (filePath.startsWith("/") ? "" : "/") + filePath).toFile(), publicPath);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error handling file: {}", filePath, e);
            throw new RuntimeException("Error handling file: " + e.getMessage(), e);
        }
    }

    private void handleDirectoryStructure(String pathStart, File outputFileDir) throws IOException, URISyntaxException {

        final File jarFile = new File(enclosingClass.getProtectionDomain().getCodeSource().getLocation().getPath());

        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!pathStart.endsWith("/")) {
                        pathStart += "/";
                    }

                    if (name.startsWith(pathStart) && !entry.isDirectory()) {
                        Path targetPath = outputFileDir.toPath().resolve(name);

                        try (InputStream fileInputStream = jar.getInputStream(entry)) {
                            if (fileInputStream != null) {
                                if (!Files.exists(targetPath.getParent())) {
                                    Files.createDirectories(targetPath.getParent());
                                }
                                Files.copy(fileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
            }
        } else { // Inside an IDE -- AKA direct access to jar files
            // Create the directory files
            Path targetPath = outputFileDir.toPath().resolve(pathStart);
            Files.createDirectories(targetPath);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(pathStart)))) {
                String finalPathStart = pathStart;
                reader.lines().forEach(line -> {
                    Path fileOrDirPath = targetPath.resolve(line);
                    try {
                        if (line.contains(".")) {
                            try (InputStream fileInputStream = getClass().getClassLoader().getResourceAsStream(finalPathStart + line)) {
                                if (fileInputStream == null)
                                    throw new FileNotFoundException("Resource not found: " + finalPathStart + line);
                                Files.copy(fileInputStream, fileOrDirPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } else {
                            Files.createDirectories(fileOrDirPath);
                            handleDirectoryStructure(finalPathStart + line + "/", fileOrDirPath.toFile());
                        }
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


        private void handleDirectory(File file, String publicPath) {
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
        if (!routes.contains(filePathRoute)) {
            routes.add(filePathRoute);
        }
    }
}

