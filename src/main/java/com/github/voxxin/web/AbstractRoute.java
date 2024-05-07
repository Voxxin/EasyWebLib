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

    /**
     * Handle a request received on this route.
     *
     * @param request      The formatted request.
     * @param clientSocket The client socket.
     */
    void handleRequest(FormattedRequest request, Socket clientSocket) {
        try (OutputStream stream = handleRequests(request, clientSocket.getOutputStream())) {
            stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle requests received on this route.
     *
     * @param request       The formatted request.
     * @param outputStream  The output stream to write the response.
     * @return              The output stream after handling the request.
     * @throws IOException  If an I/O error occurs.
     */
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {

        outputStream.write("um...".getBytes());
        return outputStream;
    }
}

