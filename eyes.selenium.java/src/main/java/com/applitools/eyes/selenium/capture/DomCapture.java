package com.applitools.eyes.selenium.capture;

import com.applitools.eyes.TaskListener;
import com.applitools.eyes.IServerConnector;
import com.applitools.eyes.Location;
import com.applitools.eyes.Logger;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.positioning.ElementPositionProvider;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriterSettings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Phaser;

public class DomCapture {


    private static String CAPTURE_FRAME_SCRIPT;

    private static String CAPTURE_CSSOM_SCRIPT;
    private final Phaser treePhaser = new Phaser(1); // Phaser for syncing all callbacks on a single Frame
    private final Phaser mainPhaser = new Phaser(1); // Phaser for syncing all Frames


    static {
        try {
            CAPTURE_FRAME_SCRIPT = GeneralUtils.readToEnd(DomCapture.class.getResourceAsStream("/captureframe.js"));

            CAPTURE_CSSOM_SCRIPT = GeneralUtils.readToEnd(DomCapture.class.getResourceAsStream("/capturecssom.js"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static IServerConnector mServerConnector = null;
    private WebDriver mDriver;
    private final Logger mLogger;

    public DomCapture(Eyes eyes) {
        mServerConnector = eyes.getServerConnector();
        mLogger = eyes.getLogger();
    }

    public String getFullWindowDom(WebDriver driver, ElementPositionProvider positionProvider) {
        this.mDriver = driver;
        Location initialPosition = positionProvider.getCurrentPosition();
        positionProvider.setPosition(Location.ZERO);
        Map dom = GetWindowDom();
        positionProvider.setPosition(initialPosition);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
            String json = objectMapper.writeValueAsString(dom);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Map<String, Object> GetWindowDom() {

        Map argsObj = initMapDom();

        Map<String, Object> result = getFrameDom_(argsObj);

        return result;
    }

    private Map initMapDom() {

        Map argsObj = new HashMap();
        argsObj.put("styleProps", new String[]{
                "background-color",
                "background-image",
                "background-size",
                "color",
                "border-width",
                "border-color",
                "border-style",
                "padding",
                "margin"
        });

        argsObj.put("attributeProps", null);
        argsObj.put("rectProps", new String[]{
                "right",
                "bottom",
                "height",
                "width",
                "top",
                "left"});
        argsObj.put("ignoredTagNames", new String[]{
                "HEAD",
                "SCRIPT"});

        return argsObj;
    }

    private Map<String, Object> getFrameDom_(Map<String, Object> argsObj) {
        mLogger.verbose("Trying to get DOM from mDriver");
        long startingTime = System.currentTimeMillis();
        String executeScripString = (String) ((JavascriptExecutor) mDriver).executeScript(CAPTURE_FRAME_SCRIPT, argsObj);

        mLogger.verbose("Finished capturing DOM in - " + (System.currentTimeMillis() - startingTime));
        startingTime = System.currentTimeMillis();

        final Map<String, Object> executeScriptMap;
        try {
            executeScriptMap = parseStringToMap(executeScripString);

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }

        mLogger.verbose("Finished converting DOM map in - " + (System.currentTimeMillis() - startingTime));
        startingTime = System.currentTimeMillis();

        try {
            traverseDomTree(mDriver, argsObj, executeScriptMap, -1, new URL(mDriver.getCurrentUrl()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mainPhaser.arriveAndAwaitAdvance();

        mLogger.verbose("Finished going over DOM CSS in - " + (System.currentTimeMillis() - startingTime));

        return executeScriptMap;
    }

    private Stack<Integer> frameIndices = new Stack<>();


    private void traverseDomTree(WebDriver mDriver, Map<String, Object> argsObj, final Map<String, Object> domTree
            , int frameIndex, URL baseUrl) {

        mLogger.verbose("DomCapture.traverseDomTree  baseUrl - " + baseUrl);

        Map<String, Object> dom = null;


        Object tagNameObj = domTree.get("tagName");

        boolean frameHasContent = true;

        if (null == tagNameObj) return;

        if (frameIndex > -1) {

            //Try switching - if frame index is valid
            try {
                mDriver.switchTo().frame(frameIndex);

                frameIndices.push(0);

            } catch (Exception e) {
                GeneralUtils.logExceptionStackTrace(e);
                mDriver.switchTo().parentFrame();
                return;
            }

            String srcUrl = null;

            Object childNodes = domTree.get("childNodes");
            List childNodesAsMap = null;
            if (childNodes != null) {
                childNodesAsMap = (List) childNodes;
            }

            if (childNodesAsMap == null || childNodesAsMap.isEmpty()) {

                String json = (String) ((JavascriptExecutor) mDriver).executeScript(CAPTURE_FRAME_SCRIPT, argsObj);

                try {
                    dom = parseStringToMap(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                frameHasContent = false;

                domTree.put("childNodes", new Object[]{dom});

                Object attrsNodeObj = domTree.get("attributes");
                if (null != attrsNodeObj) {
                    Map<String, Object> attrsNode = (Map<String, Object>) attrsNodeObj;

                    Object srcUrlObj = attrsNode.get("src");
                    if (null != srcUrlObj) {
                        srcUrl = (String) srcUrlObj;
                    }
                }

                if (srcUrl == null) {
                    mLogger.log("WARNING! IFRAME WITH NO SRC");
                }
                try {
                    if (srcUrl.contains("img.bbystatic.com/BestBuy_US/js/tracking/ens-index.html")) {
                        mLogger.verbose("sgfwgsdgsdfg");
                    }
                    URL urlHref = new URL(baseUrl, srcUrl);
                    traverseDomTree(mDriver, argsObj, dom, -1, urlHref);

                } catch (MalformedURLException e) {
                    GeneralUtils.logExceptionStackTrace(e);
                }
            }
            frameIndices.pop();
            mDriver.switchTo().parentFrame();
        }
        if (frameHasContent) {
            String tagName = (String) tagNameObj;
            boolean isHTML = tagName.equalsIgnoreCase("HTML");

            if (isHTML) {
                mainPhaser.register();
                getFrameBundledCss(baseUrl, new TaskListener<String>() {
                    @Override
                    public void onComplete(String downloadedString) {
                        domTree.put("css", downloadedString);
                        mLogger.verbose("Putting css in " + " - CSS = " + downloadedString);
                        mainPhaser.arriveAndDeregister();
                    }

                    @Override
                    public void onFail() {
                        mLogger.verbose("mainPhaser.arriveAndDeregister()");
                        mainPhaser.arriveAndDeregister();

                    }
                });
                mLogger.verbose("Finish getFrameBundledCss(baseUrl)");
            }

            loop(mDriver, argsObj, domTree, baseUrl);
        }

    }

    private void loop(WebDriver mDriver, Map<String, Object> argsObj, Map<String, Object> domTree, URL baseUrl) {
        mLogger.verbose("DomCapture.loop");
        Object childNodesObj = domTree.get("childNodes");
        int index = 0;
        index = -1;
        if (frameIndices.size() > 0) {
            index = frameIndices.peek();
        }
        if (!(childNodesObj instanceof List)) {
            return;
        }
        List childNodes = (List) childNodesObj;
        for (Object node : childNodes) {
            if (node instanceof Map) {
                final Map<String, Object> domSubTree = (Map<String, Object>) node;

                mLogger.verbose("Current DOM subtree hash : " + domSubTree.hashCode());

                Object tagNameObj = domSubTree.get("tagName");

                String tagName = (String) tagNameObj;
                boolean isIframe = tagName.equalsIgnoreCase("IFRAME");

                if (isIframe) {
                    if (frameIndices.size() > 0) {
                        frameIndices.pop();
                    } else {
                        mLogger.verbose("frameIndices size is 0");
                    }
                    frameIndices.push(index + 1);
                    traverseDomTree(mDriver, argsObj, domSubTree, index, baseUrl);
                    index++;
                } else {
                    Object childSubNodesObj = domSubTree.get("childNodes");
                    if (childSubNodesObj == null || (childSubNodesObj instanceof List) && ((List) childSubNodesObj).isEmpty()) {
                        continue;
                    }
                    traverseDomTree(mDriver, argsObj, domSubTree, -1, baseUrl);
                }
            }
        }
        mLogger.verbose("DomCapture.loop - finish");
    }


    private void getFrameBundledCss(final URL baseUrl, TaskListener listener) {
        URI uri = URI.create(baseUrl.toString());
        if (!uri.isAbsolute()) {
            mLogger.log("WARNING! Base URL is not an absolute URL!");
        }
        CssTreeNode root = new CssTreeNode();
        root.setBaseUrl(baseUrl);

        List<String> result = (List<String>) ((JavascriptExecutor) mDriver).executeScript(CAPTURE_CSSOM_SCRIPT);
        final List<CssTreeNode> nodes = new ArrayList<>();
        for (String item : result) {
            String kind = item.substring(0, 5);
            //Value can be either css style or link to a css file
            String value = item.substring(5);
            if (kind.equalsIgnoreCase("text:")) {
                parseCSS(root, value);
                root.downloadNodeCss();
            } else {
                final CssTreeNode cssTreeNode = new CssTreeNode();
                cssTreeNode.setBaseUrl(root.baseUrl);
                cssTreeNode.setUrlPostfix(value);
                downloadCss(cssTreeNode, new TaskListener<String>() {
                    @Override
                    public void onComplete(String downloadedString) {
                        mLogger.verbose("DomCapture.onComplete");

                        parseCSS(cssTreeNode, downloadedString);
                        if (cssTreeNode.allImportRules != null && !cssTreeNode.allImportRules.isEmpty()) {

                            cssTreeNode.downloadNodeCss();
                        }
                    }

                    @Override

                    public void onFail() {
                        mLogger.verbose("DomCapture.onFail");
                    }
                });
                nodes.add(cssTreeNode);
            }
        }
        root.setDecedents(nodes);
        treePhaser.arriveAndAwaitAdvance();
        listener.onComplete(root.calcCss());
    }

    class CssTreeNode {

        URL baseUrl;

        URL urlPostfix;

        StringBuilder sb = new StringBuilder();
        List<CssTreeNode> decedents = new ArrayList<>();
        ICommonsList<CSSImportRule> allImportRules;
        ICommonsList<CSSStyleRule> styleRules;

        public void setDecedents(List<CssTreeNode> decedents) {
            this.decedents = decedents;
        }


        public void setBaseUrl(URL baseUri) {
            this.baseUrl = baseUri;
        }

        String calcCss() {
            if (decedents != null) {
                for (CssTreeNode decedent : decedents) {
                    sb.append(decedent.calcCss());
                }
            }

            if (styleRules != null) {
                for (CSSStyleRule styleRule : styleRules) {
                    sb.append(styleRule.getAsCSSString(new CSSWriterSettings()));
                }
            }

            return sb.toString();
        }

        void downloadNodeCss() {
            if (allImportRules != null) {

                for (CSSImportRule importRule : allImportRules) {
                    final CssTreeNode cssTreeNode;
                    cssTreeNode = new CssTreeNode();
                    cssTreeNode.setBaseUrl(this.baseUrl);
                    String uri = importRule.getLocation().getURI();
                    cssTreeNode.setUrlPostfix(uri);
                    downloadCss(cssTreeNode, new TaskListener<String>() {
                        @Override
                        public void onComplete(String downloadedString) {
                            parseCSS(cssTreeNode, downloadedString);
                            if (!cssTreeNode.allImportRules.isEmpty()) {
                                cssTreeNode.downloadNodeCss();

                            }
                        }

                        @Override
                        public void onFail() {
                            mLogger.verbose("Download Failed");
                        }
                    });
                    decedents.add(cssTreeNode);
                }
            }
        }

        public void setUrlPostfix(String urlPostfix) {

            boolean absolute = false;
            try {
                absolute = new URI(urlPostfix).isAbsolute();
                this.urlPostfix = absolute ? new URL(urlPostfix) : new URL(baseUrl, urlPostfix);
            } catch (URISyntaxException | MalformedURLException e) {
                GeneralUtils.logExceptionStackTrace(e);
            }

        }


        public void setAllImportRules(ICommonsList<CSSImportRule> allImportRules) {
            this.allImportRules = allImportRules;
        }

        public void setAllStyleRules(ICommonsList<CSSStyleRule> allStyleRules) {
            this.styleRules = allStyleRules;
        }


    }

    private void downloadCss(final CssTreeNode node, final TaskListener<String> listener) {
        treePhaser.register();
        mLogger.verbose("Given URL to download: " + node.urlPostfix);
        mServerConnector.downloadString(node.urlPostfix, new TaskListener<String>() {
            @Override
            public void onComplete(String downloadedString) {
                try {
                    mLogger.verbose("Download Complete");
                    listener.onComplete(downloadedString);

                } catch (Exception e) {
                    GeneralUtils.logExceptionStackTrace(e);
                }
                finally {
                    treePhaser.arriveAndDeregister();
                    mLogger.verbose("treePhaser.arriveAndDeregister(); " + node.urlPostfix);
                    mLogger.verbose("current unarrived  - " + treePhaser.getUnarrivedParties());
                }
            }

            @Override
            public void onFail() {
                treePhaser.arriveAndDeregister();
                mLogger.verbose("Download Faild");
                mLogger.verbose("treePhaser.arriveAndDeregister(); " + node.urlPostfix);
                mLogger.verbose("current unarrived  - " + treePhaser.getUnarrivedParties());
            }
        });
    }

    private void parseCSS(CssTreeNode node, String css) {
        if (css == null) {
            return;
        }
        CascadingStyleSheet cascadingStyleSheet = CSSReader.readFromString(css, ECSSVersion.CSS30);
        if (cascadingStyleSheet == null) {
            return;
        }
        ICommonsList<ICSSTopLevelRule> allRules = cascadingStyleSheet.getAllRules();
        if (allRules == null) {
            return;
        }

        final CascadingStyleSheet aCSS = CSSReader.readFromString(css, ECSSVersion.CSS30);
        ICommonsList<CSSImportRule> allImportRules = aCSS.getAllImportRules();
        node.setAllImportRules(allImportRules);
        node.setAllStyleRules(aCSS.getAllStyleRules());
    }

    private Map<String, Object> parseStringToMap(String executeScripString) throws IOException {
        Map<String, Object> executeScriptMap;
        ObjectMapper mapper = new ObjectMapper();
        executeScriptMap = mapper.readValue(executeScripString, new TypeReference<Map<String, Object>>() {
        });
        return executeScriptMap;
    }


}



