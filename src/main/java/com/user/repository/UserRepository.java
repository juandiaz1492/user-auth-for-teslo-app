package com.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.entities.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByMail(String mail);

    Optional<User> findByActivationToken(String activationToken);


}
