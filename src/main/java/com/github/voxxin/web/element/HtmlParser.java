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

        Pattern pattern = Pattern.compile("<(\\w+)(.*?)>(.*?)</\\1>|<(\\w+)(.*?)>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String tagName = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
            String attributes = matcher.group(2) != null ? matcher.group(2) : matcher.group(5);
            String innerContent = matcher.group(3);

            if (tagName != null) {
                HtmlElement element = new HtmlElement(tagName, parseAttributes(attributes), innerContent != null ? innerContent : "");

                if (innerContent != null) {
                    List<HtmlElement> innerElements = parseContent(innerContent);
                    element.addSubElements(innerElements);
                }

                elements.add(element);
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