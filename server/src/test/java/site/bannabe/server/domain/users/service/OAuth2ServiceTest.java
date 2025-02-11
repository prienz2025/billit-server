package site.bannabe.server.domain.users.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.bannabe.server.domain.users.repository.UserRepository;
import site.bannabe.server.global.api.OAuth2ApiClient;
import site.bannabe.server.global.jwt.JwtService;

@ExtendWith(MockitoExtension.class)
class OAuth2ServiceTest {


  @InjectMocks
  private OAuth2Service oAuth2Service;
  @Mock
  private UserRepository userRepository;
  @Mock
  private OAuth2ApiClient oAuth2ApiClient;
  @Mock
  private JwtService jwtService;
  @Mock
  private PasswordService passwordService;

  // 소셜 로그인 정상 동작 테스트 (응답값 잘 나오는지, User Entity 잘 만들어 지는지)

  // findByEmail 예외 발생 시 새로운 사용자 등록 하는지

}