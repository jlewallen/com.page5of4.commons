package com.page5of4.common.data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActiveRecordRepository implements Repository {

   @Override
   public Object findById(Class<?> entityClass, Object id) {
      return Finders.findAndInvokeFindById(entityClass, id.toString());
   }

   @Override
   public List<?> findAll(Class<?> entityClass, int firstRow, int maximumRows) {
      Collection<?> found = Finders.findAndInvokeFindAll(entityClass);
      if(found == null) {
         return new ArrayList<Object>();
      }
      return new ArrayList<Object>(found);
   }

   @Override
   public List<?> findAll(Class<?> entityClass) {
      Collection<?> found = Finders.findAndInvokeFindAll(entityClass);
      if(found == null) {
         return new ArrayList<Object>();
      }
      return new ArrayList<Object>(found);
   }

   @Override
   public long countAll(Class<?> entityClass) {
      return 0;
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
   public Object add(Class<?> entityClass, Object entity) {
      return entity;
   }

   @Override
   public Object update(Class<?> entityClass, Object entity) {
      return entity;
   }

   @Override
   public void delete(Class<?> entityClass, Object entity) {

   }

}
