import com.github.voxxin.web.WebServer;
import org.junit.jupiter.api.Test;
import routes.ErrorRoute;
import routes.IndexRoute;

public class WebsiteTest {
    private final WebServer web;
    public WebsiteTest() {

        this.web = new WebServer(2020, new IndexRoute());
        this.web.errorPage(new ErrorRoute());
    }

    @Test
    void runWebsite() {
        this.web.start();
    }
}
