package com.github.voxxin.web.request;

import java.util.*;

public class FormattedRequest {

    private final HashMap<String, String> headers;
    private final String body;
    private final String method;
    private String path;
    private final String pathParameters;
    private final HashMap<String, String> query;
    private final String httpVersion;

    public FormattedRequest(List<String> inputHeaders) {
        this.body = (inputHeaders.get(0).split(" ").length > 1 && inputHeaders.get(0).split(" ")[2].contains("HTTP/")) ? null : inputHeaders.remove(0);

        String[] mainMethods = inputHeaders.remove(0).split(" ");
        this.method = mainMethods[0];
        String fullPath = mainMethods[1];
        this.httpVersion = mainMethods[2];

        this.headers = new HashMap<>();
        this.query = new HashMap<>();

        int queryIndex = fullPath.indexOf('?');
        if (queryIndex != -1) {
            String[] pathAndQuery = fullPath.split("\\?", 2);
            this.path = pathAndQuery[0];
            String[] queryParams = pathAndQuery[1].split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=", 2);
                query.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
            }
        } else {
            this.path = fullPath;
        }

        int pathParamIndex = path.indexOf(':');
        if (pathParamIndex != -1) {
            this.pathParameters = path.substring(pathParamIndex + 1);
            this.path = path.substring(0, pathParamIndex);
        } else {
            this.pathParameters = null;
        }

        for (String header : inputHeaders) {
            String[] parts = header.split(": ", 2);
            if (parts.length == 2) {
                headers.put(parts[0], parts[1]);
            }
        }
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getPathParameters() {
        return pathParameters;
    }

    public HashMap<String, String> getQuery() {
        return query;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
