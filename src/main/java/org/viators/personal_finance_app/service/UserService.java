package org.viators.personal_finance_app.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personal_finance_app.dtos.UserDTOs;
import org.viators.personal_finance_app.model.User;
import org.viators.personal_finance_app.model.enums.StatusEnum;
import org.viators.personal_finance_app.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDTOs.UserSummary registerUser(UserDTOs.CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new EntityExistsException("Entity already exists.");
        }

        User userToRegister = createUserRequest.toEntity();
        userToRegister.setPassword(passwordEncoder.encode(userToRegister.getPassword()));

        return UserDTOs.UserSummary.from(userRepository.save(userToRegister));
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

    public boolean updateUserPassword(String uuid, UserDTOs.UpdateUserPasswordRequest request) {
        User userToUpdate = userRepository.findByUuidAndStatus(uuid, StatusEnum.ACTIVE.getCode()).orElse(null);

        if (userToUpdate == null) {
            throw new EntityNotFoundException(String.format("User with uuid: %s does not exist", uuid));
        }

        userToUpdate.setPassword(passwordEncoder.encode(request.newPassword()));
        return true;
    }
}
