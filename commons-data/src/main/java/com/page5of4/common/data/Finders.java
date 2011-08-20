package com.page5of4.common.data;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import org.springframework.util.ClassUtils;

public class Finders {

   public static Method getFindAllFinder(Class<?> entityClass) {
      String methodName = "findAll";
      Method[] methods = entityClass.getDeclaredMethods();
      for(Method method : methods) {
         if(Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 0 && method.getReturnType().equals(Collection.class)) {
            if(method.getName().equals(methodName)) {
               return method;
            }
         }
      }
      return null;
   }

   public static Method getFindByIdFinder(Class<?> entityClass) {
      String methodName = "find" + getEntityName(entityClass);
      Method[] methods = entityClass.getDeclaredMethods();
      for(Method method : methods) {
         if(Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getReturnType().equals(entityClass)) {
            if(method.getName().equals(methodName)) {
               return method;
            }
         }
      }
      return null;
   }

   public static String getEntityName(Class<?> entityClass) {
      String shortName = ClassUtils.getShortName(entityClass);
      int lastDot = shortName.lastIndexOf('.');
      if(lastDot != -1) {
         return shortName.substring(lastDot + 1);
      }
      else {
         return shortName;
      }
   }

   public static Collection<?> findAndInvokeFindAll(Class<?> entityClass) {
      Method finder = Finders.getFindAllFinder(entityClass);
      if(finder != null) {
         try {
            return (Collection<?>)finder.invoke(null, new Object[0]);
         }
         catch(Exception error) {
            throw new RuntimeException("Error invoking " + finder, error);
         }
      }
      return null;
   }

   public static Object findAndInvokeFindById(Class<?> entityClass, Object id) {
      Method finder = Finders.getFindByIdFinder(entityClass);
      if(finder != null) {
         try {
            return finder.invoke(null, new Object[] { id });
         }
         catch(Exception error) {
            throw new RuntimeException("Error invoking " + finder, error);
         }
      }
      return null;
   }

   public static Method findIdGetter(Class<?> entityClass) {
      String methodName = "getId";
      Method[] methods = entityClass.getDeclaredMethods();
      for(Method method : methods) {
         if(method.getName().equals(methodName)) {
            if(!Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 0) {
               return method;
            }
         }
      }
      return null;
   }

}
