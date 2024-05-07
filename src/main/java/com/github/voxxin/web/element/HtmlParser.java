package com.github.voxxin.web.element;

import java.util.*;
import java.util.regex.*;

public class HtmlParser {

    /**
     * Parse HTML string into a list of HtmlElements.
     *
     * @param htmlString The HTML string to parse.
     * @return The list of HtmlElements parsed from the HTML string.
     */
    public static List<HtmlElement> parseHtmlString(String htmlString) {
        return parseContent(htmlString);
    }

    /**
     * Parses the content of an HTML string recursively.
     *
     * @param content The HTML content to parse.
     * @return The list of HtmlElements parsed from the HTML content.
     */
    private static List<HtmlElement> parseContent(String content) {
        List<HtmlElement> elements = new ArrayList<>();
        Stack<String> openTags = new Stack<>();
        Pattern pattern = Pattern.compile("<(\\w+)(.*?)>(.*?)</\\1>|<(\\w+)(.*?)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String tagName = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
            String attributes = matcher.group(2) != null ? matcher.group(2) : matcher.group(5);
            String innerContent = matcher.group(3);

            System.out.println("1: " + matcher.group(1) + " 2: " + matcher.group(2) + " 3: " + matcher.group(3) + " 4: " + matcher.group(4) + " 5: " + matcher.group(5));

            if (tagName != null) {
                if (innerContent != null) {
                    if (parseContent(innerContent).isEmpty()) elements.add(new HtmlElement(tagName, parseAttributes(attributes), innerContent));
                        else elements.add(new HtmlElement(tagName, parseAttributes(attributes), parseContent(innerContent)));
                } else {
                    elements.add(new HtmlElement(tagName, parseAttributes(attributes), (HtmlElement) null));
                    openTags.push(tagName);
                }
            }

            if (innerContent == null && matcher.group(4) != null && !openTags.isEmpty() && openTags.peek().equals(matcher.group(4))) {
                openTags.pop();
            }
        }

        return elements;
    }

    /**
     * Parse HTML attributes into a list of strings.
     *
     * @param attributesString The string containing HTML attributes.
     * @return The list of parsed attributes.
     */
    private static List<String> parseAttributes(String attributesString) {
        List<String> attributes = new ArrayList<>();
        if (attributesString != null) {
            Pattern pattern = Pattern.compile("([^\"].*?([^\"]))\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(attributesString);
            while (matcher.find()) {
                String attributeName = matcher.group(1);
                String attributeValue = matcher.group(3);
                attributes.add((attributeName + "\"" + attributeValue + "\"").trim());
            }
        }
        return attributes;
    }
}