package org.viators.personalfinanceapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.viators.personalfinanceapp.dto.item.request.CreateItemRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemPriceRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemRequest;
import org.viators.personalfinanceapp.dto.item.response.ItemDetailsResponse;
import org.viators.personalfinanceapp.dto.item.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.service.ItemService;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{uuid}")
    public ResponseEntity<ItemDetailsResponse> getItemWithDetails(@PathVariable String uuid) {
        return ResponseEntity.ok(itemService.getItem(uuid));
    }

    @PostMapping
    public ResponseEntity<ItemSummaryResponse> create(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                      @RequestBody @Valid CreateItemRequest request) {
        ItemSummaryResponse response = itemService.create(userUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{uuid}/update")
    public ResponseEntity<ItemSummaryResponse> update(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                      @RequestBody @Valid UpdateItemRequest request) {
        ItemSummaryResponse response = itemService.update(userUuid, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uuid}/update-price")
    public ResponseEntity<ItemSummaryResponse> updatePrice(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                           @RequestBody @Valid UpdateItemPriceRequest request) {
        ItemSummaryResponse response = itemService.updatePrice(userUuid, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deactivateItem(@PathVariable String uuid) {
        itemService.deactivateItem(uuid);
        return ResponseEntity.noContent().build();
    }
}
