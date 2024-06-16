package com.github.voxxin.web;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class PublicFileHandling {
    private final String filePath;
    private final Class<?> enclosingClass;
    private final ArrayList<AbstractRoute> routes;
    public final WebServer.PathType pathType;
    public final WebServer.DirectoryPosition directoryPosition;

    public PublicFileHandling(ArrayList<AbstractRoute> routes, byte[] bytes, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.enclosingClass = this.getClass();
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;
        this.filePath = "";

        addPublicFile(bytes, publicPath);
    }

    public PublicFileHandling(Class<?> enclosingClass, ArrayList<AbstractRoute> routes, String filePath, String publicPath, WebServer.PathType pathType, WebServer.DirectoryPosition directoryPosition) {
        this.enclosingClass = enclosingClass == null ? this.getClass() : enclosingClass;
        this.routes = routes;
        this.pathType = pathType;
        this.directoryPosition = directoryPosition;
        this.filePath = filePath;

        handleFile(filePath, publicPath);
    }

    public void handleFile(String filePath, String publicPath) {
        try {
            handleDirectory(handleDirectoryStructure(filePath), publicPath);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> handleDirectoryStructure(String pathStart) throws IOException {
        List<String> paths = new ArrayList<>();

        if (pathType == WebServer.PathType.INTERNAL) {
            File jarFile = new File(enclosingClass.getProtectionDomain().getCodeSource().getLocation().getPath());

            if (jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    jar.stream()
                            .filter(entry -> !entry.isDirectory() && entry.getName().startsWith(pathStart))
                            .map(JarEntry::getName)
                            .forEach(paths::add);
                } catch (IOException e) {
                    System.err.println("Error reading JAR file: " + e.getMessage());
                }
            } else { // Running from IDEs
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathStart);
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        reader.lines()
                                .forEach(p -> {
                                    String formattedP = pathStart + p;
                                    if (p.contains(".")) {
                                        if (!paths.contains(formattedP)) {
                                            paths.add(formattedP);
                                        }
                                    } else {
                                        try {
                                            paths.addAll(handleDirectoryStructure(formattedP));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            Files.walk(Paths.get(pathStart))
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .forEach(path -> {
                        String formattedP = path.replaceAll("^.*" + pathStart, pathStart + path);
                        if (!paths.contains(formattedP)) {
                            paths.add(formattedP);
                        }
                    });
        }

        return paths;
    }

    private void handleDirectory(List<String> routes, String publicPath) throws URISyntaxException, IOException {
        for (String route : routes) {
            boolean isInternal = pathType == WebServer.PathType.INTERNAL;
            String pathWithoutOriginal = route.replaceFirst("^.*" + filePath, "");
            if (!pathWithoutOriginal.contains(".")) continue; // Skip directories

            if (directoryPosition == WebServer.DirectoryPosition.CURRENT) {
                URL url = isInternal ? enclosingClass.getClassLoader().getResource(route) : new URL(route);
                if (url == null) continue;
                File file = new File(url.toURI());
                addPublicFile(Files.readAllBytes(file.toPath()), publicPath + file.getName());
            }

            ArrayList<String> split = new ArrayList<>(Arrays.asList(pathWithoutOriginal.split("/")));
            if (split.get(split.size() - 1).contains(".")) split.remove(split.size() - 1);

            if (directoryPosition == WebServer.DirectoryPosition.NONE && !split.isEmpty()) continue;
            String sb = String.join("/", split);

            URL url = isInternal ? enclosingClass.getClassLoader().getResource(route) : new URL(route);
            if (url == null) continue;
            File file = new File(url.toURI());
            addPublicFile(Files.readAllBytes(file.toPath()), publicPath + sb + file.getName());
        }
    }

    private void addPublicFile(byte[] bytes, String publicPath) {
        FilePathRoute filePathRoute = new FilePathRoute(bytes, publicPath);
        if (!routes.contains(filePathRoute)) {
            routes.add(filePathRoute);
        }
    }
}

