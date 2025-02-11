package site.bannabe.server.domain.users.repository.querydsl;

import site.bannabe.server.domain.users.entity.Users;

public interface CustomUserRepository {

  Users findByEmail(String email);

  Users findByToken(String token);

}