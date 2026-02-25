package org.viators.personalfinanceapp.store;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.viators.personalfinanceapp.store.dto.request.CreateStoreRequest;
import org.viators.personalfinanceapp.store.dto.request.StoreFilterRequest;
import org.viators.personalfinanceapp.store.dto.request.UpdateStoreRequest;
import org.viators.personalfinanceapp.store.dto.response.StoreDetailsResponse;
import org.viators.personalfinanceapp.store.dto.response.StoreSummaryResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<Page<StoreSummaryResponse>> getStores(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                                @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StoreSummaryResponse> response = storeService.getStores(userUuid, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storeUuid}")
    public ResponseEntity<StoreDetailsResponse> getStore(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                         @PathVariable String storeUuid) {
        StoreDetailsResponse response = storeService.getStore(userUuid, storeUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StoreSummaryResponse>> getStoresBasedOnFilters(@AuthenticationPrincipal(expression = "currentUser.uuid") String loggedInUserUuid,
                                                                        @Valid @ModelAttribute StoreFilterRequest request,
                                                                        @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                        Pageable pageable) {

        Page<StoreSummaryResponse> response = storeService.getStoresBasedOnFilters(loggedInUserUuid, request, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<StoreSummaryResponse> create(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                       @Valid @RequestBody CreateStoreRequest request) {
        StoreSummaryResponse response = storeService.create(userUuid, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(response.uuid())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{storeUuid}")
    public ResponseEntity<StoreSummaryResponse> update(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                       @PathVariable String storeUuid,
                                                       @Valid @RequestBody UpdateStoreRequest request) {
        StoreSummaryResponse response = storeService.update(userUuid, storeUuid, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{storeUuid}/re-activate")
    public ResponseEntity<Void> reActivateUserStore(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                    @PathVariable String storeUuid) {
        storeService.reActivateStore(userUuid, storeUuid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{storeUuid}/deactivate")
    public ResponseEntity<Void> deactivateUserStore(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                    @PathVariable String storeUuid) {
        storeService.deActivateStore(userUuid, storeUuid);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{storeUuid}")
    public ResponseEntity<Void> deleteStore(@PathVariable String storeUuid) {
        storeService.deleteStore(storeUuid);
        return ResponseEntity.noContent().build();
    }

}
