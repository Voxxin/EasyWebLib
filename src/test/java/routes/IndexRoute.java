package routes;

import com.github.voxxin.web.AbstractRoute;
import com.github.voxxin.web.request.FormattedRequest;
import com.github.voxxin.web.request.FormattedResponse;
import com.github.voxxin.web.element.HtmlElement;
import com.github.voxxin.web.element.HtmlElementBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class IndexRoute extends AbstractRoute {
    public IndexRoute() {
        super("/");
    }

    @Override
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {
        HtmlElement html = new HtmlElement("html").addAttribute("lang", "en-US").addSubElements(
                new HtmlElement("head").addSubElements(
                        new HtmlElement("meta").addAttribute("charset", "UTF-8"),
                        new HtmlElement("meta").addAttribute("name", "viewport").addAttributes("content", Arrays.asList("width=device-width", "initial-scale=1.0")),
                        new HtmlElement("title").setStringSubElement("My Melody Fan Page"),
                        new HtmlElement("style").setStringSubElement("body { background-image: url('https://i.imgur.com/XGGbDtX.gif'); background-repeat: repeat; color: #660066; font-family: \"Comic Sans MS\", cursive, sans-serif; margin: 0; padding: 0; } .container { max-width: 800px; margin: 0 auto; padding: 20px; margin-top: 20px; background-color: #ff99cc; border: 5px solid #ff3399; border-radius: 10px; box-shadow: 0 0 20px rgba(0, 0, 0, 0.1); } h1 { color: #ff3399; text-align: center; font-size: 36px; text-shadow: 2px 2px 4px #ffb3d9; } p { text-align: justify; line-height: 1.5; } .image { text-align: center; margin-bottom: 20px; } .footer { text-align: center; margin-top: 50px; color: #660066; font-size: 14px; font-style: italic; }"),
                        new HtmlElement("body").addSubElements(
                                new HtmlElement("div").addAttribute("class", "container").addSubElements(
                                        new HtmlElement("h1").setStringSubElement("Welcome to My Melody Fan Page!"),
                                        new HtmlElement("div").addAttribute("class", "image").addSubElement(
                                                new HtmlElement("img").addAttribute("src", "https://i.imgur.com/1uh7oJK.gif").addAttribute("alt", "My Melody").addAttribute("width", "200")
                                        ),
                                        new HtmlElement("p").setStringSubElement("This is a place where we celebrate everything about My Melody! From her cute pink ears to her adorable bow, My Melody brings joy to everyone's heart."),
                                        new HtmlElement("p").setStringSubElement("My Melody is a character created by Sanrio, the same company behind Hello Kitty. She is a sweet and kind-hearted rabbit who loves to bake and spend time with her friends."),
                                        new HtmlElement("p").setStringSubElement("On this fan page, you'll find information about My Melody's history, her friends, merchandise, and much more!"),
                                        new HtmlElement("div").addAttribute("class", "image").addSubElements(
                                                new HtmlElement("img").addAttribute("src", "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNjh2ZzE2cHhnYnQ0Y3NyZmh5dTA1cWdubHhyNG80YnFnaWNtb2NyYiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ts0Uy1zVJTvCo/giphy.gif").addAttribute("alt", "My Melody").addAttribute("width", "200"),
                                                new HtmlElement("img").addAttribute("src", "https://i.imgur.com/i9Q85ew.gif").addAttribute("alt", "My Melody").addAttribute("width", "200"),
                                                new HtmlElement("img").addAttribute("src", "https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbTJqdGFpMzBpY2QzMGl3eGIyN2hzNHZxMDhxdHdrMWQxMjAxc21kYyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/70v43qJWwqH4I/giphy.gif").addAttribute("alt", "My Melody").addAttribute("width", "200")
                                        ),
                                        new HtmlElement("p").setStringSubElement("So sit back, relax, and immerse yourself in the world of My Melody!")
                                )
                        ),
                        new HtmlElement("div").addAttribute("class", "footer").setStringSubElement("&copy; 2002 My Melody Fan Page. All rights reserved.")
                )
        );

        // Build the welcome page using the builder
        HtmlElementBuilder builder = new HtmlElementBuilder();
        String welcomePage = builder.addElement(html)
                .build();

        outputStream.write(
                new FormattedResponse()
                        .contentType("text/html")
                        .content(welcomePage.getBytes())
                        .statusCode(200)
                        .statusMessage("NICE ONE")
                        .build()
        );

        return super.handleRequests(request, outputStream);
    }
}
