package com.example.reelsplace.domain.repository;

import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByProviderAndProviderUserId(Provider provider, String providerUserId);
    
    boolean existsByProviderAndProviderUserId(Provider provider, String providerUserId);
}
