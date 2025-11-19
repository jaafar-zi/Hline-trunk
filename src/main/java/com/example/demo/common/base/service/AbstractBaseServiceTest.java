package com.example.demo.common.base.service;

import com.example.demo.common.base.entity.TestEntity;
import com.example.demo.common.base.repository.TestRepository;
import com.example.demo.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractBaseServiceTest {

    @Mock
    private TestRepository repository;

    @InjectMocks
    private TestService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new TestService(repository);
    }

    @Test
    void save_ShouldCallRepositoryAndAfterSave() {
        TestEntity entity = new TestEntity(1L, "Test");
        when(repository.save(entity)).thenReturn(entity);

        TestEntity result = service.save(entity);

        assertEquals(entity, result);
        assertTrue(service.isAfterSaveCalled());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void update_ShouldFindThenSave() {
        TestEntity entity = new TestEntity(1L, "Updated");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        TestEntity result = service.update(1L, entity);

        assertEquals(entity, result);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(entity);
    }

    @Test
    void update_ShouldThrowWhenEntityNotFound() {
        TestEntity entity = new TestEntity(99L, "Missing");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, entity));
        verify(repository, times(0)).save(any());
    }

    @Test
    void findByIdOrThrow_ShouldReturnEntity() {
        TestEntity entity = new TestEntity(1L, "Found");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TestEntity result = service.findByIdOrThrow(1L);

        assertEquals(entity, result);
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findByIdOrThrow_ShouldThrowWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.findByIdOrThrow(1L));
    }

    @Test
    void delete_ShouldRemoveEntityAndTriggerAfterDelete() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        assertTrue(service.isAfterDeleteCalled());
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowWhenNotExists() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.delete(1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void findAll_ShouldReturnList() {
        List<TestEntity> list = List.of(new TestEntity(1L, "One"));
        when(repository.findAll()).thenReturn(list);

        List<TestEntity> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("One", result.get(0).getName());
    }
}
