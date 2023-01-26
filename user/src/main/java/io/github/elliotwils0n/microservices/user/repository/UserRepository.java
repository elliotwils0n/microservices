package io.github.elliotwils0n.microservices.user.repository;

import io.github.elliotwils0n.microservices.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    long countByUsername(String username);

    long countByEmail(String email);

}
