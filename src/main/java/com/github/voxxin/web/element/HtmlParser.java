package com.github.voxxin.web.element;

import java.util.*;
import java.util.regex.*;

public class HtmlParser {

    public static List<HtmlElement> parseHtmlString(String htmlString) {
        List<HtmlElement> elements = new ArrayList<>();
        parseContent(htmlString, elements, new Stack<>());
        return elements;
    }

    private static void parseContent(String content, List<HtmlElement> elements, Stack<HtmlElement> stack) {
        Pattern pattern = Pattern.compile("<(\\w+)(.*?)>(.*?)</\\1>|<(\\w+)(.*?)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String tagName = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
            String attributes = matcher.group(2) != null ? matcher.group(2) : matcher.group(5);
            String innerContent = matcher.group(3);

            if (tagName != null) {
                HtmlElement element = new HtmlElement(tagName, parseAttributes(attributes), innerContent != null ? innerContent : "");

                if (innerContent != null) {
                    parseContent(innerContent, elements, stack);
                }
                if (!stack.isEmpty()) {
                    stack.peek().addSubElement(element);
                }
                elements.add(element);
            } else if (!stack.isEmpty()) {
                stack.pop();
            }
        }
    }

    private static List<String> parseAttributes(String attributesString) {
        List<String> attributes = new ArrayList<>();
        if (attributesString != null) {
            Pattern pattern = Pattern.compile("([^\"].*?([^\"]))\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(attributesString);
            while (matcher.find()) {
                String attributeName = matcher.group(1);
                String attributeValue = matcher.group(3);
                attributes.add((attributeName + attributeValue).trim());
            }
        }
        return attributes;
    }
}