package com.github.voxxin.web;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.github.voxxin.web.FilePathRoute.LOGGER;

public class PublicFileHandling {
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
                return;
            }

            handleDirectory(Paths.get(tempDirPath.toFile().getPath() + (filePath.startsWith("/") ? "" : "/") + filePath).toFile(), publicPath);
        } catch (IOException e) {
            LOGGER.error("Error handling file: {}", filePath, e);
            throw new RuntimeException("Error handling file: " + e.getMessage(), e);
        }
    }

    private void handleDirectoryStructure(String pathStart, File outputFileDir) throws IOException {
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!pathStart.endsWith("/")) {
                        pathStart += "/";
                    }

                    String[] directories = pathStart.split("/");
                    Path targetPath = outputFileDir.toPath();
                    for (String directory : directories) {
                        targetPath = targetPath.resolve(directory);
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    }

                    if (name.startsWith(pathStart) && !entry.isDirectory()) {
                        targetPath = outputFileDir.toPath().resolve(name);

                        try (InputStream fileInputStream = jar.getInputStream(entry)) {
                            if (fileInputStream == null) {
                                continue;
                            }
                            if (!Files.exists(targetPath.getParent())) {
                                Files.createDirectories(targetPath.getParent());
                            }
                            Files.copy(fileInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
    }

    private void handleDirectory(File file, String publicPath) {
        System.out.println(file.toPath().toString());

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

