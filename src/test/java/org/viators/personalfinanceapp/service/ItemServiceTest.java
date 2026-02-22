//package org.viators.personalfinanceapp.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.viators.personalfinanceapp.category.CategoryRepository;
//import org.viators.personalfinanceapp.item.dto.request.CreateItemRequest;
//import org.viators.personalfinanceapp.item.dto.response.ItemSummaryResponse;
//import org.viators.personalfinanceapp.priceobservation.dto.request.CreatePriceObservationRequest;
//import org.viators.personalfinanceapp.exceptions.ResourceNotFoundException;
//import org.viators.personalfinanceapp.item.Item;
//import org.viators.personalfinanceapp.item.ItemRepository;
//import org.viators.personalfinanceapp.item.ItemService;
//import org.viators.personalfinanceapp.priceobservation.PriceObservationRepository;
//import org.viators.personalfinanceapp.store.Store;
//import org.viators.personalfinanceapp.store.StoreRepository;
//import org.viators.personalfinanceapp.user.User;
//import org.viators.personalfinanceapp.common.enums.CurrencyEnum;
//import org.viators.personalfinanceapp.common.enums.ItemUnitEnum;
//import org.viators.personalfinanceapp.common.enums.StatusEnum;
//import org.viators.personalfinanceapp.common.enums.StoreTypeEnum;
//import org.viators.personalfinanceapp.common.enums.UserRolesEnum;
//import org.viators.personalfinanceapp.user.UserRepository;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("Item Service Test")
//public class ItemServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private ItemRepository itemRepository;
//    @Mock
//    private CategoryRepository categoryRepository;
//    @Mock
//    private StoreRepository storeRepository;
//    @Mock
//    private PriceObservationRepository priceObservationRepository;
//
//    @InjectMocks
//    private ItemService itemService;
//
//    private User testUser;
//    private Store testStore;
//    private Item testItem;
//    private CreateItemRequest createItemRequest;
//    private CreatePriceObservationRequest createPriceObservationRequest;
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.builder()
//                .id(1L)
//                .uuid("550e8400-e29b-41d4-a716-446655440000")
//                .username("johndoe")
//                .email("john@example.com")
//                .password("encrypted")
//                .firstName("John")
//                .lastName("Doe")
//                .userRole(UserRolesEnum.USER)
//                .status(StatusEnum.ACTIVE.getCode())
//                .build();
//
//        testStore = new Store();
//        testStore.setId(1L);
//        testStore.setUuid("660e8400-e29b-41d4-a716-446655440001");
//        testStore.setName("Sklavenitis");
//        testStore.setStoreType(StoreTypeEnum.SUPERMARKET);
//        testStore.setCity("Athens");
//        testStore.setCountry("Greece");
//        testStore.setStatus(StatusEnum.ACTIVE.getCode());
//
//        testItem = Item.builder()
//                .id(1L)
//                .uuid("770e8400-e29b-41d4-a716-446655440002")
//                .name("Olive Oil")
//                .description("Extra virgin olive oil 1L bottle")
//                .itemUnit(ItemUnitEnum.LITER)
//                .brand("Minerva")
//                .user(testUser)
//                .isFavorite(false)
//                .status(StatusEnum.ACTIVE.getCode())
//                .build();
//
//        createPriceObservationRequest = new CreatePriceObservationRequest(
//                new BigDecimal("3.9995342452352325"),
//                CurrencyEnum.EUR,
//                Instant.now(),
//                "Athens, Greece",
//                "Weekly offer price",
//                testItem.getUuid(),
//                testStore.getUuid()
//        );
//
//        createItemRequest = new CreateItemRequest(
//                "Olive Oil",
//                "Extra virgin olive oil 1L bottle",
//                ItemUnitEnum.LITER,
//                "Minerva",
//                createPriceObservationRequest
//        );
//    }
//
//    @Test
//    void createItem_ValidRequest_SuccessResponse() {
//        // Arrange
//        when(userRepository.findByUuidAndStatus(testUser.getUuid(), StatusEnum.ACTIVE.getCode()))
//                .thenReturn(Optional.of(testUser));
//        when(storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(createPriceObservationRequest.storeUuid(), StatusEnum.ACTIVE.getCode(), testUser.getUuid()))
//                .thenReturn(Optional.of(testStore));
//        // Mocks save operation
//        when(itemRepository.save(any(Item.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        ItemSummaryResponse response = itemService.create(testUser.getUuid(), createItemRequest);
//
//        // Assert
//        assertThat(response).isNotNull();
//        assertThat(response.name()).isEqualTo(createItemRequest.name());
//
//        // Verification with Argument Capture
//        ArgumentCaptor<Item> itemArgumentCaptor = ArgumentCaptor.forClass(Item.class);
//        verify(itemRepository).save(itemArgumentCaptor.capture());
//
//        System.out.println(itemArgumentCaptor.getValue());
//        assertThat(itemArgumentCaptor.getValue().getName()).isEqualTo("Olive Oil");
//
//    }
//
//    @Test
//    void createItem_InvalidRequest_ExceptionThrow() {
//        // Arrange
//        when(userRepository.findByUuidAndStatus(testUser.getUuid(), StatusEnum.ACTIVE.getCode()))
//                .thenReturn(Optional.of(testUser));
//        when(storeRepository.findByUuidAndStatusAndUserIsNullOrUser_Uuid(createPriceObservationRequest.storeUuid(), StatusEnum.ACTIVE.getCode(), testUser.getUuid()))
//                .thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> itemService.create(testUser.getUuid(), createItemRequest))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessage("Store could not be found");
//
//        verify(itemRepository, never()).save(any());
//
//    }
//
//}
