package com.github.voxxin.web.request;

public class FormattedResponse {

    private String httpVersion = "HTTP/1.1"; // Default HTTP version
    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String content;

    public FormattedResponse withHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    public FormattedResponse withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public FormattedResponse withStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public FormattedResponse withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public FormattedResponse withContent(String content) {
        this.content = content;
        return this;
    }

    public String build() {
        String builder = httpVersion + " " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" +
                content;
        return builder;
    }
}

