package com.page5of4.common.data;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.transaction.annotation.Transactional;

public class JpaRepository implements Repository {

   @PersistenceContext
   private EntityManager entityManager;

   public EntityManager getEntityManager() {
      return entityManager;
   }

   @Override
   @Transactional
   public Object findById(Class<?> entityClass, Object id) {
      if(id instanceof String) {
         if(id.toString().length() == 0) {
            return null;
         }
         id = Long.parseLong(id.toString());
      }
      return entityManager.find(entityClass, id);
   }

   @Override
   @Transactional
   public List<?> findAll(Class<?> entityClass) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<?> query = cb.createQuery(entityClass);
      Root<?> from = query.from(entityClass);
      return entityManager.createQuery(query).getResultList();
   }

   @Override
   @Transactional
   public List<?> findAll(Class<?> entityClass, int firstRow, int maximumRows) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<?> query = cb.createQuery(entityClass);
      Root<?> from = query.from(entityClass);
      TypedQuery<?> q = entityManager.createQuery(query);
      q.setFirstResult(firstRow);
      q.setMaxResults(maximumRows);
      return q.getResultList();
   }

   @Override
   @Transactional
   public long countAll(Class<?> entityClass) {
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<Long> cq = cb.createQuery(Long.class);
      CriteriaQuery<Long> query = cq.select(cb.count(cq.from(entityClass)));
      return entityManager.createQuery(query).getSingleResult();
   }

   @Override
   public Object getIdOf(Object entity) {
      try {
         Method idGetter = Finders.findIdGetter(entity.getClass());
         return idGetter.invoke(entity);
      }
      catch(Exception error) {
         throw new RuntimeException("Error getting Id: " + entity, error);
      }
   }

   @Override
   @Transactional
   public Object add(Class<?> entityClass, Object entity) {
      entityManager.persist(entity);
      return entity;
   }

   @Override
   @Transactional
   public Object update(Class<?> entityClass, Object entity) {
      return entityManager.merge(entity);
   }

   @Override
   @Transactional
   public void delete(Class<?> entityClass, Object entity) {
      entityManager.remove(entity);
   }

}
