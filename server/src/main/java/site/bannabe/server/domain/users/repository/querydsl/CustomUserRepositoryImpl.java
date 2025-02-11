package site.bannabe.server.domain.users.repository.querydsl;

import static site.bannabe.server.domain.users.entity.QUsers.users;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Repository
@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Users findByEmail(String email) {
    Users user = jpaQueryFactory.selectFrom(users)
                                .where(users.email.eq(email))
                                .fetchFirst();

    return Optional.ofNullable(user).orElseThrow(() -> new BannabeServiceException(ErrorCode.USER_NOT_FOUND));
  }

  @Override
  public Users findByToken(String token) {
    Users user = jpaQueryFactory.selectFrom(users)
                                .where(users.token.eq(token))
                                .fetchFirst();

    return Optional.ofNullable(user).orElseThrow(() -> new BannabeServiceException(ErrorCode.USER_NOT_FOUND));
  }

}