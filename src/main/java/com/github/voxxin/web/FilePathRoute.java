package com.github.voxxin.web;

import com.github.voxxin.web.request.FormattedRequest;
import com.github.voxxin.web.request.FormattedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import java.io.*;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FilePathRoute extends AbstractRoute {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePathRoute.class);
    private final String route;
    private final File file;
    private final byte[] bytes;

    public FilePathRoute(File file, String route) {
        super(route + file.getName());
        this.route = route;
        this.file = file;
        this.bytes = null;
    }

    public FilePathRoute(byte[] bytes, String route) {
        super(route + getFileNameAndExtension(route)[0]);
        this.route = route;
        this.bytes = bytes;
        this.file = null;
    }

    @Override
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {
        if (file != null) {
            try (InputStream fileInputStream = new FileInputStream(file)) {
                writeResponse(fileInputStream, outputStream, Files.probeContentType(file.toPath()));
            } catch (IOException e) {
                LOGGER.error("Error occurred while handling file request for {}: {}", file.getName(), e.getMessage());
            }
        } else {
            try {
                Path tempFile = Files.createTempFile("tempImageWebconfig", "." + getFileNameAndExtension(route)[1]);
                Files.write(tempFile, bytes);
                try (InputStream inputStream = Files.newInputStream(tempFile)) {
                    writeResponse(inputStream, outputStream, Files.probeContentType(tempFile));
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (IOException e) {
                LOGGER.error("Error occurred while handling byte array request: {}", e.getMessage());
            }
        }
        return outputStream;
    }

    private void writeResponse(InputStream inputStream, OutputStream outputStream, String contentType) throws IOException {
        outputStream.write(new FormattedResponse()
                .contentType(contentType)
                .content(inputStream.readAllBytes())
                .statusCode(200)
                .statusMessage("OK").build());
    }

    private static String[] getFileNameAndExtension(String path) {
        String[] parts = new String[2];
        Pattern p = Pattern.compile("/([^/]+)\\.([^/]+)$");
        java.util.regex.Matcher m = p.matcher(path);
        if (m.find()) {
            // Part 1 is the file name
            parts[0] = m.group(1);

            // Part 2 is the extension
            parts[1] = m.group(2);
        }
        return parts;
    }
}


