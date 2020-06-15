package com.applitools.connectivity;

import com.applitools.connectivity.api.*;
import com.applitools.eyes.*;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.brotli.dec.BrotliInputStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class ServerConnector extends RestClient  implements IServerConnector {

    public static final int DEFAULT_CLIENT_TIMEOUT = 1000 * 60 * 5; // 5 minutes
    public static final int MAX_CONNECTION_RETRIES = 3;

    String API_SESSIONS = "api/sessions";
    String CLOSE_BATCH = "api/sessions/batches/%s/close/bypointerid";

    //Rendering Grid
    String RENDER_INFO_PATH = API_SESSIONS + "/renderinfo";

    public static final String API_PATH = "/api/sessions/running";

    private String apiKey = null;
    private RenderingInfo renderingInfo;

    /***
     * @param logger    Logger instance.
     * @param serverUrl The URI of the rest server.
     */
    public ServerConnector(Logger logger, URI serverUrl, int timeout) {
        super(logger, serverUrl, timeout);
    }

    public ServerConnector(Logger logger, URI serverUrl) {
        this(logger, serverUrl, DEFAULT_CLIENT_TIMEOUT);
    }

    public ServerConnector(Logger logger) {
        this(logger, GeneralUtils.getServerUrl());
    }

    public ServerConnector() {
        this(new Logger());
    }

    /**
     * Sets the API key of your applitools Eyes account.
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
        return this.apiKey != null ? this.apiKey : GeneralUtils.getEnvString("APPLITOOLS_API_KEY");
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentId() {
        return this.agentId;
    }

    /**
     * Sets the current server URL used by the rest client.
     * @param serverUrl The URI of the rest server.
     */
    public void setServerUrl(URI serverUrl) {
        setServerUrlBase(serverUrl);
    }

    public URI getServerUrl() {
        return getServerUrlBase();
    }

    void updateClient(HttpClient client) {
        restClient = client;
    }

    @Override
    public Response sendHttpWebRequest(final String url, final String method, final String... accept) {
        final Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(url).queryParam("apiKey", getApiKey()).request(accept);
            }
        });
        String currentTime = GeneralUtils.toRfc1123(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
        return request
                .header("Eyes-Date", currentTime)
                .method(method, null, null);
    }

    /**
     * Starts a new running session in the agent. Based on the given parameters,
     * this running session will either be linked to an existing session, or to
     * a completely new session.
     * @param sessionStartInfo The start parameters for the session.
     * @return RunningSession object which represents the current running
     * session
     * @throws EyesException For invalid status codes, or if response parsing
     *                       failed.
     */
    public RunningSession startSession(SessionStartInfo sessionStartInfo) throws EyesException {
        ArgumentGuard.notNull(sessionStartInfo, "sessionStartInfo");
        initClient();
        String postData;
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

        Response response;
        try {
            Request request = makeEyesRequest(new HttpRequestBuilder() {
                @Override
                public Request build() {
                    return restClient.target(serverUrl).path(API_PATH)
                            .queryParam("apiKey", getApiKey()).request(MediaType.APPLICATION_JSON);
                }
            });
            response = sendLongRequest(request, HttpMethod.POST, postData, MediaType.APPLICATION_JSON);
        } catch (RuntimeException e) {
            logger.log("startSession(): Server request failed: " + e.getMessage());
            throw e;
        }

        try {
            // Ok, let's create the running session from the response
            List<Integer> validStatusCodes = new ArrayList<>();
            validStatusCodes.add(HttpStatus.SC_OK);
            validStatusCodes.add(HttpStatus.SC_CREATED);

            // If this is a new session, we set this flag.
            RunningSession runningSession = parseResponseWithJsonData(response, validStatusCodes, new TypeReference<RunningSession>() {});
            if (runningSession.getIsNew() == null) {
                runningSession.setIsNew(response.getStatusCode() == HttpStatus.SC_CREATED);
            }
            return runningSession;
        } finally {
            response.close();
        }
    }

    /**
     * Stops the running session.
     * @param runningSession The running session to be stopped.
     * @return TestResults object for the stopped running session
     * @throws EyesException For invalid status codes, or if response parsing
     *                       failed.
     */
    public TestResults stopSession(final RunningSession runningSession,
                                   final boolean isAborted, final boolean save) throws EyesException {
        ArgumentGuard.notNull(runningSession, "runningSession");
        Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(serverUrl).path(API_PATH).path(runningSession.getId())
                        .queryParam("apiKey", getApiKey())
                        .queryParam("aborted", String.valueOf(isAborted))
                        .queryParam("updateBaseline", String.valueOf(save))
                        .request(MediaType.APPLICATION_JSON);
            }
        });
        Response response = sendLongRequest(request, HttpMethod.DELETE, null, null);

        try {
            // Ok, let's create the running session from the response
            List<Integer> validStatusCodes = new ArrayList<>();
            validStatusCodes.add(HttpStatus.SC_OK);
            return parseResponseWithJsonData(response, validStatusCodes, new TypeReference<TestResults>() {});
        } finally {
            response.close();
        }
    }

    public void deleteSession(final TestResults testResults) {
        ArgumentGuard.notNull(testResults, "testResults");
        Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(serverUrl)
                        .path("/api/sessions/batches/")
                        .path(testResults.getBatchId())
                        .path("/")
                        .path(testResults.getId())
                        .queryParam("apiKey", getApiKey())
                        .queryParam("AccessToken", testResults.getSecretToken())
                        .request(MediaType.APPLICATION_JSON);
            }
        });

        Response response = request.method(HttpMethod.DELETE, null, null);
        response.close();
    }

    /**
     * Matches the current window (held by the WebDriver) to the expected
     * window.
     * @param runningSession The current agent's running session.
     * @param matchData      Encapsulation of a capture taken from the application.
     * @return The results of the window matching.
     * @throws EyesException For invalid status codes, or response parsing
     *                       failed.
     */
    public MatchResult matchWindow(final RunningSession runningSession, MatchWindowData matchData) throws EyesException {
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.notNull(matchData, "model");

        // Serializing model into JSON (we'll treat it as binary later).
        String jsonData;
        try {
            jsonData = jsonMapper.writeValueAsString(matchData);
        } catch (IOException e) {
            throw new EyesException("Failed to serialize model for matchWindow!", e);
        }

        Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(serverUrl).path(API_PATH).path(runningSession.getId())
                        .queryParam("apiKey", getApiKey())
                        .request(MediaType.APPLICATION_JSON);
            }
        });

        Response response = sendLongRequest(request, HttpMethod.POST, jsonData, MediaType.APPLICATION_JSON);

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(HttpStatus.SC_OK);

        try {
            return parseResponseWithJsonData(response, validStatusCodes, new TypeReference<MatchResult>() {});
        } finally {
            response.close();
        }
    }

    public Response uploadData(byte[] bytes, RenderingInfo renderingInfo, final String targetUrl, String contentType, final String mediaType) {
        Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(targetUrl).request(mediaType);
            }
        });
        request = request.header("X-Auth-Token", renderingInfo.getAccessToken())
                .header("x-ms-blob-type", "BlockBlob");
        return request.method(HttpMethod.PUT, bytes, contentType);
    }

    public void downloadString(final URL uri, final TaskListener<String> listener) {
        downloadString(uri, listener, 1);
    }

    public void downloadString(final URL uri, final TaskListener<String> listener, final int attemptNumber) {
        AsyncRequest asyncRequest = restClient.target(uri.toString()).asyncRequest(MediaType.WILDCARD);
        asyncRequest.method(HttpMethod.GET, new AsyncRequestCallback() {
            @Override
            public void onComplete(Response response) {
                try {
                    int statusCode = response.getStatusCode();
                    if (statusCode >= 300) {
                        logger.verbose("Got response status code - " + statusCode);
                        listener.onFail();
                        return;
                    }

                    byte[] fileContent = downloadFile(response);
                    listener.onComplete(new String(fileContent));
                } catch (Throwable t) {
                    logger.verbose(t.getMessage());
                } finally {
                    response.close();
                }
            }

            @Override
            public void onFail(Throwable throwable) {
                GeneralUtils.logExceptionStackTrace(logger, throwable);
                if (attemptNumber < MAX_CONNECTION_RETRIES) {
                    logger.verbose(String.format("Failed downloading resource %s - trying again", uri));
                    downloadString(uri, listener, attemptNumber + 1);
                } else {
                    listener.onFail();
                }
            }
        }, null, null, false);
    }

    public RenderingInfo getRenderInfo() {
        if (renderingInfo != null) {
            return renderingInfo;
        }

        Request request = makeEyesRequest(new HttpRequestBuilder() {
            @Override
            public Request build() {
                return restClient.target(serverUrl).path(RENDER_INFO_PATH)
                        .queryParam("apiKey", getApiKey()).request();
            }
        });
        Response response = sendLongRequest(request, HttpMethod.GET, null, null);

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(HttpStatus.SC_OK);

        try {
            renderingInfo = parseResponseWithJsonData(response, validStatusCodes, new TypeReference<RenderingInfo>() {});
            return renderingInfo;
        } finally {
            response.close();
        }
    }

    public String postViewportImage(byte[] bytes) {
        String targetUrl;
        RenderingInfo renderingInfo = getRenderInfo();
        if (renderingInfo != null && (targetUrl = renderingInfo.getResultsUrl()) != null) {
            try {
                UUID uuid = UUID.randomUUID();
                targetUrl = targetUrl.replace("__random__", uuid.toString());
                logger.verbose("uploading viewport image to " + targetUrl);

                for (int i = 0; i < ServerConnector.MAX_CONNECTION_RETRIES; i++) {
                    Response response = uploadData(bytes, renderingInfo, targetUrl, "image/png", "image/png");
                    int statusCode = response.getStatusCode();
                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                        return targetUrl;
                    }

                    String errorMessage = String.format("Status: %d %s.",
                            statusCode, response.getStatusPhrase());

                    if (statusCode < 500) {
                        throw new IOException(String.format("Failed uploading image. %s", errorMessage));
                    }

                    logger.log(String.format("Failed uploading image, retrying. %s", errorMessage));
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                logger.log("Error uploading viewport image");
                GeneralUtils.logExceptionStackTrace(logger, e);
            }
        }
        return null;
    }

    public Map<String, List<Region>> postLocators(VisualLocatorsData visualLocatorsData) {
        String postData;
        try {
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
            postData = jsonMapper.writeValueAsString(visualLocatorsData);
        } catch (IOException e) {
            throw new EyesException("Failed to convert " +
                    "visualLocatorsData into Json string!", e);
        }

        ConnectivityTarget target = restClient.target(serverUrl).path(("api/locators/locate")).queryParam("apiKey", getApiKey());
        Request request = target.request(MediaType.APPLICATION_JSON);
        Response response = sendLongRequest(request, HttpMethod.POST, postData, MediaType.APPLICATION_JSON);
        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(javax.ws.rs.core.Response.Status.OK.getStatusCode());

        return parseResponseWithJsonData(response, validStatusCodes, new TypeReference<Map<String, List<Region>>>(){});
    }

    public void closeBatch(String batchId) {
        boolean dontCloseBatchesStr = GeneralUtils.getDontCloseBatches();
        if (dontCloseBatchesStr) {
            logger.log("APPLITOOLS_DONT_CLOSE_BATCHES environment variable set to true. Skipping batch close.");
            return;
        }
        ArgumentGuard.notNull(batchId, "batchId");
        this.logger.log("called with " + batchId);

        Response response = null;
        try {
            final String url = String.format(CLOSE_BATCH, batchId);
            initClient();
            Request request = makeEyesRequest(new HttpRequestBuilder() {
                @Override
                public Request build() {
                    return restClient.target(serverUrl).path(url)
                            .queryParam("apiKey", getApiKey())
                            .request((String) null);
                }
            });
             response = request.method(HttpMethod.DELETE, null, null);
        } finally {
            if (response != null) {
                response.close();
            }

            restClient.close();
        }
    }

    public void closeConnector() {
        if (restClient != null) {
            restClient.close();
        }
    }

    private byte[] downloadFile(Response response) {
        byte[] responseBody = response.getBody();
        String contentEncoding = response.getHeader("Content-Encoding", false);
        if (!"br".equalsIgnoreCase(contentEncoding)) {
            return responseBody;
        }

        try {
            return IOUtils.toByteArray(new BrotliInputStream(new ByteArrayInputStream(responseBody)));
        } catch (IOException e) {
            GeneralUtils.logExceptionStackTrace(logger, e);
        }
        return new byte[0];
    }
}
