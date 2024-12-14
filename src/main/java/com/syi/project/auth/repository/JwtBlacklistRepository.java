package com.syi.project.auth.repository;

import com.syi.project.auth.entity.JwtBlacklist;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

  Optional<JwtBlacklist> findByTokenIdAndTokenType(String tokenId, String tokenType);

  int deleteAllByExpirationBefore(LocalDateTime dateTime);

}
