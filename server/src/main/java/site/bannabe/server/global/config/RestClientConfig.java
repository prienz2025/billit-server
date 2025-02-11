package site.bannabe.server.global.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.DefaultBackoffStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClient() {
    return RestClient.builder().requestFactory(clientHttpRequestFactory()).build();
  }

  private ClientHttpRequestFactory clientHttpRequestFactory() {
    return new HttpComponentsClientHttpRequestFactory(httpClient());
  }

  private HttpClient httpClient() {
    return HttpClients.custom()
                      .setConnectionManager(connectionManager())
                      .setDefaultRequestConfig(requestConfig())
                      .setConnectionBackoffStrategy(new DefaultBackoffStrategy())
                      .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                      .setRetryStrategy(new DefaultHttpRequestRetryStrategy(1, TimeValue.ofSeconds(1L)))
                      .evictIdleConnections(TimeValue.ofSeconds(30L))
                      .build();

  }

  private PoolingHttpClientConnectionManager connectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(50); // Max Connection Pool Size
    connectionManager.setDefaultMaxPerRoute(25); // Max Connection Pool Size per Route
    connectionManager.setDefaultSocketConfig(SocketConfig.DEFAULT); // Socket Config
    connectionManager.setDefaultConnectionConfig(connectionConfig()); // Connection Config
    return connectionManager;
  }

  private RequestConfig requestConfig() {
    return RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(3L)) // Connection Poll에서 Connection을 얻기까지 Timeout
                        .setResponseTimeout(Timeout.ofSeconds(5L)) // 서버로부터 응답을 기다리는 Timeout
                        .build();
  }

  private ConnectionConfig connectionConfig() {
    return ConnectionConfig.custom()
                           .setConnectTimeout(Timeout.ofSeconds(5L)) // 서버에 연결을 시도할 때의 Timeout
                           .build();
  }
}