package io.billit.server.global.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import io.billit.server.domain.users.entity.Users;
import io.billit.server.domain.users.repository.UserRepository;
import io.billit.server.global.exceptions.BannabeServiceException;
import io.billit.server.global.security.auth.PrincipalDetails;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
      Users user = userRepository.findByEmail(username);
      return PrincipalDetails.create(user);
    } catch (BannabeServiceException e) {
      throw new UsernameNotFoundException(e.getErrorCode().getMessage());
    }
  }

}