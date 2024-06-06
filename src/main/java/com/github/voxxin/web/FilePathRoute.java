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

class FilePathRoute extends AbstractRoute {
    private final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private final File file;

    public FilePathRoute(File file, String route) {
        super(route + file.getName());
        this.file = file;
    }

    @Override
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            outputStream.write(new FormattedResponse()
                    .contentType(Files.probeContentType(file.toPath()))
                    .content(fileInputStream.readAllBytes())
                    .statusCode(200)
                    .statusMessage("OK").build());
        } catch (IOException e) {
            LOGGER.error("Error occurred while handling file request: {}", e.getMessage());
        }

        return outputStream;
    }
}

