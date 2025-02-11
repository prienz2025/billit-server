package site.bannabe.server.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing
public class TestDBConfig {

  @PersistenceContext
  EntityManager entityManager;

  @Bean
  JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }

}