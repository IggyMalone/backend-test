package com.ijeremic.backendtest.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Created by Iggy on 26-Jan-2020
 */
@ExtendWith(MockitoExtension.class)
public class GenericDaoTest
{
  GenericDao dao;

  @Mock
  EntityManagerFactory mockEMF;

  @Mock
  EntityManager entityManager;

  @Mock
  EntityTransaction entityTransaction;

  @BeforeEach
  void init()
  {
    dao = new TestGenericDao();
    GenericDao.emf = mockEMF;
    dao.entityManager = entityManager;
  }

  @AfterAll
  static void cleanup()
  {
    GenericDao.emf = null;
  }

  @DisplayName("Regular test getById()")
  @Test
  void getByIdTest()
  {
    Long id = 1L;
    Mockito.lenient().when(entityManager.find(TestEntity.class, id)).thenReturn(new TestEntity());
    Optional<TestEntity> result = dao.getById(1L);

    assertTrue(result.isPresent());
  }

  @DisplayName("getById() should return empty optional object if entity for a given id cannot be found")
  @Test
  void getByIdNotFoundTest()
  {
    Long id = 1L;
    Mockito.lenient().when(entityManager.find(TestEntity.class, id)).thenReturn(null);
    Optional<TestEntity> result = dao.getById(1L);

    assertFalse(result.isPresent());
  }

  @DisplayName("Regular test persist()")
  @Test
  void persistTest()
  {
    TestEntity entity = new TestEntity();
    TestEntity result = (TestEntity) dao.persist(entity);

    Mockito.verify(entityManager, Mockito.times(1)).persist(entity);
  }

  @DisplayName("Regular test update()")
  @Test
  void updateTest()
  {
    Long id = 1L;
    TestEntity entity = new TestEntity();
    TestEntity dbEntity = new TestEntity();
    Mockito.lenient().when(entityManager.find(TestEntity.class, id)).thenReturn(dbEntity);
    Mockito.lenient().when(entityManager.merge(entity)).thenReturn(new TestEntity());

    Optional<TestEntity> result = dao.update(id, entity);

    Mockito.verify(entityManager, Mockito.times(1)).merge(entity);
    assertTrue(result.isPresent());
    assertNotEquals(result.get(), dbEntity);
  }

  @DisplayName("Regular test update()")
  @Test
  void updateEntityNotFoundTest()
  {
    Long id = 1L;
    TestEntity entity = new TestEntity();
    Mockito.lenient().when(entityManager.find(TestEntity.class, id)).thenReturn(null);

    Exception thrown = assertThrows(EntityNotFoundException.class,
        () -> dao.update(id, entity),
        "");

    Mockito.verify(entityManager, Mockito.times(0)).merge(ArgumentMatchers.any(TestEntity.class));
    assertEquals(thrown.getClass(), EntityNotFoundException.class);
  }

  @DisplayName("Regular test remove()")
  @Test
  void removeTest()
  {
    TestEntity entity = new TestEntity();

    dao.remove(entity);

    Mockito.verify(entityManager, Mockito.times(1)).remove(entity);
  }

  @DisplayName("Regular test startTransaction()")
  @Test
  void startTransactionTest()
  {
    Mockito.lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);

    dao.startTransaction();

    Mockito.verify(entityManager.getTransaction(), Mockito.times(1)).begin();
  }

  @DisplayName("Regular test commitTransaction()")
  @Test
  void commitTransactionTest()
  {
    Mockito.lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);

    dao.commitTransaction();

    Mockito.verify(entityManager.getTransaction(), Mockito.times(1)).commit();
  }

  @DisplayName("Regular test rollbackTransaction()")
  @Test
  void rollbackTransactionTest()
  {
    Mockito.lenient().when(entityManager.getTransaction()).thenReturn(entityTransaction);
    Mockito.lenient().when(entityTransaction.isActive()).thenReturn(true);

    dao.rollbackTransaction();

    Mockito.verify(entityManager.getTransaction(), Mockito.times(1)).rollback();
  }

  private class TestEntity
  {
    Long id;
  }
  private class TestGenericDao extends GenericDao<TestEntity, Long> {}

}
