package com.example.solumonbackend.global.config;

import com.example.solumonbackend.global.elasticsearch.PostSearchRepository;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackageClasses = PostSearchRepository.class)
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

  @Value("${spring.elasticsearch.username}")
  private String hostname;

  @Value("${spring.elasticsearch.port}")
  private String port;

  @Bean
  public ElasticsearchRestTemplate elasticsearchRestTemplate() {
    return new ElasticsearchRestTemplate(elasticsearchClient());
  }

  @Bean
  @Override
  public RestHighLevelClient elasticsearchClient() {
    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(hostname + ":" + port)
        .build();

    return RestClients.create(clientConfiguration).rest();
  }
}
