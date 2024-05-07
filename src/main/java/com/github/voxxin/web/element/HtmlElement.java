package com.github.voxxin.web.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HtmlElement {
    private final String tagName;
    private final List<String> attributes;
    private List<HtmlElement> subElements;
    private String subElement;
    private String htmlString;

    /**
     * Constructor for HtmlElement with sub-elements.
     *
     * @param tagName     The tag name of the HTML element.
     * @param attributes  The list of attributes for the HTML element.
     * @param subElements The sub-elements contained within this HTML element.
     */
    public HtmlElement(@NotNull String tagName, @Nullable List<String> attributes, @Nullable HtmlElement... subElements) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.subElement = null;
        this.subElements = new ArrayList<>();
        if (subElements != null) {
            for (HtmlElement element : subElements) {
                addSubElement(element);
            }
        }
    }

    /**
     * Constructor for HtmlElement with text content.
     *
     * @param tagName    The tag name of the HTML element.
     * @param attributes The list of attributes for the HTML element.
     * @param subElement The text content of the HTML element.
     */
    public HtmlElement(@NotNull String tagName, @Nullable List<String> attributes, @Nullable String subElement) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.subElement = subElement;
        this.subElements = new ArrayList<>();
    }

    /**
     * Add a sub-element to this HtmlElement.
     *
     * @param element The sub-element to be added.
     */
    public void addSubElement(HtmlElement element) {
        if (element != null) {
            subElements.add(element);
            this.subElement = null;
        }
    }

    /**
     * Set the list of sub-elements for this HtmlElement.
     *
     * @param subElements The list of sub-elements.
     */
    public void setSubElements(List<HtmlElement> subElements) {
        this.subElements = subElements;
        this.subElement = null;
    }

    /**
     * Set the text content for this HtmlElement.
     *
     * @param subElement The text content.
     */
    public void setSubElement(String subElement) {
        this.subElements = null;
        this.subElement = subElement;
    }

    /**
     * Get the tag name of this HtmlElement.
     *
     * @return The tag name.
     */
    public String getTagName() {
        return this.tagName;
    }

    /**
     * Get the list of attributes of this HtmlElement.
     *
     * @return The list of attributes.
     */
    public List<String> getAttributes() {
        return attributes;
    }

    /**
     * Get the list of sub-elements of this HtmlElement.
     *
     * @return The list of sub-elements.
     */
    public List<HtmlElement> getSubElements() {
        return subElements;
    }

    /**
     * Get the text content of this HtmlElement.
     *
     * @return The text content.
     */
    public String getSubElement() {
        return subElement;
    }

    /**
     * Generate the HTML string representation of this HtmlElement.
     *
     * @return The HTML string.
     */
    protected String htmlString() {
        if (htmlString == null) {
            StringBuilder mainBuilder = new StringBuilder();
            StringBuilder attributesBuilder = new StringBuilder();
            if (attributes != null) {
                for (String attribute : attributes) {
                    attribute = attribute.trim();
                    attributesBuilder.append(" ").append(attribute);
                }
            }

            mainBuilder.append("<").append(tagName).append(attributesBuilder).append("> ");
            if (!subElements.isEmpty()) {
                for (HtmlElement element : subElements) {
                    mainBuilder.append(" ").append(element.htmlString());
                }
            } else if (subElement != null) {
                mainBuilder.append(subElement);
            }
            mainBuilder.append("</").append(tagName).append(">");

            htmlString = mainBuilder.toString();
        }
        return htmlString;
    }
}