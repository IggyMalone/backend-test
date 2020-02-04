package com.ijeremic.backendtest.model.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

/**
 * Generic DAO class used by all other DAO classes for base CRUD operations
 *
 * Created by Iggy on 19-Jan-2020
 */
public abstract class GenericDao<T, ID extends Serializable>
{
  final static Logger logger = Logger.getLogger(GenericDao.class.getName());
  protected static final String PERSISTENCE_UNIT_NAME = "backend-test-pu";

  protected static EntityManagerFactory emf;

  @PersistenceContext(unitName = "backend-test-pu", type = PersistenceContextType.EXTENDED)
  protected EntityManager entityManager;

  protected Class<T> entityClass;

  public GenericDao()
  {
    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
    entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];

    if (emf == null)
    {
      emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
    entityManager = emf.createEntityManager();
  }

  /**
   * Used to get an entity from the database by its id.
   *
   * @param id id of the entity
   * @return entity
   */
  public Optional<T> getById(ID id)
  {
    T entity = entityManager.find(entityClass, id);
    if (entity == null)
    {
      logger.log(Level.WARNING, "Entity {0} not found for a given id: {1}", new Object[]{entityClass, id});
      return Optional.empty();
    }

    return Optional.of(entity);
  }

  /**
   * Used to insert entity in database.
   *
   * @param entity entity
   * @return stored entity
   */
  public T persist(T entity)
  {
    entityManager.persist(entity);
    return entity;
  }

  /**
   * Used to update existing entity in db.
   *
   * @param entity - entity to be updated
   * @param id     - entity id used for lookup
   * @return optional merged persistent instance
   * @throws EntityNotFoundException if entity not found
   */
  public Optional<T> update(ID id, T entity) throws EntityNotFoundException
  {
    if (!getById(id).isPresent())
    {
      throw new EntityNotFoundException();
    }

    T mergedEntity = entityManager.merge(entity);
    return Optional.ofNullable(mergedEntity);
  }

  /**
   * Used to remove specific entity from the database.
   *
   * @param entity entity
   */
  public void remove(T entity)
  {
    entityManager.remove(entity);
  }

  /**
   * Used to start transaction in entity manager
   *
   */
  public void startTransaction()
  {
    entityManager.getTransaction().begin();
  }

  /**
   * Used to commit transaction in entity manager
   *
   */
  public void commitTransaction()
  {
    entityManager.getTransaction().commit();
  }

  /**
   * Used to rollback transaction in entity manager
   *
   */
  public void rollbackTransaction()
  {
    if (entityManager.getTransaction().isActive())
    {
      entityManager.getTransaction().rollback();
    }
  }

}
