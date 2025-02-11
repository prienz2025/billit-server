package site.bannabe.server.config;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;

@IntegrationTest
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractIntegrationTest extends AbstractTestContainers {

  protected static final String DEFAULT_RESTDOC_PATH = "{class_name}/{method_name}";
  protected RequestSpecification spec;

  @LocalServerPort
  private int port;

  @BeforeEach
  void setup() {
    RestAssured.port = port;
  }

  @BeforeEach
  void setupRestDocs(RestDocumentationContextProvider provider) {
    this.spec = new RequestSpecBuilder()
        .setPort(port)
        .addFilter(documentationConfiguration(provider)
            .operationPreprocessors()
            .withRequestDefaults(prettyPrint())
            .withResponseDefaults(prettyPrint()))
        .build();
  }

}