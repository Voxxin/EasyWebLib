package com.github.voxxin.web.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HtmlElement {
    private final String tagName;
    private final List<String> attributes;
    private final List<HtmlElement> subElements;
    private final String subElement;
    private String htmlString;

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

    public HtmlElement(@NotNull String tagName, @Nullable List<String> attributes, @Nullable String subElement) {
        this.tagName = tagName;
        this.attributes = attributes;
        this.subElement = subElement;
        this.subElements = new ArrayList<>();
    }

    public void addSubElement(HtmlElement element) {
        if (element != null) {
            subElements.add(element);
        }
    }


    public String getTagName() {
        return this.tagName;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<HtmlElement> getSubElements() {
        return subElements;
    }

    public String getSubElement() {
        return subElement;
    }

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
                    mainBuilder.append(" ")
                            .append(element.htmlString());
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
