package com.app.repository;

import com.app.models.User;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findById(Long id);

    User findByPhoneNumber(@NonNull String phoneNumber);

    List<User> findByPhoneNumberIn(@NonNull List<String> phoneNumbers);

    boolean deleteById(Long id);
}
