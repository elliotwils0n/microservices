package io.github.elliotwils0n.microservices.verification.repository;

import io.github.elliotwils0n.microservices.verification.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {

    Optional<Verification> findByUserIdAndHashAndUsed(UUID userId, String hash, Boolean used);

}
