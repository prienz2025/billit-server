package site.bannabe.server.global.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import site.bannabe.server.domain.users.entity.Role;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;
import site.bannabe.server.global.security.auth.PrincipalDetails;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  @Mock
  private UserRepository userRepository;

  @Test
  @DisplayName("회원 조회 성공 시 PrincipalDetails 응답")
  void loadUserByUsername() {
    //given
    String email = "test@test.com";
    String password = "1234";
    Users user = mock(Users.class);
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(Role.USER.getRoleKey()));

    given(user.getEmail()).willReturn(email);
    given(user.getRole()).willReturn(Role.USER);
    given(user.getPassword()).willReturn(password);
    given(userRepository.findByEmail(email)).willReturn(user);

    //when
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

    //then
    assertThat(userDetails).isInstanceOf(PrincipalDetails.class)
                           .extracting("username", "password", "authorities")
                           .containsExactly(email, password, authorities);
  }

  @Test
  @DisplayName("회원 정보 미 존재시 UserNotFoundException 발생")
  void notFoundUser() {
    //given
    String email = "test@test.com";
    given(userRepository.findByEmail(email)).willThrow(new BannabeServiceException(ErrorCode.USER_NOT_FOUND));

    //when then
    assertThatExceptionOfType(UsernameNotFoundException.class)
        .isThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
        .withMessage(ErrorCode.USER_NOT_FOUND.getMessage());
  }

}