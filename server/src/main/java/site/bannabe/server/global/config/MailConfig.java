package site.bannabe.server.global.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
  private static final String MAIL_DEBUG = "mail.smtp.debug";
  private static final String MAIL_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
  private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Value("${spring.mail.properties.mail.smtp.auth}")
  private boolean auth;

  @Value("${spring.mail.properties.mail.smtp.debug}")
  private boolean debug;

  @Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
  private int connectionTimeout;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private boolean startTlsEnable;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost(host);
    javaMailSender.setPort(port);
    javaMailSender.setUsername(username);
    javaMailSender.setPassword(password);

    Properties properties = javaMailSender.getJavaMailProperties();
    properties.put(MAIL_SMTP_AUTH, auth);
    properties.put(MAIL_DEBUG, debug);
    properties.put(MAIL_CONNECTION_TIMEOUT, connectionTimeout);
    properties.put(MAIL_SMTP_STARTTLS_ENABLE, startTlsEnable);

    javaMailSender.setJavaMailProperties(properties);
    javaMailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());

    return javaMailSender;
  }
}