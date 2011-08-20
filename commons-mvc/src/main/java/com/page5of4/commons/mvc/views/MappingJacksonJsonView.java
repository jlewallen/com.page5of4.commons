package com.page5of4.commons.mvc.views;

import java.util.Map;

public class MappingJacksonJsonView extends org.springframework.web.servlet.view.json.MappingJacksonJsonView {
   @Override
   @SuppressWarnings("unchecked")
   protected Object filterModel(Map<String, Object> model) {
      Map<String, Object> filtered = (Map<String, Object>)super.filterModel(model);
      if(filtered.isEmpty() || filtered.size() > 1) {
         return filtered;
      }
      return filtered.values().iterator().next();
   }
}
