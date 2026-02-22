package org.viators.personalfinanceapp.userpreferences;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    // The underscore (`_`), "traversal delimiter", explicitly tells Spring Data JPA to traverse into a nested entity.
    // It's resolving this path: UserPreferences.user.uuid
    Optional<UserPreferences> findByUser_Uuid(String uuid);

}
