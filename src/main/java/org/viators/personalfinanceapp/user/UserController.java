package org.viators.personalfinanceapp.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.viators.personalfinanceapp.item.ItemService;
import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.user.dto.request.CreateUserRequest;
import org.viators.personalfinanceapp.user.dto.request.UpdateUserRequest;
import org.viators.personalfinanceapp.user.dto.response.UserDetailsResponse;
import org.viators.personalfinanceapp.user.dto.response.UserSummaryResponse;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryResponse>> getUsers(@PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserSummaryResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}/details")
    public ResponseEntity<UserDetailsResponse> getUserWithDetails(@PathVariable String uuid) {
        UserDetailsResponse response = userService.findUserByUuidWithAllRelationships(uuid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserSummaryResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserSummaryResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{uuid}")
    public ResponseEntity<UserSummaryResponse> getUser(@PathVariable String uuid) {
        UserSummaryResponse response = userService.findUserByUuid(uuid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserSummaryResponse> updateUser(@PathVariable String uuid, @RequestBody UpdateUserRequest request) {
        UserSummaryResponse response = userService.updateUserInfo(uuid, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}/deactivate")
    @PreAuthorize("@userSecurity.isSelf(#uuid)")
    public ResponseEntity<Void> deactivateUser(@PathVariable String uuid) {
        userService.deactivateUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{uuid}/items")
    public ResponseEntity<Page<ItemSummaryResponse>> getAllItems(@PathVariable String uuid,
                                                                 @PageableDefault()Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItemsForUser(uuid, pageable));
    }
}
