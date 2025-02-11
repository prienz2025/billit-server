package site.bannabe.server.domain.users.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import site.bannabe.server.config.AbstractTestContainers;
import site.bannabe.server.config.CustomDataJpaTest;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@CustomDataJpaTest
class UserRepositoryTest extends AbstractTestContainers {

  private final String email = "test@test.com";

  @Autowired
  private UserRepository userRepository;

  @PersistenceContext
  private EntityManager em;

  @Test
  @DisplayName("이메일 기반 회원 조회")
  void findByEmail() {
    //given
    Users user = Users.builder().email(email).build();
    em.persist(user);
    em.flush();
    em.clear();

    //when
    Users result = userRepository.findByEmail(email);

    //then
    assertThat(result).isNotNull()
                      .extracting("email")
                      .isEqualTo(email);
  }

  @Test
  @DisplayName("회원정보 미 존재시 예외 발생")
  void notFoundUser() {
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> userRepository.findByEmail(email))
        .withMessage(ErrorCode.USER_NOT_FOUND.getMessage());
  }


}