package com.board.repository;

import com.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JPA - 정적 쿼리 (CRUD)
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
