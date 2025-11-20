package mingovvv.common.http.config;

import lombok.RequiredArgsConstructor;
import mingovvv.common.http.client.TestServerClient;
import mingovvv.common.http.interceptor.RestClientLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RestClientLoggingInterceptor restClientLoggingInterceptor;

    @Bean
    public TestServerClient testServerClient(@Value("${api.test-client.url}") String url,
                                             @Value("${api.test-client.connection-timeout:1s}") Duration connectionTimeout,
                                             @Value("${api.test-client.read-timeout:10s}") Duration readTimeout,
                                             @Value("${api.test-client.api-key}") String apiKey) {
        return new RestClientBuilder()
            .url(url)
            .headers("Authorization", "Bearer " + apiKey)
            .connectionTimeout(connectionTimeout)
            .readTimeout(readTimeout)
            .requestInterceptors(restClientLoggingInterceptor)
            .build(TestServerClient.class);
    }

}
