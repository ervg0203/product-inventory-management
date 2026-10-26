package com.example.inventory.service;

import com.example.inventory.dto.request.*;
import com.example.inventory.dto.response.*;
import com.example.inventory.entity.*;
import com.example.inventory.exception.*;
import com.example.inventory.mapper.InventoryMapper;
import com.example.inventory.repository.*;
import com.example.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.result.UpdateResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    @Mock InventoryRepository inventoryRepository;
    @Mock ProductRepository productRepository;
    @Mock WarehouseRepository warehouseRepository;
    @Mock InventoryMapper mapper;
    @Mock MongoTemplate mongoTemplate;
    @InjectMocks InventoryServiceImpl service;

    private WarehouseEntity active() { return WarehouseEntity.builder().id("w").active(true).build(); }
    private InventoryEntity inventory() { return InventoryEntity.builder().id("i").productId("p").warehouseId("w").quantity(10).build(); }
    private InventoryRequestDTO request() { return InventoryRequestDTO.builder().productId("p").warehouseId("w").quantity(5).build(); }

    @Test
    void createsInventoryAndRejectsDuplicate() {
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(inventoryRepository.findByProductIdAndWarehouseId("p", "w")).thenReturn(Optional.empty());
        InventoryEntity entity = inventory(); when(mapper.toEntity(any())).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenReturn(entity);
        InventoryResponseDTO response = InventoryResponseDTO.builder().id("i").quantity(5).build();
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        assertSame(response, service.createInventory(request()));
        when(inventoryRepository.findByProductIdAndWarehouseId("p", "w")).thenReturn(Optional.of(entity));
        assertThrows(InventoryConflictException.class, () -> service.createInventory(request()));
    }

    @Test
    void translatesDuplicateKeyAndRejectsMissingOrInactiveReferences() {
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(inventoryRepository.findByProductIdAndWarehouseId("p", "w")).thenReturn(Optional.empty());
        InventoryEntity entity = inventory();
        when(mapper.toEntity(any())).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenThrow(new DuplicateKeyException("duplicate"));
        assertThrows(InventoryConflictException.class, () -> service.createInventory(request()));

        when(productRepository.existsById("p")).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> service.createInventory(request()));
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(WarehouseEntity.builder().id("w").active(false).build()));
        assertThrows(InventoryConflictException.class, () -> service.createInventory(request()));
        when(warehouseRepository.findById("w")).thenReturn(Optional.empty());
        assertThrows(WarehouseNotFoundException.class, () -> service.createInventory(request()));
    }

    @Test
    void bulkCreateValidatesAndSavesRecords() {
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(inventoryRepository.findByProductIdAndWarehouseId("p", "w")).thenReturn(Optional.empty());
        InventoryEntity entity = inventory();
        when(mapper.toEntity(any())).thenReturn(entity);
        when(inventoryRepository.saveAll(any())).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().id("i").quantity(5).build());
        assertEquals(1, service.createInventories(List.of(request())).size());
        verify(inventoryRepository).saveAll(any());
    }

    @Test
    void bulkCreateRejectsInvalidInputAndDuplicatePairs() {
        assertThrows(IllegalArgumentException.class, () -> service.createInventories(null));
        assertThrows(IllegalArgumentException.class, () -> service.createInventories(List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.createInventories(Arrays.asList(request(), null)));
        when(productRepository.existsById("p")).thenReturn(false);
        when(mapper.toResponseDTO(any(InventoryRequestDTO.class))).thenReturn(InventoryResponseDTO.builder().build());
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(request())));
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(inventoryRepository.findByProductIdAndWarehouseId("p", "w")).thenReturn(Optional.of(inventory()));
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(request())));
    }

    @Test
    void bulkValidationCoversMissingFieldsInactiveWarehousesAndQuantityRules() {
        when(mapper.toResponseDTO(any(InventoryRequestDTO.class)))
                .thenReturn(InventoryResponseDTO.builder().build());
        InventoryRequestDTO missingProduct = request(); missingProduct.setProductId("");
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(missingProduct)));
        InventoryRequestDTO missingWarehouse = request(); missingWarehouse.setWarehouseId("");
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(missingWarehouse)));
        InventoryRequestDTO missingQuantity = request(); missingQuantity.setQuantity(null);
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(missingQuantity)));
        InventoryRequestDTO negative = request(); negative.setQuantity(-1);
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(negative)));

        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(
                WarehouseEntity.builder().id("w").active(false).build()));
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(request())));
        when(warehouseRepository.findById("w")).thenReturn(Optional.empty());
        assertThrows(InventoryValidationException.class, () -> service.createInventories(List.of(request())));
    }

    @Test
    void bulkUpdateValidatesUpdatesAndMissingIds() {
        InventoryEntity entity = inventory();
        when(inventoryRepository.findAllById(Set.of("i"))).thenReturn(List.of(entity));
        when(productRepository.existsById("p")).thenReturn(true);
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(inventoryRepository.saveAll(any())).thenReturn(List.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().id("i").quantity(5).build());
        InventoryRequestDTO update = request(); update.setId("i");
        assertEquals(1, service.updateInventories(List.of(update)).size());
        assertEquals(5, entity.getQuantity());
        when(mapper.toResponseDTO(any(InventoryRequestDTO.class))).thenReturn(InventoryResponseDTO.builder().build());
        InventoryRequestDTO missing = request(); missing.setId("missing");
        when(inventoryRepository.findAllById(Set.of("missing"))).thenReturn(List.of());
        assertThrows(InventoryValidationException.class, () -> service.updateInventories(List.of(missing)));
        InventoryRequestDTO blank = request();
        assertThrows(InventoryValidationException.class, () -> service.updateInventories(List.of(blank)));
    }

    @Test
    void stockReadsSumAndLowStockSelectsCorrectRepository() {
        when(productRepository.existsById("p")).thenReturn(true);
        when(inventoryRepository.findByProductId("p")).thenReturn(List.of(inventory()));
        when(mapper.toResponseDTO(any(InventoryEntity.class))).thenReturn(InventoryResponseDTO.builder().quantity(10).build());
        assertEquals(10, service.getProductStock("p").getTotalStock());
        when(warehouseRepository.existsById("w")).thenReturn(true);
        when(inventoryRepository.findByWarehouseIdAndQuantityLessThanEqual("w", 3)).thenReturn(List.of());
        assertTrue(service.getLowStock(3, "w").isEmpty());
        verify(inventoryRepository).findByWarehouseIdAndQuantityLessThanEqual("w", 3);
        assertThrows(IllegalArgumentException.class, () -> service.getLowStock(-1, null));
        when(inventoryRepository.findByQuantityLessThanEqual(3)).thenReturn(List.of(inventory()));
        assertEquals(1, service.getLowStock(3, null).size());
        when(warehouseRepository.existsById("missing")).thenReturn(false);
        assertThrows(WarehouseNotFoundException.class, () -> service.getLowStock(3, "missing"));
    }

    @Test
    void readsInventoryAndWarehouseInventory() {
        InventoryEntity entity = inventory();
        when(inventoryRepository.findById("i")).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().id("i").build());
        assertEquals("i", service.getInventoryById("i").getId());
        when(inventoryRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(InventoryNotFoundException.class, () -> service.getInventoryById("missing"));
        when(warehouseRepository.existsById("w")).thenReturn(true);
        when(inventoryRepository.findByWarehouseId("w")).thenReturn(List.of(entity));
        assertEquals(1, service.getWarehouseInventory("w").size());
        when(warehouseRepository.existsById("missing")).thenReturn(false);
        assertThrows(WarehouseNotFoundException.class, () -> service.getWarehouseInventory("missing"));
        when(productRepository.existsById("missing")).thenReturn(false);
        assertThrows(ProductNotFoundException.class, () -> service.getProductStock("missing"));
    }

    @Test
    void setsAndIncreasesStockAndHandlesMissingInventory() {
        InventoryEntity entity = inventory();
        when(inventoryRepository.findById("i")).thenReturn(Optional.of(entity));
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(InventoryEntity.class))).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().quantity(7).build());
        assertEquals(7, service.setStock("i", SetStockRequestDTO.builder().quantity(7).build()).getQuantity());
        UpdateResult matched = mock(UpdateResult.class);
        when(mongoTemplate.updateFirst(any(), any(), eq(InventoryEntity.class))).thenReturn(matched);
        when(matched.getMatchedCount()).thenReturn(1L);
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().quantity(12).build());
        assertEquals(12, service.increaseStock("i", StockAdjustmentRequestDTO.builder().quantity(2).build()).getQuantity());
        when(matched.getMatchedCount()).thenReturn(0L);
        assertThrows(InventoryNotFoundException.class,
                () -> service.increaseStock("i", StockAdjustmentRequestDTO.builder().quantity(2).build()));
        when(inventoryRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(InventoryNotFoundException.class,
                () -> service.setStock("missing", SetStockRequestDTO.builder().quantity(1).build()));
    }

    @Test
    void decreaseUsesConditionalAtomicUpdateAndRejectsInsufficientStock() {
        InventoryEntity entity = inventory();
        when(inventoryRepository.findById("i")).thenReturn(Optional.of(entity));
        when(warehouseRepository.findById("w")).thenReturn(Optional.of(active()));
        StockAdjustmentRequestDTO request = StockAdjustmentRequestDTO.builder().quantity(4).build();
        UpdateResult matched = mock(UpdateResult.class); when(matched.getMatchedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(), any(), eq(InventoryEntity.class))).thenReturn(matched);
        when(mapper.toResponseDTO(entity)).thenReturn(InventoryResponseDTO.builder().quantity(6).build());
        assertEquals(6, service.decreaseStock("i", request).getQuantity());
        verify(mongoTemplate).updateFirst(any(), any(), eq(InventoryEntity.class));
        when(matched.getMatchedCount()).thenReturn(0L);
        assertThrows(InsufficientStockException.class, () -> service.decreaseStock("i", request));
    }

    @Test
    void deletesInventoryAndRejectsMissingInventory() {
        when(inventoryRepository.findById("i")).thenReturn(Optional.of(inventory()));
        service.deleteInventory("i");
        verify(inventoryRepository).deleteById("i");
        when(inventoryRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(InventoryNotFoundException.class, () -> service.deleteInventory("missing"));
    }
}
