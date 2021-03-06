/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.websub.basic;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.awaitility.Duration;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;

/**
 * This class tests introducing custom/specific webhooks in projects, extending the WebSub Subscriber Service.
 *
 * @since 1.0
 */
@Test(groups = "websub-test") // not necessary
public class WebSubServiceExtensionProjectTestCase extends WebSubBaseTest {
    private BServerInstance webSubSubscriber;

    private static final String MOCK_HEADER = "MockHeader";
    private static final int LOG_LEECHER_TIMEOUT = 10000;

    private static final String INTENT_VER_REQ_RECEIVED_LOG = "[Project]Intent verification request received";

    private static final String BY_KEY_CREATED_LOG = "[Project]Created Notification Received, action: created";
    private static final String BY_KEY_FEATURE_LOG = "[Project]Feature Notification Received, domain: feature";

    private static final String BY_HEADER_ISSUE_LOG =
            "[Project]Issue Notification Received, header value: issue action: deleted";
    private static final String BY_HEADER_COMMIT_LOG =
            "[Project]Commit Notification Received, header value: commit action: created";

    private static final String BY_HEADER_AND_PAYLOAD_ISSUE_CREATED_LOG =
            "[Project]Issue Created Notification Received, header value: issue action: created";
    private static final String BY_HEADER_AND_PAYLOAD_FEATURE_PULL_LOG =
            "[Project]Feature Pull Notification Received, header value: pull domain: feature";
    private static final String BY_HEADER_AND_PAYLOAD_HEADER_ONLY_LOG =
            "[Project]HeaderOnly Notification Received, header value: headeronly action: header_only";
    private static final String BY_HEADER_AND_PAYLOAD_KEY_ONLY_LOG =
            "[Project]KeyOnly Notification Received, header value: key_only action: keyonly";

    private LogLeecher intentVerReqReceivedLogLeecher = new LogLeecher(INTENT_VER_REQ_RECEIVED_LOG);

    private LogLeecher byKeyCreatedLogLeecher = new LogLeecher(BY_KEY_CREATED_LOG);
    private LogLeecher byKeyFeatureLogLeecher = new LogLeecher(BY_KEY_FEATURE_LOG);

    private LogLeecher byHeaderIssueLogLeecher = new LogLeecher(BY_HEADER_ISSUE_LOG);
    private LogLeecher byHeaderCommitLogLeecher = new LogLeecher(BY_HEADER_COMMIT_LOG);

    private LogLeecher byHeaderAndPayloadIssueCreatedLogLeecher =
            new LogLeecher(BY_HEADER_AND_PAYLOAD_ISSUE_CREATED_LOG);
    private LogLeecher byHeaderAndPayloadFeaturePullLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_FEATURE_PULL_LOG);
    private LogLeecher byHeaderAndPayloadHeaderOnlyLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_HEADER_ONLY_LOG);
    private LogLeecher byHeaderAndPayloadKeyOnlyLogLeecher = new LogLeecher(BY_HEADER_AND_PAYLOAD_KEY_ONLY_LOG);

    @BeforeClass
    public void setup() throws BallerinaTestException {
        webSubSubscriber = new BServerInstance(balServer);

        String sourceRoot = new File("src" + File.separator + "test" + File.separator + "resources" +
                                        File.separator + "websub" + File.separator + "subscriber" + File.separator + 
                                             "custom-subs-proj").getAbsolutePath();

        webSubSubscriber.addLogLeecher(intentVerReqReceivedLogLeecher);
        webSubSubscriber.addLogLeecher(byKeyCreatedLogLeecher);
        webSubSubscriber.addLogLeecher(byKeyFeatureLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderIssueLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderCommitLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadIssueCreatedLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadHeaderOnlyLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadFeaturePullLogLeecher);
        webSubSubscriber.addLogLeecher(byHeaderAndPayloadKeyOnlyLogLeecher);

        webSubSubscriber.startServer(sourceRoot, "custom_subs_mod", new int[]{23595, 23596, 23597});
        // Wait for the services to start up
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);

        given().ignoreException(ConnectException.class).with().pollInterval(Duration.ONE_SECOND).await()
                .atMost(60, SECONDS).until(() -> {
            HttpResponse response = HttpClientRequest.doPost("http://localhost:23595/key",
                                                             "{\"action\":\"statuscheck\"}", headers);
            return response.getResponseCode() == 202;
        });

        given().ignoreException(ConnectException.class).with().pollInterval(Duration.ONE_SECOND).await()
                .atMost(60, SECONDS).until(() -> {
            headers.put(MOCK_HEADER, "status");
            HttpResponse response = HttpClientRequest.doPost("http://localhost:23596/header",
                                                             "{\"action\":\"deleted\"}",
                                                             headers);
            return response.getResponseCode() == 202;
        });

        given().ignoreException(ConnectException.class).with().pollInterval(Duration.ONE_SECOND).await()
                .atMost(60, SECONDS).until(() -> {
            headers.remove(MOCK_HEADER);
            headers.put(MOCK_HEADER, "status");
            HttpResponse response = HttpClientRequest.doPost("http://localhost:23597/headerAndPayload",
                                                             "{\"action\":\"created\"}", headers);
            return response.getResponseCode() == 202;
        });
    }

    @Test
    public void testOnIntentVerificationInvocation() throws IOException, BallerinaTestException {
        HttpClientRequest.doGet("http://localhost:23595/key");
        intentVerReqReceivedLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDispatchingByKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost("http://localhost:23595/key", "{\"action\":\"created\"}",
                                                         headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byKeyCreatedLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);

        response = HttpClientRequest.doPost("http://localhost:23595/key",
                                            "{\"domain\":\"feature\",\"extra\":{\"value\":\"feature\"}}",
                                            headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byKeyFeatureLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDispatchingByHeader() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "issue");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:23596/header", "{\"action\":\"deleted\"}",
                                                         headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderIssueLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);

        headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "commit");
        response = HttpClientRequest.doPost("http://localhost:23596/header", "{\"action\":\"created\"}", headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderCommitLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "issue");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:23597/headerAndPayload",
                                                         "{\"action\":\"created\"}", headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderAndPayloadIssueCreatedLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);

        headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "pull");
        response = HttpClientRequest.doPost("http://localhost:23597/headerAndPayload",
                                            "{\"domain\":\"feature\",\"extra\":{\"value\":\"feature\"}}",
                                            headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderAndPayloadFeaturePullLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKeyForOnlyHeader() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "headeronly");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:23597/headerAndPayload",
                                                         "{\"action\":\"header_only\"}", headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderAndPayloadHeaderOnlyLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @Test
    public void testDispatchingByHeaderAndPayloadKeyForOnlyKey() throws BallerinaTestException, IOException {
        Map<String, String> headers = new HashMap<>(2);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        headers.put(MOCK_HEADER, "key_only");
        HttpResponse response = HttpClientRequest.doPost("http://localhost:23597/headerAndPayload",
                                                         "{\"action\":\"keyonly\"}", headers);
        Assert.assertEquals(response.getResponseCode(), HttpResponseStatus.ACCEPTED.code());
        byHeaderAndPayloadHeaderOnlyLogLeecher.waitForText(LOG_LEECHER_TIMEOUT);
    }

    @AfterClass
    private void teardown() throws Exception {
        webSubSubscriber.shutdownServer();
    }
}
