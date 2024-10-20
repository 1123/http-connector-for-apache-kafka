/*
 * Copyright 2021 Aiven Oy and http-connector-for-apache-kafka project contributors
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

package io.aiven.kafka.connect.http.sender.request;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

public class OAuth2AccessTokenRequestForm {

    private static final String SCOPE = "scope";

    private final String grantTypeProperty;
    private final String grantType;

    private final String scope;
    private final String clientIdProperty;
    private final String clientId;

    private final String clientSecretProperty;
    private final String clientSecret;

    private final String username;
    private final String password;

    private OAuth2AccessTokenRequestForm(
        final String grantTypeProperty,
        final String grantType,
        final String scope,
        final String clientIdProperty,
        final String clientId,
        final String clientSecretProperty,
        final String clientSecret,
        final String username,
        final String password
    ) {
        this.grantTypeProperty = grantTypeProperty;
        this.grantType = grantType;
        this.scope = scope;
        this.clientIdProperty = clientIdProperty;
        this.clientId = clientId;
        this.clientSecretProperty = clientSecretProperty;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
    }

    public String toBodyString() {
        final StringJoiner stringJoiner = new StringJoiner("&").add(encodeNameAndValue(grantTypeProperty, grantType));
        if (scope != null) {
            stringJoiner.add(encodeNameAndValue(SCOPE, scope));
        }
        if (clientId != null && clientSecret != null) {
            stringJoiner
                .add(encodeNameAndValue(clientIdProperty, clientId))
                .add(encodeNameAndValue(clientSecretProperty, clientSecret));
        }
        if (username != null && password != null) {
            // TODO: we might want to make this configurable in the future.
            // For the moment the attribute names are hard-coded.
            stringJoiner
                    .add(encodeNameAndValue("username", username))
                    .add(encodeNameAndValue("password", password));
        }
        return stringJoiner.toString();
    }

    private String encodeNameAndValue(final String name, final String value) {
        return String.format("%s=%s", encode(name), encode(value));
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String grantTypeProperty;
        private String grantType;

        private String scope;
        private String clientIdProperty;
        private String clientId;

        private String clientSecretProperty;
        private String clientSecret;

        private String username;
        private String password;

        private Builder() {
        }

        public Builder withGrantTypeProperty(final String grantTypeProperty) {
            this.grantTypeProperty = grantTypeProperty;
            return this;
        }

        public Builder withGrantType(final String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder withScope(final String scope) {
            this.scope = scope;
            return this;
        }

        public Builder withClientIdProperty(final String clientIdProperty) {
            this.clientIdProperty = clientIdProperty;
            return this;
        }

        public Builder withClientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withClientSecretProperty(final String clientSecretProperty) {
            this.clientSecretProperty = clientSecretProperty;
            return this;
        }

        public Builder withClientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public OAuth2AccessTokenRequestForm build() {
            Objects.requireNonNull(grantTypeProperty, "The grant type property is required");
            Objects.requireNonNull(grantType, "The grant type is required");

            if (grantType.equals("client_credentials")) {
                // Both of the credential properties need to be set
                if (clientIdProperty != null || clientSecretProperty != null) {
                    Objects.requireNonNull(clientIdProperty, "The client id property is required");
                    Objects.requireNonNull(clientSecretProperty, "The client secret property is required");
                }
                // Both of the credential values need to be set
                if (clientId != null || clientSecret != null) {
                    Objects.requireNonNull(clientId, "The client id is required");
                    Objects.requireNonNull(clientSecret, "The client secret is required");
                }
            }

            if (grantType.equals("password")) {
                // Both of the credential values need to be set
                if (username != null || password != null) {
                    Objects.requireNonNull(username, "With grant_type password, the username must be set.");
                    Objects.requireNonNull(password, "With grant_type password, the password must be set. ");
                }

            }

            return new OAuth2AccessTokenRequestForm(
                grantTypeProperty, grantType, scope,
                    clientIdProperty, clientId, clientSecretProperty, clientSecret,
                    username, password
            );
        }

    }

}
