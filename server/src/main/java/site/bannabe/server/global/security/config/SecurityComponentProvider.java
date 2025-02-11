package site.bannabe.server.global.security.config;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import site.bannabe.server.global.jwt.JwtService;
import site.bannabe.server.global.security.filter.ExceptionHandleFilter;
import site.bannabe.server.global.security.filter.JSONUsernamePasswordAuthenticationFilter;
import site.bannabe.server.global.security.filter.JwtAuthenticationFilter;
import site.bannabe.server.global.security.handler.CustomAuthenticationEntryPoint;
import site.bannabe.server.global.security.handler.CustomLogoutHandler;
import site.bannabe.server.global.security.handler.CustomLogoutSuccessHandler;
import site.bannabe.server.global.utils.ErrorResponseWriter;

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