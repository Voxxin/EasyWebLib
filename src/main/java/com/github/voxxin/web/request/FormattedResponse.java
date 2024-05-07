package com.github.voxxin.web.request;

public class FormattedResponse {

    private String httpVersion = "HTTP/1.1"; // Default HTTP version
    private int statusCode;
    private String statusMessage;
    private String contentType;
    private String content;

    /**
     * Set the HTTP version for the response.
     *
     * @param httpVersion The HTTP version.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse withHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    /**
     * Set the status code for the response.
     *
     * @param statusCode The status code.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Set the status message for the response.
     *
     * @param statusMessage The status message.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse withStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * Set the content type for the response.
     *
     * @param contentType The content type.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the content for the response.
     *
     * @param content The content.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Build the formatted response.
     *
     * @return The formatted response string.
     */
    public String build() {
        String builder = httpVersion + " " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "\r\n" +
                content;
        return builder;
    }
}