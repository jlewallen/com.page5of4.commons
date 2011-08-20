package com.page5of4.commons.mvc.jquery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.js.ajax.SpringJavascriptAjaxHandler;

public class JQueryAjaxHandler extends SpringJavascriptAjaxHandler {

   private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

   @Override
   protected boolean isAjaxRequestInternal(HttpServletRequest request, HttpServletResponse response) {
      String requestedWithHeader = request.getHeader("X-Requested-With");
      return super.isAjaxRequestInternal(request, response) || XML_HTTP_REQUEST.equals(requestedWithHeader);
   }

}
