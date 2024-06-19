package routes;

import com.github.voxxin.web.AbstractRoute;
import com.github.voxxin.web.element.HtmlElement;
import com.github.voxxin.web.element.HtmlElementBuilder;
import com.github.voxxin.web.element.HtmlParser;
import com.github.voxxin.web.request.FormattedRequest;
import com.github.voxxin.web.request.FormattedResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ErrorRoute extends AbstractRoute {
    public ErrorRoute() {
        super("/error");
    }

    @Override
    public OutputStream handleRequests(FormattedRequest request, OutputStream outputStream) throws IOException {
        List<HtmlElement> base = HtmlParser.parseHtmlString("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>404 Not Found</title><style>body{font-family:Arial,sans-serif;background-color:#f0f0f0;margin:0;padding:0;display:flex;justify-content:center;align-items:center;height:100vh;}.container{text-align:center;}h1{font-size:48px;margin-bottom:20px;color:#333;}p{font-size:24px;color:#666;margin-bottom:40px;}img{max-width:100%;border-radius:8px;box-shadow:0 4px 8px rgba(0,0,0,0.1);}</style></head><body><div class=\"container\"><h1>404 - Page Not Found</h1><p>Sorry, the page you are looking for could not be found.</p><img src=\"https://cataas.com/cat/gif\" alt=\"meow\"></div></body></html>\n");
        HtmlElementBuilder builderOutput = new HtmlElementBuilder();

        for (HtmlElement element : base) {
            builderOutput.addElement(element);
        }

        outputStream.write(
                new FormattedResponse()
                        .contentType("text/html")
                        .content(builderOutput.build())
                        .statusCode(404)
                        .statusMessage("Not Found")
                        .build()
        );
        return outputStream;
    }

}
