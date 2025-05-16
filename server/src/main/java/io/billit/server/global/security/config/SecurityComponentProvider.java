package io.billit.server.global.security.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import io.billit.server.global.jwt.JwtService;
import io.billit.server.global.security.filter.ExceptionHandleFilter;
import io.billit.server.global.security.filter.JSONUsernamePasswordAuthenticationFilter;
import io.billit.server.global.security.filter.JwtAuthenticationFilter;
import io.billit.server.global.security.handler.CustomAuthenticationEntryPoint;
import io.billit.server.global.security.handler.CustomLogoutHandler;
import io.billit.server.global.security.handler.CustomLogoutSuccessHandler;
import io.billit.server.global.utils.ErrorResponseWriter;

@Getter
@Configuration
public class SecurityComponentProvider {

  private final ExceptionHandleFilter exceptionHandleFilter;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  private final JSONUsernamePasswordAuthenticationFilter jsonLoginFilter;

  private final CustomAuthenticationEntryPoint authenticationEntryPoint;

  private final CustomLogoutHandler logoutHandler;

  private final CustomLogoutSuccessHandler logoutSuccessHandler;

  public SecurityComponentProvider(JSONUsernamePasswordAuthenticationFilter jsonLoginFilter, JwtService jwtService,
      CustomAuthenticationEntryPoint authenticationEntryPoint, CustomLogoutHandler logoutHandler,
      CustomLogoutSuccessHandler logoutSuccessHandler, ErrorResponseWriter errorResponseWriter) {
    this.exceptionHandleFilter = new ExceptionHandleFilter(errorResponseWriter);
    this.jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
    this.jsonLoginFilter = jsonLoginFilter;
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.logoutHandler = logoutHandler;
    this.logoutSuccessHandler = logoutSuccessHandler;
  }

}