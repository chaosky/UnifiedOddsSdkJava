/*
 * Copyright (C) Sportradar AG. See LICENSE for full license governing this code
 */

package com.sportradar.unifiedodds.sdk.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sportradar.unifiedodds.sdk.SDKInternalConfiguration;
import com.sportradar.unifiedodds.sdk.exceptions.internal.CommunicationException;
import com.sportradar.unifiedodds.sdk.impl.apireaders.HttpHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to fetch content from the Unified API, the output of this.get() is usually
 * used in combination with a {@link Deserializer} to get a valid useful Java object.
 */
@SuppressWarnings(
    {
        "BooleanExpressionComplexity",
        "ConstantName",
        "LineLength",
        "MethodLength",
        "NPathComplexity",
        "UnnecessaryParentheses",
        "CyclomaticComplexity",
    }
)
abstract class HttpDataFetcher {

    private static final Logger logger = LoggerFactory.getLogger(HttpDataFetcher.class);
    private final SDKInternalConfiguration config;
    private final CloseableHttpClient httpClient;
    private final UnifiedOddsStatistics statsBean;
    private final Deserializer apiDeserializer;

    HttpDataFetcher(
        SDKInternalConfiguration config,
        CloseableHttpClient httpClient,
        UnifiedOddsStatistics statsBean,
        Deserializer apiDeserializer
    ) {
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(httpClient);
        Preconditions.checkNotNull(statsBean);
        Preconditions.checkNotNull(apiDeserializer);

        this.config = config;
        this.httpClient = httpClient;
        this.statsBean = statsBean;
        this.apiDeserializer = apiDeserializer;
    }

    /**
     * Gets the content on the given path trough a GET request
     *
     * @param path a valid HTTP GET request path
     * @return the content of the request
     */
    public HttpData get(String path) throws CommunicationException {
        return send(new HttpGet(path));
    }

    /**
     * Gets the content on the given path trough a POST request
     *
     * @param path a valid HTTP POST request path
     * @param content a content to send using POST request
     * @return the content of the request
     */
    public HttpData post(String path, HttpEntity content) throws CommunicationException {
        HttpPost httpPost = new HttpPost(path);
        httpPost.setEntity(content);
        return send(httpPost);
    }

    /**
     * Gets the content on the given HTTP request
     *
     * @param request a valid HTTP request
     * @return the content of the request
     */
    protected HttpData send(HttpRequestBase request) throws CommunicationException {
        String path = request.getURI().toString();
        try {
            if (statsBean != null) {
                statsBean.onStreamingHttpGet(path);
            }
            request.addHeader("x-access-token", config.getAccessToken());

            CloseableHttpResponse resp = null;
            String respString = null;
            String errorMessage = null;
            int statusCode;
            try {
                resp = httpClient.execute(request);
                statusCode = resp.getStatusLine().getStatusCode();

                // the whoami endpoint is a special case since we are interested in the response even if the response code is forbidden
                boolean isWhoAmI = path.endsWith("whoami.xml");

                if (
                    statusCode == HttpStatus.SC_OK ||
                    statusCode == HttpStatus.SC_ACCEPTED ||
                    (isWhoAmI && statusCode == HttpStatus.SC_FORBIDDEN)
                ) {
                    respString = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                } else {
                    errorMessage =
                        HttpHelper.tryDeserializeResponseMessage(
                            apiDeserializer,
                            resp.getEntity().getContent()
                        );
                    logger.warn(
                        "Bad API response: " +
                        resp.getStatusLine() +
                        " " +
                        statusCode +
                        ", message: '" +
                        errorMessage +
                        "' " +
                        path
                    );
                }
            } finally {
                if (resp != null) {
                    resp.close();
                }
            }

            if (!Strings.isNullOrEmpty(respString)) {
                return new HttpData(respString, resp.getAllHeaders());
            } else {
                if (Strings.isNullOrEmpty(errorMessage)) {
                    errorMessage = "no message";
                }
                throw new CommunicationException(
                    "Invalid server response. Message=" + errorMessage,
                    path,
                    statusCode
                );
            }
        } catch (IOException e) {
            throw new CommunicationException("There was a problem retrieving the requested data", path, e);
        } catch (CommunicationException e) {
            throw e;
        } finally {
            request.releaseConnection();
        }
    }
}
