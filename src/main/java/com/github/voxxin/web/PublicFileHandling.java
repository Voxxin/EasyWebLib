package com.github.voxxin.web;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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
            final File jarFile = new File(enclosingClass.getProtectionDomain().getCodeSource().getLocation().getPath());

            if (jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory() || !entry.getName().startsWith(pathStart)) continue;
                        if (!paths.contains(entry.getName())) {
                            paths.add(entry.getName());
                        }
                    }
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
                String fileName = pathWithoutOriginal.split("/")[pathWithoutOriginal.split("/").length - 1];
                addPublicFile(isInternal ? getInternalFile(route) : Files.readAllBytes(new File(route).toPath()), publicPath + fileName);
            }

            ArrayList<String> split = new ArrayList<>(Arrays.asList(pathWithoutOriginal.split("/")));
            if (split.get(split.size() - 1).contains(".")) split.remove(split.size() - 1);

            if (directoryPosition == WebServer.DirectoryPosition.NONE && !split.isEmpty()) continue;
            String sb = String.join("/", split);
            if (!sb.isEmpty()) sb = sb+"/";

            String fileName = pathWithoutOriginal.split("/")[pathWithoutOriginal.split("/").length - 1];
            addPublicFile(isInternal ? getInternalFile(route) : Files.readAllBytes(new File(route).toPath()), publicPath + sb + fileName);
        }
    }

    public byte[] getInternalFile(String filePath) throws IOException, URISyntaxException {
        final File jarFile = new File(enclosingClass.getProtectionDomain().getCodeSource().getLocation().getPath());
        byte[] bytes = null;
        if (jarFile.isFile()) {
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String path = entry.getName();
                    if (path.equals(filePath)) {
                        File file = File.createTempFile("tempFile", "."+path.replaceAll("^.*\\.", ""));

                        try (InputStream inputStream = jar.getInputStream(entry)) {
                            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }

                        bytes = Files.readAllBytes(file.toPath());
                        file.delete();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading JAR file: " + e.getMessage());
            }
        } else {
            URL url = enclosingClass.getProtectionDomain().getClassLoader().getResource(filePath);
            if (url == null) return null;
            File file = new File(url.toURI());
            bytes = Files.readAllBytes(file.toPath());
        }

        return bytes;
    }

    private void addPublicFile(byte[] bytes, String publicPath) {
        FilePathRoute filePathRoute = new FilePathRoute(bytes, publicPath);
        if (!routes.contains(filePathRoute)) {
            routes.add(filePathRoute);
        }
    }
}

