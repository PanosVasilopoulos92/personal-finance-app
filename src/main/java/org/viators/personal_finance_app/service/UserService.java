package org.viators.personal_finance_app.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personal_finance_app.dtos.UserDTOs;
import org.viators.personal_finance_app.model.User;
import org.viators.personal_finance_app.model.UserPreferences;
import org.viators.personal_finance_app.repository.UserRepository;
import org.viators.personal_finance_app.model.enums.StatusEnum;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserDTOs.UserSummary registerUser(UserDTOs.CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EntityExistsException("Email is already in use");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new EntityExistsException("Username is already in use");
        }

        if (request.age() < 13) {
            throw new IllegalArgumentException("User must be above 13 years old in order to register.");
        }

        User userToRegister = request.toEntity();
        userToRegister.setPassword(encryptPassword(request.password()));

        //Create default Preferences
        UserPreferences userPreferences = UserPreferences.createDefaultPreferences();
        userToRegister.addUserPreferences(userPreferences);

        return UserDTOs.UserSummary.from(userRepository.save(userToRegister));
    }

    private String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean updateUserPassword(String uuid, UserDTOs.UpdateUserPasswordRequest request) {
        User userToUpdate = userRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode()).orElse(null);

        if (userToUpdate == null) {
            throw new EntityNotFoundException(String.format("User with uuid: %s does not exist", uuid));
        }

        userToUpdate.setPassword(passwordEncoder.encode(request.newPassword()));
        return true;
    }

    @Transactional(readOnly = true)
    public User findUserByUuidAndStatus(String uuid, StatusEnum status) {
        return userRepository.findByUuidAndStatus(uuid, status.getCode()).orElse(null);
    }

    public UserDTOs.UserSummary updateUser(String uuid, UserDTOs.UpdateUserRequest updateUserRequest) {
        User userToUpdate = userRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode()).orElse(null);

        if (userToUpdate == null) {
            throw new EntityNotFoundException(String.format("User with uuid: %s does not exist", uuid));
        }

        updateUserRequest.updateUser(userToUpdate);
        return UserDTOs.UserSummary.from(userRepository.save(userToUpdate));
    }

}
