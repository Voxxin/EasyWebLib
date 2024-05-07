package routes;

import com.github.voxxin.web.AbstractRoute;
import com.github.voxxin.web.request.FormattedRequest;
import com.github.voxxin.web.request.FormattedResponse;
import com.github.voxxin.web.element.HtmlElement;
import com.github.voxxin.web.element.HtmlElementBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class IndexRoute extends AbstractRoute {
    public IndexRoute() {
        super("/");
    }

    @Override
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {
        HtmlElement html = new HtmlElement("html", Arrays.asList("lang=\"en\""),
                new HtmlElement("head", null,
                        new HtmlElement("meta", Arrays.asList("charset=\"UTF-8\"")),
                        new HtmlElement("meta", Arrays.asList("name=\"viewport\"", "content=\"width=device-width, initial-scale=1.0\"")),
                        new HtmlElement("title", null, "My Melody Fan Page"),
                        new HtmlElement("style", null, "body { background-image: url('https://i.imgur.com/XGGbDtX.gif'); background-repeat: repeat; color: #660066; font-family: \"Comic Sans MS\", cursive, sans-serif; margin: 0; padding: 0; } .container { max-width: 800px; margin: 0 auto; padding: 20px; margin-top: 20px; background-color: #ff99cc; border: 5px solid #ff3399; border-radius: 10px; box-shadow: 0 0 20px rgba(0, 0, 0, 0.1); } h1 { color: #ff3399; text-align: center; font-size: 36px; text-shadow: 2px 2px 4px #ffb3d9; } p { text-align: justify; line-height: 1.5; } .image { text-align: center; margin-bottom: 20px; } .footer { text-align: center; margin-top: 50px; color: #660066; font-size: 14px; font-style: italic; }")),
                new HtmlElement("body", null,
                        new HtmlElement("div", Arrays.asList("class=\"container\""),
                                new HtmlElement("h1", null, "Welcome to My Melody Fan Page!"),
                                new HtmlElement("div", Arrays.asList("class=\"image\""),
                                        new HtmlElement("img", Arrays.asList("src=\"https://i.imgur.com/1uh7oJK.gif\"", "alt=\"My Melody\"", "width=\"200\""))
                                ),
                                new HtmlElement("p", null, "This is a place where we celebrate everything about My Melody! From her cute pink ears to her adorable bow, My Melody brings joy to everyone's heart."),
                                new HtmlElement("p", null, "My Melody is a character created by Sanrio, the same company behind Hello Kitty. She is a sweet and kind-hearted rabbit who loves to bake and spend time with her friends."),
                                new HtmlElement("p", null, "On this fan page, you'll find information about My Melody's history, her friends, merchandise, and much more!"),
                                new HtmlElement("div", Arrays.asList("class=\"image\""),
                                        new HtmlElement("img", Arrays.asList("src=\"https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNjh2ZzE2cHhnYnQ0Y3NyZmh5dTA1cWdubHhyNG80YnFnaWNtb2NyYiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ts0Uy1zVJTvCo/giphy.gif\"", "alt=\"My Melody\"", "width=\"200\"")),
                                        new HtmlElement("img", Arrays.asList("src=\"https://i.imgur.com/i9Q85ew.gif\"", "alt=\"My Melody\"", "width=\"200\"")),
                                        new HtmlElement("img", Arrays.asList("src=\"https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExbTJqdGFpMzBpY2QzMGl3eGIyN2hzNHZxMDhxdHdrMWQxMjAxc21kYyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/70v43qJWwqH4I/giphy.gif\"", "alt=\"My Melody\"", "width=\"200\""))
                                ),
                                new HtmlElement("p", null, "So sit back, relax, and immerse yourself in the world of My Melody!")
                        )
                ),
                new HtmlElement("div", Arrays.asList("class=\"footer\""),
                        "&copy; 2002 My Melody Fan Page. All rights reserved."
                )
        );

        // Build the welcome page using the builder
        HtmlElementBuilder builder = new HtmlElementBuilder();
        String welcomePage = builder.addElement(html)
                .build();

        outputStream.write(
                new FormattedResponse()
                        .contentType("text/html")
                        .content(welcomePage)
                        .statusCode(200)
                        .statusMessage("NICE ONE")
                        .build()
                        .getBytes()
        );

        return super.handleRequests(request, outputStream);
    }
}
