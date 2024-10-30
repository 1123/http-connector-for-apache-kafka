/*
 * Copyright 2023 Aiven Oy and http-connector-for-apache-kafka project contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.aiven.kafka.connect.http.sender;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.aiven.kafka.connect.http.config.HttpSinkConfig;

import org.apache.http.HttpEntity;
import org.junit.jupiter.api.Test;

import static io.aiven.kafka.connect.http.sender.AbstractHttpSender.fileMultiPartEntity;

public class TestSendAsFileUpload {

    @Test
    public void theDefaultHttpSenderShouldSendMessagesAsFileAttachmentsWhenConfiguredAccordingly() {
        // TODO: Test cases should be independent of any external projects.
        // There is a spring file upload project available at
        // https://github.com/spring-guides/gs-uploading-files.git,
        // which is used for testing in this class.
        final var config = new HttpSinkConfig(Map.of(
                "http.url", "http://localhost:8080/",
                "http.authorization.type", "none",
                "send.as.file.upload", "true"
        ));
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpSender httpSender = new DefaultHttpSender(config, httpClient);
        final var result = httpSender.send("foo");
        System.out.println(result);
    }

    private HttpResponse<String> sendFile(
            final HttpClient httpClient, final HttpEntity httpEntity)
            throws URISyntaxException, IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:8080"))
                .header("Content-Type", httpEntity.getContentType().getValue())
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                new String(httpEntity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                        )
                ).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @Test
    public void theFileMultiPartEntityShouldBeSendableViaAnHttpClient()
            throws URISyntaxException, IOException, InterruptedException {
        final HttpEntity httpEntity = fileMultiPartEntity("this is the message");
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpResponse<String> responseBody = sendFile(httpClient, httpEntity);
        System.out.println("body: " + responseBody.body());
    }

}
