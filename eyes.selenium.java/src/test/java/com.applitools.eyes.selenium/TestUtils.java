package com.applitools.eyes.selenium;

import com.applitools.eyes.EyesBase;
import com.applitools.eyes.Logger;
import com.applitools.eyes.RestClient;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.metadata.ActualAppOutput;
import com.applitools.eyes.metadata.SessionResults;
import com.applitools.utils.ArgumentGuard;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    public static List<Object[]> generatePermutationsList(List<List<Object>> lists) {
        List<Object[]> result = new ArrayList<>();
        generatePermutations(lists, result, 0, null);
        return result;
    }

    public static Object[][] generatePermutations(List<List<Object>> lists) {
        List<Object[]> result = generatePermutationsList(lists);
        return result.toArray(new Object[0][0]);
    }

    @SafeVarargs
    public static Object[][] generatePermutations(List<Object>... lists) {
        return generatePermutations(Arrays.asList(lists));
    }

    private static void generatePermutations(List<List<Object>> lists, List<Object[]> result, int depth, List<Object> permutation) {
        if (depth == lists.size()) {
            if (permutation != null) {
                result.add(permutation.toArray());
            }
            return;
        }

        List<Object> listInCurrentDepth = lists.get(depth);
        for (Object newItem : listInCurrentDepth) {
            if (permutation == null || depth == 0) {
                permutation = new ArrayList<>();
            }

            permutation.add(newItem);
            generatePermutations(lists, result, depth + 1, permutation);
            permutation.remove(permutation.size() - 1);
        }
    }

    public static SessionResults getSessionResults(String apiKey, TestResults results) throws java.io.IOException {
        String apiSessionUrl = results.getApiUrls().getSession();
        URI apiSessionUri = UriBuilder.fromUri(apiSessionUrl)
                .queryParam("format", "json")
                .queryParam("AccessToken", results.getSecretToken())
                .queryParam("apiKey", apiKey)
                .build();

        RestClient client = new RestClient(new Logger(), apiSessionUri);
        String srStr = client.getString(apiSessionUri.toString(), MediaType.APPLICATION_JSON);
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return jsonMapper.readValue(srStr, SessionResults.class);
    }

    public static String getStepDom(EyesBase eyes, ActualAppOutput actualAppOutput) {
        ArgumentGuard.notNull(eyes, "eyes");
        ArgumentGuard.notNull(actualAppOutput, "actualAppOutput");

        String apiSessionUrl = eyes.getServerUrl().toString();
        URI apiSessionUri = UriBuilder.fromUri(apiSessionUrl)
                .path("api/images/dom")
                .path(actualAppOutput.getImage().getDomId())
                .queryParam("apiKey", eyes.getApiKey())
                .build();

        RestClient client = new RestClient(new Logger(), apiSessionUri);
        String result = client.getString(apiSessionUri.toString(), MediaType.APPLICATION_JSON);
        return result;
    }

}
