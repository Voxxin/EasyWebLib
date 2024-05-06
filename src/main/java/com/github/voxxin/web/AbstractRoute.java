package com.github.voxxin.web;

import com.github.voxxin.web.request.FormattedRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class AbstractRoute {

    public final String route;

    public AbstractRoute(String route) {
        this.route = route;
    }

    void handleRequest(FormattedRequest request, Socket clientSocket) {
        try (OutputStream stream = handleRequests(request, clientSocket.getOutputStream())) {
            stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {

        outputStream.write("um...".getBytes());
        return outputStream;
    }
}

