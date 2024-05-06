package com.github.voxxin.web.element;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HtmlElementBuilder {
    private final List<Object> elements;

    public HtmlElementBuilder() {
        this.elements = new ArrayList<>();
    }

    public HtmlElementBuilder addElement(@NotNull HtmlElement element) {
        this.elements.add(element);
        return this;
    }

    public HtmlElementBuilder addElement(@NotNull String element) {
        this.elements.add(element);
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object obj : elements) {
            stringBuilder.append(obj instanceof HtmlElement ? ((HtmlElement) obj).htmlString() : obj).append("\n");
        }
        return stringBuilder.toString();
    }
}
