package org.viators.personalfinanceapp.userpreferences;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.store.Store;
import org.viators.personalfinanceapp.store.StoreService;
import org.viators.personalfinanceapp.userpreferences.dto.request.UpdatePreferredStoresRequest;
import org.viators.personalfinanceapp.userpreferences.dto.request.UpdateUserPrefRequest;
import org.viators.personalfinanceapp.userpreferences.dto.response.UserPreferencesSummaryResponse;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;

    // Other Service dependencies
    private final StoreService storeService;

    public UserPreferencesSummaryResponse getPreferences(String uuid) {
        UserPreferences userPreferences = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        return UserPreferencesSummaryResponse.from(userPreferences);
    }

    @Transactional
    public UserPreferencesSummaryResponse updateUserPrefs(String uuid, UpdateUserPrefRequest request) {
        UserPreferences userPreferencesToUpdate = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        request.updateUserPrefs(userPreferencesToUpdate);
        return UserPreferencesSummaryResponse.from(userPreferencesToUpdate);
    }

    @Transactional
    public UserPreferencesSummaryResponse resetUserPrefsToDefault(String uuid) {
        UserPreferences userPreferencesToUpdate = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No such user in system."));

        UpdateUserPrefRequest.resetUserPrefs(userPreferencesToUpdate);
        return UserPreferencesSummaryResponse.from(userPreferencesToUpdate);
    }

    @Transactional
    public void updateUserPreferredStores(String userUuid, String uuid, UpdatePreferredStoresRequest request) {
        UserPreferences userPreferences = userPreferencesRepository.findByUser_Uuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this uuid"));

        Store storeToUpdate = storeService.getActiveStoreThatIsGlobalOrBelongsToUser(request.uuid(), userUuid);

        if (userPreferences.getPreferredStores().contains(storeToUpdate)) {
            userPreferences.getPreferredStores().remove(storeToUpdate);
        } else {
            userPreferences.getPreferredStores().add(storeToUpdate);
        }
    }

}
