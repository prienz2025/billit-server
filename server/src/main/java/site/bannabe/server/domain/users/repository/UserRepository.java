package site.bannabe.server.domain.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.bannabe.server.domain.users.entity.Users;
import site.bannabe.server.domain.users.repository.querydsl.CustomUserRepository;

public interface UserRepository extends JpaRepository<Users, Long>, CustomUserRepository {

  Boolean existsByEmail(String email);

  Boolean existsByNickname(String nickname);

}