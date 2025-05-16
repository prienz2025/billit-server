package io.billit.server.domain.users.repository.querydsl;

import io.billit.server.domain.users.entity.Users;

public interface CustomUserRepository {

  Users findByEmail(String email);

  Users findByToken(String token);

}