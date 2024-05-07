package com.github.voxxin.web.request;

import java.nio.charset.StandardCharsets;

public class FormattedResponse {

    private static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";
    private static final int DEFAULT_STATUS_CODE = 404;

    private String httpVersion = DEFAULT_HTTP_VERSION;
    private int statusCode = DEFAULT_STATUS_CODE;
    private String statusMessage;
    private String contentType;
    private byte[] contentBytes;

    /**
     * Set the HTTP version for the response.
     *
     * @param httpVersion The HTTP version.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse httpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }

    /**
     * Set the status code for the response.
     *
     * @param statusCode The status code.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Set the status message for the response.
     *
     * @param statusMessage The status message.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse statusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * Set the content type for the response.
     *
     * @param contentType The content type.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the content for the response.
     *
     * @param content The content.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse content(String content) {
        this.contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Set the content for the response.
     *
     * @param contentBytes The content bytes.
     * @return The FormattedResponse instance.
     */
    public FormattedResponse content(byte[] contentBytes) {
        this.contentBytes = contentBytes;
        return this;
    }

    /**
     * Build the formatted response.
     *
     * @return The formatted response string.
     */
    public String build() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(httpVersion).append(" ").append(statusCode).append(" ").append(statusMessage).append("\r\n")
                .append("Content-Type: ").append(contentType).append("\r\n");

        if (contentBytes != null) {
            responseBuilder.append("Content-Length: ").append(contentBytes.length).append("\r\n\r\n")
                    .append(new String(contentBytes, StandardCharsets.UTF_8));
        } else {
            responseBuilder.append("\r\n");
        }

        return responseBuilder.toString();
    }
}
