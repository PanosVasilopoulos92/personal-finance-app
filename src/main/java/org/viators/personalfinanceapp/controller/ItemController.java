package org.viators.personalfinanceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.viators.personalfinanceapp.config.openapi.OwnerProtectedReadResponses;
import org.viators.personalfinanceapp.config.openapi.OwnerProtectedWriteResponses;
import org.viators.personalfinanceapp.config.openapi.ValidatedCreateResponses;
import org.viators.personalfinanceapp.dto.item.request.CreateItemRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemPriceRequest;
import org.viators.personalfinanceapp.dto.item.request.UpdateItemRequest;
import org.viators.personalfinanceapp.dto.item.response.ItemDetailsResponse;
import org.viators.personalfinanceapp.dto.item.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.service.ItemService;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "CRUD operations for tracked items and their prices")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(
            summary = "List user's items",
            description = "Returns a paginated list of the authenticated user's active items, "
                    + "sorted by creation date (newest first) by default.")
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    public ResponseEntity<Page<ItemSummaryResponse>> getItems(@AuthenticationPrincipal(expression = "currentUser.uuid") String loggedInUserUuid,
                                                              @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ItemSummaryResponse> response = itemService.getItems(loggedInUserUuid, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @Operation(
            summary = "Get item with details",
            description = "Retrieves full details for a single item including categories, "
                    + "price observations, alerts, and comparisons.")
    @ApiResponse(responseCode = "200", description = "Item found",
            content = @Content(schema = @Schema(implementation = ItemDetailsResponse.class)))
    @OwnerProtectedReadResponses
    public ResponseEntity<ItemDetailsResponse> getItemWithDetails(@AuthenticationPrincipal(expression = "currentUser.uuid") String loggedInUserUuid,
                                                                  @Parameter(description = "Item UUID", example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
                                                                  @PathVariable String uuid) {
        return ResponseEntity.ok(itemService.getItem(uuid, loggedInUserUuid));
    }

    @PostMapping
    @Operation(
            summary = "Create a new item",
            description = "Creates a new item for the authenticated user along with an initial price observation.")
    @ApiResponse(responseCode = "201", description = "Item created successfully",
            content = @Content(schema = @Schema(implementation = ItemSummaryResponse.class)))
    @ValidatedCreateResponses
    public ResponseEntity<ItemSummaryResponse> create(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                      @RequestBody @Valid CreateItemRequest request) {
        ItemSummaryResponse response = itemService.create(userUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{uuid}/update")
    @Operation(
            summary = "Update an item",
            description = "Updates the details of an existing item owned by the authenticated user. "
                    + "Optionally assigns the item to a category.")
    @ApiResponse(responseCode = "200", description = "Item updated successfully",
            content = @Content(schema = @Schema(implementation = ItemSummaryResponse.class)))
    @OwnerProtectedWriteResponses
    public ResponseEntity<ItemSummaryResponse> update(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                      @Parameter(description = "Item UUID", example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
                                                      @PathVariable String uuid,
                                                      @RequestBody @Valid UpdateItemRequest request) {
        ItemSummaryResponse response = itemService.update(userUuid, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{uuid}/update-price")
    @Operation(
            summary = "Update an item's price",
            description = "Records a new price observation for an item, deactivating the previous active price.")
    @ApiResponse(responseCode = "200", description = "Price updated successfully",
            content = @Content(schema = @Schema(implementation = ItemSummaryResponse.class)))
    @OwnerProtectedWriteResponses
    public ResponseEntity<ItemSummaryResponse> updatePrice(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                                           @Parameter(description = "Item UUID", example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
                                                           @PathVariable("uuid") String itemUuid,
                                                           @RequestBody @Valid UpdateItemPriceRequest request) {
        ItemSummaryResponse response = itemService.updatePrice(userUuid, itemUuid, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{uuid}")
    @Operation(
            summary = "Deactivate an item",
            description = "Soft-deletes an item by setting its status to inactive. "
                    + "Only the item owner can perform this action.")
    @ApiResponse(responseCode = "204", description = "Item deactivated successfully")
    @OwnerProtectedReadResponses
    public ResponseEntity<Void> deactivateItem(@AuthenticationPrincipal(expression = "currentUser.uuid") String userUuid,
                                               @Parameter(description = "Item UUID", example = "c5d3e2f1-4a6b-7c8d-9e0f-1a2b3c4d5e6f")
                                               @PathVariable("uuid") String itemUuid) {
        itemService.deactivateItem(userUuid, itemUuid);
        return ResponseEntity.noContent().build();
    }
}
