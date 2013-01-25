package com.prettybit.likes;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

/**
 * @author Pavel Mikhalchuk
 */
public class Main {

    private static WebClient browser = initBrowser();

    public static void main(String[] args) throws IOException {
        loginToFacebook();
        for (Resource r : fetchResourcesFromLikeToCash()) {
            processLikeToCash(r);
        }
        for (Resource r : fetchResourcesFromYouLikeCash()) {
            processYouLikeCash(r);
        }
    }

    private static HtmlPage loginToFacebook() throws IOException {
        System.out.println("Logging in to Facebook...");
        HtmlPage p = browser.getPage("https://www.facebook.com/");
        p.getElementById("email").setAttribute("value", "te.akkou@gmail.com");
        p.getElementById("pass").setAttribute("value", "369369369ZaYa");
        p = (HtmlPage) ((HtmlLabel) p.getElementById("loginbutton")).click();
        System.out.println("Logged in to Facebook!");
        return p;
    }

    private static Collection<Resource> fetchResourcesFromLikeToCash() throws IOException {
        System.out.println("Fetching resources from LikeToCash...");
        HtmlPage p = browser.getPage("http://liketocash.com/members/login");
        Collection<Resource> result = transform(filter(p.getElementsByTagName("div"), Resource.isResource()), Resource.fromDiv());
        System.out.println("Fetched " + result.size() + " resources from LikeToCash!");
        return result;
    }

    private static Collection<Resource> fetchResourcesFromYouLikeCash() throws IOException {
        System.out.println("Fetching resources from YouLikeCash...");
        HtmlPage p = browser.getPage("http://youlikecash.com/members/login");
        Collection<Resource> result = transform(filter(p.getElementsByTagName("div"), Resource.isResource()), Resource.fromDiv());
        System.out.println("Fetched " + result.size() + " resources from LikeToCash!");
        return result;
    }

    private static void processLikeToCash(Resource r) throws IOException {
        try {
            like(r);
            cashInLikeToCash(r);
        } catch (IllegalAccessError e) {
            System.out.println(e.getMessage());
        }
    }

    private static void processYouLikeCash(Resource r) throws IOException {
        try {
            like(r);
            cashInYouLikeCash(r);
        } catch (IllegalAccessError e) {
            System.out.println(e.getMessage());
        }
    }

    private static void like(Resource r) throws IOException {
        System.out.println("Liking - " + r.getUrl() + "...");

        List<NameValuePair> parameters = new LinkedList<NameValuePair>();
        parameters.add(new NameValuePair("fbpage_id", r.getPageId()));
        parameters.add(new NameValuePair("add", "true"));
        parameters.add(new NameValuePair("reload", "false"));
        parameters.add(new NameValuePair("fan_origin", "page_timeline"));
        parameters.add(new NameValuePair("nctr[_mod]", "pagelet_timeline_page_actions"));
        parameters.add(new NameValuePair("__user", "100005092930776"));
        parameters.add(new NameValuePair("__a", "1"));
        parameters.add(new NameValuePair("__req", "a"));
        parameters.add(new NameValuePair("fb_dtsg", "AQCsbF3c"));
        parameters.add(new NameValuePair("phstamp", "165816711598705199167"));

        WebRequest likeRequest = new WebRequest(new URL("http://www.facebook.com/ajax/pages/fan_status.php"), HttpMethod.POST);
        likeRequest.setRequestParameters(parameters);

        browser.getPage(likeRequest);

        System.out.println("Liked - " + r.getUrl() + "!");
    }

    private static void cashInLikeToCash(Resource r) throws IOException {
        browser.getPage("http://liketocash.com/like/confirm/" + r.getPageId() + "/" + r.getOwnId());
    }

    private static void cashInYouLikeCash(Resource r) throws IOException {
        browser.getPage("http://youlikecash.com/like/confirm/" + r.getPageId() + "/" + r.getOwnId());
    }

    private static WebClient initBrowser() {
        System.out.println("Starting browser...");

        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

        WebClient browser = new WebClient(BrowserVersion.CHROME_16);
        browser.getOptions().setCssEnabled(false);

        browser.setIncorrectnessListener(new IncorrectnessListener() {
            @Override
            public void notify(String arg0, Object arg1) {
            }
        });

        browser.setCssErrorHandler(new ErrorHandler() {
            @Override
            public void warning(CSSParseException exception) throws CSSException {
            }

            @Override
            public void fatalError(CSSParseException exception) throws CSSException {
            }

            @Override
            public void error(CSSParseException exception) throws CSSException {
            }
        });

        browser.setJavaScriptErrorListener(new JavaScriptErrorListener() {
            @Override
            public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
            }

            @Override
            public void scriptException(HtmlPage arg0, ScriptException arg1) {
            }

            @Override
            public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
            }

            @Override
            public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {
            }
        });

        browser.setHTMLParserListener(new HTMLParserListener() {
            @Override
            public void error(String message, URL url, String html, int line, int column, String key) {
            }

            @Override
            public void warning(String message, URL url, String html, int line, int column, String key) {
            }
        });

        browser.getOptions().setThrowExceptionOnFailingStatusCode(false);
        browser.getOptions().setThrowExceptionOnScriptError(false);

        System.out.println("Browser started!");

        return browser;
    }

    private static class Resource {

        private String url;
        private String state;
        private String pageId;
        private String ownId;

        private Resource(DomElement div) {
            url = div.getAttribute("pageurl");
            state = div.getAttribute("state");
            pageId = div.getAttribute("pageid");
            ownId = div.getAttribute("ownid");
        }

        public String getUrl() {
            return url;
        }

        public String getState() {
            return state;
        }

        public String getPageId() {
            return pageId;
        }

        public String getOwnId() {
            return ownId;
        }

        @Override
        public String toString() {
            return state + ":" + url + ":" + pageId + ":" + ownId;
        }

        public static Predicate<DomElement> isResource() {
            return new Predicate<DomElement>() {
                @Override
                public boolean apply(DomElement e) {
                    return e.hasAttribute("class") && e.getAttribute("class").contains("grid_4 box");
                }
            };
        }

        public static Function<DomElement, Resource> fromDiv() {
            return new Function<DomElement, Resource>() {
                @Override
                public Resource apply(DomElement e) { return new Resource(e); }
            };
        }

    }

}
