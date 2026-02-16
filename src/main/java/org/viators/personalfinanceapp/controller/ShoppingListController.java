package org.viators.personalfinanceapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.viators.personalfinanceapp.dto.shoppinglist.request.CreateShoppingListRequest;
import org.viators.personalfinanceapp.dto.shoppinglist.request.UpdateShoppingListRequest;
import org.viators.personalfinanceapp.dto.shoppinglist.response.ShoppingListDetailsResponse;
import org.viators.personalfinanceapp.dto.shoppinglist.response.ShoppingListSummaryResponse;
import org.viators.personalfinanceapp.service.ShoppingListService;

import java.net.URI;

@RequestMapping("/api/v1/shopping-lists")
@RequiredArgsConstructor
@Slf4j
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping("/{uuid}")
    public ResponseEntity<ShoppingListDetailsResponse> getShoppingList(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                                       @PathVariable String uuid) {
        ShoppingListDetailsResponse response = shoppingListService.getShoppingList(userUuid, uuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShoppingListSummaryResponse>> getAllActiveShoppingListsForUser(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                                                              @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
                                                                                              Pageable pageable) {
        Page<ShoppingListSummaryResponse> response = shoppingListService.getAllActiveShoppingListsForUser(userUuid, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ShoppingListSummaryResponse> create(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                              @Valid @RequestBody CreateShoppingListRequest request) {
        ShoppingListSummaryResponse response = shoppingListService.create(userUuid, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(response.uuid())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ShoppingListSummaryResponse> update(@PathVariable String uuid,
                                                              @Valid @RequestBody UpdateShoppingListRequest request) {
        ShoppingListSummaryResponse response = shoppingListService.update(uuid, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deactivateList(@PathVariable String uuid) {
        shoppingListService.deactivateShoppingList(uuid);
        return ResponseEntity.noContent().build();
    }

}
