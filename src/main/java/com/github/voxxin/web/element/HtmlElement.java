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
     * Add multiple sub-elements to this HtmlElement.
     *
     * @param subElements The list of sub-elements to be added.
     */
    public void addSubElements(List<HtmlElement> subElements) {
        this.subElements.addAll(subElements);
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
     * Check if the HtmlElement has an attribute with the specified type and value.
     *
     * @param attributeType  The type of the attribute.
     * @param attributeValue The value of the attribute.
     * @return True if the HtmlElement has the specified attribute, false otherwise.
     */
    public boolean hasAttribute(String attributeType, String attributeValue) {
        if (attributes != null) {
            for (String attr : attributes) {
                String[] parts = attr.split("=");
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    String value = parts[1].replaceAll("\"", "").trim();
                    if (type.equals(attributeType) && value.equals(attributeValue)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the value of the attribute with the specified type.
     *
     * @param attributeType The type of the attribute.
     * @return The value of the attribute, or null if not found.
     */
    public String[] getAttributeValue(String attributeType) {
        if (attributes != null) {
            for (int i = 0; i < attributes.size(); i++) {
                String attr = attributes.get(i);
                String[] parts = attr.split("=");
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    String value = parts[1].replaceAll("\"", "").trim();
                    if (type.equals(attributeType)) {
                        return new String[]{value, Integer.toString(i)};
                    }
                }
            }
        }
        return null;
    }

    /**
     * Modify the value of the attribute at the specified index.
     *
     * @param index           The index of the attribute to modify.
     * @param newAttributeValue The new value for the attribute.
     */
    public void modifyAttributeAtIndex(int index, String newAttributeValue) {
        if (attributes != null && index >= 0 && index < attributes.size()) {
            attributes.set(index, newAttributeValue);
        }
    }

    /**
     * Modify the value of the attribute with the specified type.
     *
     * @param attributeType    The type of the attribute to modify.
     * @param newAttributeValue The new value for the attribute.
     */
    public void modifyAttribute(String attributeType, String newAttributeValue) {
        if (attributes != null) {
            for (int i = 0; i < attributes.size(); i++) {
                String attr = attributes.get(i);
                String[] parts = attr.split("=");
                if (parts.length == 2) {
                    String type = parts[0].trim();
                    String value = parts[1].replaceAll("\"", "").trim();
                    if (type.equals(attributeType)) {
                        attributes.set(i, type + "=\"" + newAttributeValue + "\"");
                        return;
                    }
                }
            }
        }
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
            if (this.subElements != null && !this.subElements.isEmpty()) {
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