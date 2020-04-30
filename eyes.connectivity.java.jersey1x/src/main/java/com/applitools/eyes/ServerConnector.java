/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.async.TypeListener;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Provides an API for communication with the Applitools agent
 */
public class ServerConnector extends RestClient
        implements IServerConnector {

    private static final int TIMEOUT = 1000 * 60 * 5; // 5 Minutes
    private static final String API_PATH = "/api/sessions/running";
    private static final String DEFAULT_CHARSET_NAME = "UTF-8";

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
    @SuppressWarnings("WeakerAccess")
    public ServerConnector(Logger logger) {
        this(logger, GeneralUtils.getDefaultServerUrl());
    }

    /***
     * @param serverUrl The URI of the Eyes server.
     */
    public ServerConnector(URI serverUrl) {
        this(null, serverUrl);
    }

    public ServerConnector() {
        this((Logger) null);
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
     *
     * @param abstractProxySettings The proxy settings to be used by the rest client.
     *                      If {@code null} then no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setProxy(AbstractProxySettings abstractProxySettings) {
        setProxyBase(abstractProxySettings);
        // After the server is updated we must make sure the endpoint refers
        // to the correct path.
        endPoint = endPoint.path(API_PATH);
    }

    /**
     * @return The current proxy settings used by the rest client,
     * or {@code null} if no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public AbstractProxySettings getProxy() {
        return getProxyBase();
    }

    /**
     * Sets the current server URL used by the rest client.
     *
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
     * session
     * @throws EyesException For invalid status codes, or if response parsing
     *                       failed.
     */
    public RunningSession startSession(SessionStartInfo sessionStartInfo)
            throws EyesException {

        ArgumentGuard.notNull(sessionStartInfo, "sessionStartInfo");

        logger.verbose("Using Jersey1 for REST API calls.");

        String postData;
        ClientResponse response;
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
            WebResource.Builder builder = endPoint.queryParam("apiKey", getApiKey()).accept(MediaType.APPLICATION_JSON);
            response = sendLongRequest(builder, HttpMethod.POST, postData, MediaType.APPLICATION_JSON);
        } catch (RuntimeException e) {
            logger.log("startSession(): Server request failed: " + e.getMessage());
            throw e;
        }

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(ClientResponse.Status.OK.getStatusCode());
        validStatusCodes.add(ClientResponse.Status.CREATED.getStatusCode());

        runningSession = parseResponseWithJsonData(response, validStatusCodes,
                RunningSession.class);

        // If this is a new session, we set this flag.
        statusCode = response.getStatus();
        isNewSession = runningSession.getIsNew() != null ?
                runningSession.getIsNew() :
                (statusCode == Response.Status.CREATED.getStatusCode());
        runningSession.setIsNewSession(isNewSession);

        return runningSession;
    }

    /**
     * Stops the running session.
     *
     * @param runningSession The running session to be stopped.
     * @return TestResults object for the stopped running session
     * @throws EyesException For invalid status codes, or if response parsing
     *                       failed.
     */
    public TestResults stopSession(final RunningSession runningSession,
                                   final boolean isAborted, final boolean save)
            throws EyesException {

        ArgumentGuard.notNull(runningSession, "runningSession");

        final String sessionId = runningSession.getId();
        ClientResponse response;
        List<Integer> validStatusCodes;
        TestResults result;

        WebResource.Builder builder = endPoint.path(sessionId)
                .queryParam("apiKey", getApiKey())
                .queryParam("aborted", String.valueOf(isAborted))
                .queryParam("updateBaseline", String.valueOf(save))
                .accept(MediaType.APPLICATION_JSON);

        response = sendLongRequest(builder, HttpMethod.DELETE, null, null);

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(ClientResponse.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes,
                TestResults.class);
        return result;
    }

    @Override
    public void deleteSession(TestResults testResults) {
        ArgumentGuard.notNull(testResults, "testResults");

        WebResource sessionsResources = restClient.resource(serverUrl);
        WebResource.Builder builder = sessionsResources
                .path("/api/sessions/batches/")
                .path(testResults.getBatchId())
                .path("/")
                .path(testResults.getId())
                .queryParam("apiKey", getApiKey())
                .queryParam("AccessToken", testResults.getSecretToken())
                .accept(MediaType.APPLICATION_JSON);

        builder.delete();
    }

    /**
     * Matches the current window (held by the WebDriver) to the expected
     * window.
     *
     * @param runningSession The current agent's running session.
     * @param matchData      Encapsulation of a capture taken from the application.
     * @return The results of the window matching.
     * @throws EyesException For invalid status codes, or response parsing
     *                       failed.
     */
    public MatchResult matchWindow(RunningSession runningSession,
                                   MatchWindowData matchData)
            throws EyesException {
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.notNull(matchData, "model");

        ClientResponse response;
        List<Integer> validStatusCodes;
        MatchResult result;
        String jsonData;

        // since we rather not add an empty "tag" param
        WebResource runningSessionsEndpoint =
                endPoint.path(runningSession.getId());

        // Serializing model into JSON (we'll treat it as binary later).
        try {
            jsonData = jsonMapper.writeValueAsString(matchData);
        } catch (IOException e) {
            throw new EyesException("Failed to serialize data for matchWindow!",
                    e);
        }

        // Sending the request
        WebResource.Builder request = runningSessionsEndpoint.queryParam("apiKey", getApiKey())
                .accept(MediaType.APPLICATION_JSON);

        response = sendLongRequest(request, HttpMethod.POST, jsonData, MediaType.APPLICATION_JSON);

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(ClientResponse.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes, MatchResult.class);

        return result;
    }

    @Override
    public void downloadString(URL uri, boolean isSecondRetry, final IDownloadListener listener) {

        AsyncWebResource target = Client.create().asyncResource(uri.toString());

        AsyncWebResource.Builder request = target.accept(MediaType.WILDCARD);


        request.get(new TypeListener<ClientResponse>(ClientResponse.class) {

            public void onComplete(Future<ClientResponse> f) {
                int status = 0;
                ClientResponse clientResponse = null;
                try {
                    clientResponse = f.get();
                    status = clientResponse.getStatus();
                    if (status > 300) {
                        logger.verbose("Got response status code - " + status);
                        listener.onDownloadFailed();
                        return;
                    }
                    InputStream entityInputStream = clientResponse.getEntityInputStream();

                    StringWriter writer = new StringWriter();

                    IOUtils.copy(entityInputStream, writer, "UTF-8");

                    String theString = writer.toString();

                    listener.onDownloadComplete(theString);

                } catch (Exception e) {
                    GeneralUtils.logExceptionStackTrace(e);
                    logger.verbose("Failed to parse request(status= " + status + ") = "+ clientResponse.getEntity(String.class));
                    listener.onDownloadFailed();

                }
            }

        });
    }

    @Override
    public Map<String, List<Region>> postLocators(VisualLocatorsData visualLocatorsData) {
        WebResource target = restClient.resource(serverUrl).path("api/locators/locate").queryParam("apiKey", getApiKey());

        String postData;
        try {
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
            postData = jsonMapper.writeValueAsString(visualLocatorsData);
        } catch (IOException e) {
            throw new EyesException("Failed to convert " +
                    "visualLocatorsData into Json string!", e);
        }

        WebResource.Builder request = target.accept(MediaType.APPLICATION_JSON).entity(postData, MediaType.APPLICATION_JSON_TYPE);

        ClientResponse response = sendLongRequest(request, HttpMethod.POST, postData, MediaType.APPLICATION_JSON);

        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        return parseResponseWithJsonData(response, validStatusCodes, new TypeReference<Map<String, List<Region>>>(){});
    }

    @Override
    protected ClientResponse sendHttpWebRequest(String path, final String method, String accept) {
        // Building the request
        WebResource.Builder invocationBuilder = restClient
                .resource(path)
                .queryParam("apikey", getApiKey())
                .accept(accept);

        // Actually perform the method call and return the result
        return invocationBuilder.method(method, ClientResponse.class);
    }

    @Override
    public RenderingInfo getRenderInfo() {
        this.logger.verbose("enter");
        if (renderingInfo == null) {
            WebResource target = restClient.resource(serverUrl).path(RENDER_INFO_PATH).queryParam("apiKey", getApiKey());
            WebResource.Builder request = target.accept(MediaType.APPLICATION_JSON);
            ClientResponse response = request.get(ClientResponse.class);

            // Ok, let's create the running session from the response
            List<Integer> validStatusCodes = new ArrayList<>(1);
            validStatusCodes.add(ClientResponse.Status.OK.getStatusCode());

            renderingInfo = parseResponseWithJsonData(response, validStatusCodes, RenderingInfo.class);
        }
        return renderingInfo;
    }

    @Override
    public int uploadData(byte[] bytes, RenderingInfo renderingInfo, String targetUrl, String contentType, String mediaType) {
        WebResource target = restClient.resource(targetUrl);
        WebResource.Builder request = target
                .accept(mediaType)
                .entity(bytes, contentType)
                .header("X-Auth-Token", renderingInfo.getAccessToken())
                .header("x-ms-blob-type", "BlockBlob");

        ClientResponse response = request.put(ClientResponse.class);
        int statusCode = response.getStatus();
        response.close();
        logger.verbose("Upload Status Code: " + statusCode);
        return statusCode;
    }
}
