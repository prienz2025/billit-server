package site.bannabe.server.global.security.auth;

import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import site.bannabe.server.domain.users.entity.Users;

public class PrincipalDetails implements UserDetails {

  private final String email;

  private final String password;
  @Getter
  private final String entityToken;

  private final Collection<? extends GrantedAuthority> authorities;

  private PrincipalDetails(String email, String password, String entityToken,
      Collection<? extends GrantedAuthority> authorities) {
    this.email = email;
    this.password = password;
    this.entityToken = entityToken;
    this.authorities = authorities;
  }

  public static PrincipalDetails create(Users user) {
    String roleKey = user.getRole().getRoleKey();
    Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(roleKey));
    return new PrincipalDetails(user.getEmail(), user.getPassword(), user.getToken(), authorities);
  }

  public static PrincipalDetails create(String entityToken, Collection<? extends GrantedAuthority> authorities) {
    return new PrincipalDetails(entityToken, null, entityToken, authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }
}