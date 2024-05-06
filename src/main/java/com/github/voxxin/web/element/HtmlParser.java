package com.github.voxxin.web.element;

import java.util.*;
import java.util.regex.*;

public class HtmlParser {

    public static List<HtmlElement> parseHtmlString(String htmlString) {
        List<HtmlElement> elements = new ArrayList<>();
        Stack<HtmlElement> stack = new Stack<>();
        Pattern pattern = Pattern.compile("<(\\w+)(.*?)>(.*?)</\\1>|<(\\w+)(.*?)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlString);

        while (matcher.find()) {
            String tagName = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
            String attributes = matcher.group(2) != null ? matcher.group(2) : matcher.group(5);
            String content = matcher.group(3);

            if (tagName != null) {
                HtmlElement element = new HtmlElement(tagName, parseAttributes(attributes), content != null ? content : "");
                if (!stack.isEmpty()) {
                    HtmlElement parent = stack.peek();
                    parent.addSubElement(element);
                }
                if (content == null) {
                    stack.push(element);
                }
                elements.add(element);
            } else {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
        }
        return elements;
    }

    private static List<String> parseAttributes(String attributesString) {
        List<String> attributes = new ArrayList<>();
        if (attributesString != null) {
            Pattern pattern = Pattern.compile("(\\w+)=\"(.*?)\"");
            Matcher matcher = pattern.matcher(attributesString);
            while (matcher.find()) {
                String attributeName = matcher.group(1);
                String attributeValue = matcher.group(2);
                attributes.add(attributeName + "=\"" + attributeValue + "\"");
            }
        }
        return attributes;
    }
}


