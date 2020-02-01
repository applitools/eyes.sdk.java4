/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides an API for communication with the Applitools agent
 */
public class ServerConnector extends RestClient
        implements IServerConnector {

    private static final int TIMEOUT = 1000 * 60 * 5; // 5 Minutes
    private static final String API_PATH = "/api/sessions/running";
    private static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final int THREAD_SLEEP_MILLIS = 3;
    private static final int NUM_OF_RETRIES = 100;

    private String apiKey = null;
    private RenderingInfo renderingInfo;

    /***
     * @param logger A logger instance.
     * @param serverUrl The URI of the Eyes server.
     */
    public ServerConnector(Logger logger, URI serverUrl) {
        super(logger, serverUrl, TIMEOUT);

        endPoint = endPoint.path(API_PATH);
    }

    /***
     * @param logger A logger instance.
     */
    public ServerConnector(Logger logger) {
        this(logger, GeneralUtils.getDefaultServerUrl());
    }

    /***
     * @param serverUrl The URI of the Eyes server.
     */
    public ServerConnector(URI serverUrl) {
        this(null, serverUrl);
    }

    public ServerConnector(){
        this((Logger)null);
    }

    /**
     * Sets the API key of your applitools Eyes account.
     *
     * @param apiKey The api key to set.
     */
    public void setApiKey(String apiKey) {
        ArgumentGuard.notNull(apiKey, "apiKey");
        this.apiKey = apiKey;
    }

    /**
     * @return The currently set API key or {@code null} if no key is set.
     */
    public String getApiKey() {
        return this.apiKey != null ? this.apiKey : GeneralUtils.getEnvString(GeneralUtils.APPLITOOLS_API_KEY);
    }

    /**
     * Sets the proxy settings to be used by the rest client.
     * @param abstractProxySettings The proxy settings to be used by the rest client.
     * If {@code null} then no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setProxy(AbstractProxySettings abstractProxySettings) {
        setProxyBase(abstractProxySettings);
        // After the server is updated we must make sure the endpoint refers
        // to the correct path.
        endPoint = endPoint.path(API_PATH);
    }

    /**
     *
     * @return The current proxy settings used by the rest client,
     * or {@code null} if no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public AbstractProxySettings getProxy() {
        return getProxyBase();
    }

    /**
     * Sets the current server URL used by the rest client.
     * @param serverUrl The URI of the rest server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setServerUrl(URI serverUrl) {
        setServerUrlBase(serverUrl);
        // After the server is updated we must make sure the endpoint refers
        // to the correct path.
        endPoint = endPoint.path(API_PATH);
    }

    /**
     * @return The URI of the eyes server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public URI getServerUrl() {
        return getServerUrlBase();
    }

    /**
     * Starts a new running session in the agent. Based on the given parameters,
     * this running session will either be linked to an existing session, or to
     * a completely new session.
     *
     * @param sessionStartInfo The start parameters for the session.
     * @return RunningSession object which represents the current running
     *         session
     * @throws EyesException For invalid status codes, or if response parsing
     *          failed.
     */
    public RunningSession startSession(SessionStartInfo sessionStartInfo)
            throws EyesException {

        ArgumentGuard.notNull(sessionStartInfo, "sessionStartInfo");

        logger.verbose("Using JBoss for REST API calls.");

        String postData;
        Response response;
        int statusCode;
        List<Integer> validStatusCodes;
        boolean isNewSession;
        RunningSession runningSession;

        try {

            // since the web API requires a root property for this message
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            postData = jsonMapper.writeValueAsString(sessionStartInfo);

            // returning the root property addition back to false (default)
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        } catch (IOException e) {
            throw new EyesException("Failed to convert " +
                    "sessionStartInfo into Json string!", e);
        }

        try {
            Invocation.Builder request = endPoint.queryParam("apiKey", getApiKey()).
                    request(MediaType.APPLICATION_JSON);
            response = postWithRetry(request, Entity.json(postData), null);
        } catch (RuntimeException e) {
            logger.log("Server request failed: " + e.getMessage());
            throw e;
        }

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());
        validStatusCodes.add(Response.Status.CREATED.getStatusCode());

        runningSession = parseResponseWithJsonData(response, validStatusCodes,
                RunningSession.class);

        // If this is a new session, we set this flag.
        statusCode = response.getStatus();
        isNewSession = (statusCode == Response.Status.CREATED.getStatusCode());
        runningSession.setIsNewSession(isNewSession);

        return runningSession;
    }

    /**
     * Stops the running session.
     *
     * @param runningSession The running session to be stopped.
     * @return TestResults object for the stopped running session
     * @throws EyesException For invalid status codes, or if response parsing
     *          failed.
     */
    public TestResults stopSession(final RunningSession runningSession,
                                   final boolean isAborted, final boolean save)
            throws EyesException {

        ArgumentGuard.notNull(runningSession, "runningSession");

        final String sessionId = runningSession.getId();
        Response response;
        List<Integer> validStatusCodes;
        TestResults result;

        Invocation.Builder invocationBuilder = endPoint.path(sessionId)
                .queryParam("apiKey", getApiKey())
                .queryParam("aborted", String.valueOf(isAborted))
                .queryParam("updateBaseline", String.valueOf(save))
                .request(MediaType.APPLICATION_JSON);

        response = sendLongRequest(invocationBuilder, HttpMethod.DELETE, null);

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes,
                TestResults.class);
        return result;
    }

    @Override
    public void deleteSession(TestResults testResults) {
        ArgumentGuard.notNull(testResults, "testResults");

        Invocation.Builder invocationBuilder = restClient.target(serverUrl)
                .path("/api/sessions/batches/")
                .path(testResults.getBatchId())
                .path("/")
                .path(testResults.getId())
                .queryParam("apiKey", getApiKey())
                .queryParam("AccessToken", testResults.getSecretToken())
                .request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.delete();
    }

    /**
     * Matches the current window (held by the WebDriver) to the expected
     * window.
     *
     * @param runningSession The current agent's running session.
     * @param matchData Encapsulation of a capture taken from the application.
     * @return The results of the window matching.
     * @throws EyesException For invalid status codes, or response parsing
     * failed.
     */
    public MatchResult matchWindow(RunningSession runningSession,
                                   MatchWindowData matchData)
            throws EyesException {
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.notNull(matchData, "model");

        Response response;
        List<Integer> validStatusCodes;
        MatchResult result;
        String jsonData;

        // since we rather not add an empty "tag" param
        WebTarget runningSessionsEndpoint =
                endPoint.path(runningSession.getId());

        // Serializing model into JSON (we'll treat it as binary later).
        try {
            jsonData = jsonMapper.writeValueAsString(matchData);
        } catch (IOException e) {
            throw new EyesException("Failed to serialize data for matchWindow!",
                                    e);
        }

        // Sending the request
        Invocation.Builder request = runningSessionsEndpoint.queryParam("apiKey", getApiKey())
                .request(MediaType.APPLICATION_JSON);

        response = sendLongRequest(request, HttpMethod.POST, Entity.entity(jsonData, MediaType.APPLICATION_JSON));

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes,
                MatchResult.class);

        return result;
    }

    @Override
    public void downloadString(final URL uri, final boolean isSecondRetry, final IDownloadListener listener) {

        Client client = ClientBuilder.newBuilder().build();

        WebTarget target = client.target(uri.toString());

        Invocation.Builder request = target.request(MediaType.WILDCARD);

        request.async().get(new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                logger.verbose(uri + " - completed");
                listener.onDownloadComplete(response);
            }

            @Override
            public void failed(Throwable throwable) {
                GeneralUtils.logExceptionStackTrace(new Exception(throwable));
                if (!isSecondRetry) {
                    logger.verbose("Entring retry");
                    downloadString(uri, true, listener);
                }
                else {
                    listener.onDownloadFailed();
                }
            }
        });


    }

    @Override
    public String postDomSnapshot(String domJson) {

        WebTarget target = restClient.target(serverUrl).path(("api/sessions/running/data")).queryParam("apiKey", getApiKey());

        byte[] resultStream = GeneralUtils.getGzipByteArrayOutputStream(domJson);

        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        Response response = postWithRetry(request, Entity.entity(resultStream,
                MediaType.APPLICATION_OCTET_STREAM), null);

        String entity = response.getHeaderString("Location");

        return entity;
    }

    @Override
    public String postViewportImage(String base64Image) {
        WebTarget target = restClient.target(serverUrl).path(("api/sessions/running/data")).queryParam("apiKey", getApiKey());

        byte[] screenshot = Base64.decodeBase64(base64Image);

        ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();
        DataOutputStream requestDos = new DataOutputStream(requestOutputStream);
        byte[] requestData;
        String result = null;
        try {
            requestOutputStream.write(screenshot);
            requestOutputStream.flush();

            requestData = requestOutputStream.toByteArray();
            requestDos.close();

            Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

            Response response = postWithRetry(request, Entity.entity(requestData, MediaType.APPLICATION_OCTET_STREAM), null);
            result = response.getHeaderString("Location");
        } catch (IOException e) {
            logger.verbose("Failed to send viewport image");
        }
        return result;
    }

    @Override
    public Map<String, List<Region>> postLocators(VisualLocatorsData visualLocatorsData) {
        WebTarget target = restClient.target(serverUrl).path("api/locators/locate").queryParam("apiKey", getApiKey());

        String postData;
        try {
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
            postData = jsonMapper.writeValueAsString(visualLocatorsData);
        } catch (IOException e) {
            throw new EyesException("Failed to convert " +
                    "visualLocatorsData into Json string!", e);
        }

        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        List<Integer> validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        Response response = postWithRetry(request, Entity.json(postData), null);
        return parseResponseWithJsonData(response, validStatusCodes, new TypeReference<Map<String, List<Region>>>(){});
    }

    @Override
    public RenderingInfo getRenderInfo() {
        if (renderingInfo == null) {
            String apiKey = getApiKey();
            WebTarget target = restClient.target(serverUrl).path((RENDER_INFO_PATH)).queryParam("apiKey", apiKey);
            Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

            // Ok, let's create the running session from the response
            List<Integer> validStatusCodes = new ArrayList<>();
            validStatusCodes.add(Response.Status.OK.getStatusCode());

            Response response = request.get();
            renderingInfo = parseResponseWithJsonData(response, validStatusCodes, RenderingInfo.class);
        }
        return renderingInfo;
    }

    @Override
    public int uploadImage(byte[] screenshotBytes, RenderingInfo renderingInfo, String imageTargetUrl) {
        WebTarget target = restClient.target(imageTargetUrl);
        Invocation.Builder request = target
                .request("image/png")
                .accept("image/png")
                .header("X-Auth-Token", renderingInfo.getAccessToken())
                .header("x-ms-blob-type", "BlockBlob");

        Response response = request.put(Entity.entity(screenshotBytes, "image/png"));
        int statusCode = response.getStatus();
        response.close();
        logger.verbose("Upload Status Code: " + statusCode);
        return statusCode;
    }

    private Response postWithRetry(Invocation.Builder request, Entity entity, AtomicInteger retiresCounter) {

        if (retiresCounter == null) {

            retiresCounter = new AtomicInteger(0);

        }
        try {
            return request.
                    post(entity);
        } catch (Exception e) {

            GeneralUtils.logExceptionStackTrace(e);
            try {

                Thread.sleep(THREAD_SLEEP_MILLIS);

            } catch (InterruptedException e1) {
                GeneralUtils.logExceptionStackTrace(e);
            }

            if(retiresCounter.incrementAndGet() < NUM_OF_RETRIES){

                return postWithRetry(request, entity, retiresCounter);
            }
            else{
                throw e;
            }
        }

    }

    @Override
    protected Response sendHttpWebRequest(String path, final String method, String accept) {
        // Building the request
        Invocation.Builder invocationBuilder = restClient
                .target(path)
                .queryParam("apikey", getApiKey())
                .request(accept);

        // Actually perform the method call and return the result
        return invocationBuilder.method(method);
    }
}
