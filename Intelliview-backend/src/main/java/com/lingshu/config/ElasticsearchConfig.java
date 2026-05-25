// config/ElasticsearchConfig.java
package com.lingshu.config;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@Lazy
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.host:localhost}")
    private String host;

    @Value("${spring.elasticsearch.port:9200}")
    private int port;

    @Override
    @Bean
    @Lazy
    public RestHighLevelClient elasticsearchClient() {
        try {
            ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo(host + ":" + port)
                    .build();
            RestHighLevelClient client = RestClients.create(clientConfiguration).rest();
            // 测试连接
            client.ping(RequestOptions.DEFAULT);
            return client;
        } catch (Exception e) {
            System.err.println("Elasticsearch连接失败，请检查配置和服务状态: " + e.getMessage());
            throw new RuntimeException("Failed to connect to Elasticsearch", e);
        }
    }
}
