package org.viators.personalfinanceapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.dto.userpreferences.request.UpdatePreferredStores;
import org.viators.personalfinanceapp.dto.userpreferences.request.UpdateUserPrefRequest;
import org.viators.personalfinanceapp.dto.userpreferences.response.UserPreferencesSummary;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.UserPreferences;
import org.viators.personalfinanceapp.repository.StoreRepository;
import org.viators.personalfinanceapp.repository.UserPreferencesRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final StoreRepository storeRepository;

    public UserPreferencesSummary getPreferences(String uuid) {
        UserPreferences userPreferences = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        return UserPreferencesSummary.from(userPreferences);
    }

    @Transactional
    public UserPreferencesSummary updateUserPrefs(String uuid, UpdateUserPrefRequest request) {
        UserPreferences userPreferencesToUpdate = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        request.updateUserPrefs(userPreferencesToUpdate);
        return UserPreferencesSummary.from(userPreferencesToUpdate);
    }

    @Transactional
    public UserPreferencesSummary resetUserPrefsToDefault(String uuid) {
        UserPreferences userPreferencesToUpdate = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        UpdateUserPrefRequest.resetUserPrefs(userPreferencesToUpdate);
        return UserPreferencesSummary.from(userPreferencesToUpdate);
    }

    @Transactional
    public void updateUserPreferredStores(String uuid, UpdatePreferredStores request) {
        UserPreferences userPreferences = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this uuid"));

        Store storeToUpdate = storeRepository.findByUuid(request.uuid())
                .orElseThrow(() -> new ResourceNotFoundException("No store found with this uuid"));

        if (userPreferences.getPreferredStores().contains(storeToUpdate)) {
            userPreferences.getPreferredStores().remove(storeToUpdate);
        } else {
            userPreferences.getPreferredStores().add(storeToUpdate);
        }
    }

}
