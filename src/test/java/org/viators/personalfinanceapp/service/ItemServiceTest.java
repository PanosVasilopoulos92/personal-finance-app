package org.viators.personalfinanceapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.viators.personalfinanceapp.dto.item.request.CreateItemRequest;
import org.viators.personalfinanceapp.dto.item.response.ItemSummaryResponse;
import org.viators.personalfinanceapp.dto.priceobservation.request.CreatePriceObservationRequest;
import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
import org.viators.personalfinanceapp.model.Item;
import org.viators.personalfinanceapp.model.Store;
import org.viators.personalfinanceapp.model.User;
import org.viators.personalfinanceapp.model.enums.CurrencyEnum;
import org.viators.personalfinanceapp.model.enums.ItemUnitEnum;
import org.viators.personalfinanceapp.model.enums.StatusEnum;
import org.viators.personalfinanceapp.model.enums.StoreTypeEnum;
import org.viators.personalfinanceapp.model.enums.UserRolesEnum;
import org.viators.personalfinanceapp.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Item Service Test")
public class ItemServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private PriceObservationRepository priceObservationRepository;

    @InjectMocks
    private ItemService itemService;

    private User testUser;
    private Store testStore;
    private Item testItem;
    private CreateItemRequest createItemRequest;
    private CreatePriceObservationRequest createPriceObservationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .uuid("550e8400-e29b-41d4-a716-446655440000")
                .username("johndoe")
                .email("john@example.com")
                .password("encrypted")
                .firstName("John")
                .lastName("Doe")
                .userRole(UserRolesEnum.USER)
                .status(StatusEnum.ACTIVE.getCode())
                .build();

        testStore = new Store();
        testStore.setId(1L);
        testStore.setUuid("660e8400-e29b-41d4-a716-446655440001");
        testStore.setName("Sklavenitis");
        testStore.setStoreType(StoreTypeEnum.SUPERMARKET);
        testStore.setCity("Athens");
        testStore.setCountry("Greece");
        testStore.setStatus(StatusEnum.ACTIVE.getCode());

        testItem = Item.builder()
                .id(1L)
                .uuid("770e8400-e29b-41d4-a716-446655440002")
                .name("Olive Oil")
                .description("Extra virgin olive oil 1L bottle")
                .itemUnit(ItemUnitEnum.LITTER)
                .brand("Minerva")
                .user(testUser)
                .isFavorite(false)
                .status(StatusEnum.ACTIVE.getCode())
                .build();

        createPriceObservationRequest = new CreatePriceObservationRequest(
                new BigDecimal("3.9995342452352325"),
                CurrencyEnum.EUR,
                LocalDate.now(),
                "Athens, Greece",
                "Weekly offer price",
                1L,
                1L
        );

        createItemRequest = new CreateItemRequest(
                "Olive Oil",
                "Extra virgin olive oil 1L bottle",
                ItemUnitEnum.LITTER,
                "Minerva",
                "Sklavenitis",
                createPriceObservationRequest
        );
    }

    @Test
    void createItem_ValidRequest_SuccessResponse() {
        // Arrange
        when(userRepository.findByUuidAndStatus(testUser.getUuid(), StatusEnum.ACTIVE.getCode()))
                .thenReturn(Optional.of(testUser));
        when(storeRepository.findByNameAndStatusAndUserIsNullOrUser_Uuid(createItemRequest.storeName(), StatusEnum.ACTIVE.getCode(), testUser.getUuid()))
                .thenReturn(Optional.of(testStore));
        // Mocks save operation
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ItemSummaryResponse response = itemService.create(testUser.getUuid(), createItemRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(createItemRequest.name());

        // Verification with Argument Capture
        ArgumentCaptor<Item> itemArgumentCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemArgumentCaptor.capture());

        System.out.println(itemArgumentCaptor.getValue());
        assertThat(itemArgumentCaptor.getValue().getName()).isEqualTo("Olive Oil");

    }

    @Test
    void createItem_InvalidRequest_ExceptionThrow() {
        // Arrange
        when(userRepository.findByUuidAndStatus(testUser.getUuid(), StatusEnum.ACTIVE.getCode()))
                .thenReturn(Optional.of(testUser));
        when(storeRepository.findByNameAndStatusAndUserIsNullOrUser_Uuid(createItemRequest.storeName(), StatusEnum.ACTIVE.getCode(), testUser.getUuid()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(testUser.getUuid(), createItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Store could not be found");

        verify(itemRepository, never()).save(any());

    }

}
