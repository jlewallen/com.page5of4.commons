package com.page5of4.commons.mvc.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.Definition;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.context.TilesRequestContext;
import org.apache.tiles.context.TilesRequestContextFactory;
import org.apache.tiles.impl.BasicTilesContainer;
import org.apache.tiles.servlet.context.ServletTilesRequestContextFactory;
import org.apache.tiles.servlet.context.ServletUtil;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.JstlUtils;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.tiles2.TilesView;

import com.page5of4.commons.mvc.jquery.JQueryAjaxHandler;

/**
 * A {@link TilesView} that fixes several issues with fragments rendering.
 * 
 * @author jlewallen
 */
public class CustomAjaxTilesView extends TilesView {

   private static final String FRAGMENTS_PARAM = "fragments";

   private TilesRequestContextFactory tilesRequestContextFactory;

   private AjaxHandler ajaxHandler = new JQueryAjaxHandler();

   public void afterPropertiesSet() throws Exception {
      super.afterPropertiesSet();
      tilesRequestContextFactory = new ServletTilesRequestContextFactory();
      tilesRequestContextFactory.init(new HashMap());
   }

   public AjaxHandler getAjaxHandler() {
      return ajaxHandler;
   }

   public void setAjaxHandler(AjaxHandler ajaxHandler) {
      this.ajaxHandler = ajaxHandler;
   }

   protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

      ServletContext servletContext = getServletContext();
      if(ajaxHandler.isAjaxRequest(request, response)) {
         BasicTilesContainer container = (BasicTilesContainer)ServletUtil.getCurrentContainer(request, servletContext);
         if(container == null) {
            throw new ServletException("Tiles container is not initialized. " + "Have you added a TilesConfigurer to your web application context?");
         }

         String[] fragmentsToRender = getRenderFragments(model, request, response);
         if(fragmentsToRender.length == 0) {
            logger.debug("An Ajax request was detected, but no fragments were specified to be re-rendered. Falling back to full page render. This can cause unpredictable results when processing the ajax response on the client: "
                  + request.getRequestURL());
            renderMergedOutputModelDefault(model, request, response);
            return;
         }

         exposeModelAsRequestAttributes(model, request);
         JstlUtils.exposeLocalizationContext(new RequestContext(request, servletContext));

         TilesRequestContext tilesRequestContext = tilesRequestContextFactory.createRequestContext(container.getApplicationContext(), new Object[] { request, response });
         Definition compositeDefinition = container.getDefinitionsFactory().getDefinition(getUrl(), tilesRequestContext);

         Map flattenedAttributeMap = new HashMap();
         flattenAttributeMap(container, tilesRequestContext, flattenedAttributeMap, compositeDefinition, request, response);
         addRuntimeAttributes(container, flattenedAttributeMap, request, response);

         if(fragmentsToRender.length > 1) {
            request.setAttribute(ServletUtil.FORCE_INCLUDE_ATTRIBUTE_NAME, true);
         }

         for(int i = 0; i < fragmentsToRender.length; i++) {
            Attribute attributeToRender = (Attribute)flattenedAttributeMap.get(fragmentsToRender[i]);

            if(attributeToRender == null) {
               throw new ServletException("No tiles attribute with a name of '" + fragmentsToRender[i] + "' could be found for the current view: " + this);
            }
            else {
               container.startContext(request, response).inheritCascadedAttributes(compositeDefinition);
               container.render(attributeToRender, request, response);
               container.endContext(request, response);
            }
         }
      }
      else {
         renderMergedOutputModelDefault(model, request, response);
      }
   }

   protected void renderMergedOutputModelDefault(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
      ServletContext servletContext = getServletContext();
      TilesContainer container = ServletUtil.getCurrentContainer(request, servletContext);
      if(container == null) {
         throw new ServletException("Tiles container is not initialized. " + "Have you added a TilesConfigurer to your web application context?");
      }

      exposeModelAsRequestAttributes(model, request);
      JstlUtils.exposeLocalizationContext(new RequestContext(request, servletContext));

      if(!response.isCommitted()) {
         // Tiles is going to use a forward, but some web containers (e.g. OC4J 10.1.3)
         // do not properly expose the Servlet 2.4 forward request attributes... However,
         // must not do this on Servlet 2.5 or above, mainly for GlassFish compatibility.
         /*
         if(this.exposeForwardAttributes) {
            try {
               WebUtils.exposeForwardRequestAttributes(request);
            }
            catch(Exception ex) {
               // Servlet container rejected to set internal attributes, e.g. on TriFork.
               this.exposeForwardAttributes = false;
            }
         }
         */
      }

      container.render(getUrl(), request, response);
   }

   protected String[] getRenderFragments(Map model, HttpServletRequest request, HttpServletResponse response) {
      String attrName = request.getParameter(FRAGMENTS_PARAM);
      String[] renderFragments = StringUtils.commaDelimitedListToStringArray(attrName);
      return StringUtils.trimArrayElements(renderFragments);
   }

   /**
    * <p>
    * Iterate over all attributes in the given Tiles definition. Every attribute value that represents a template (i.e.
    * start with "/") or is a nested definition is added to a Map. The method class itself recursively to traverse
    * nested definitions.
    * </p>
    * 
    * @param container
    *           the TilesContainer
    * @param requestContext
    *           the TilesRequestContext
    * @param resultMap
    *           the output Map where attributes of interest are added to.
    * @param compositeDefinition
    *           the definition to search for attributes of interest.
    * @param request
    *           the servlet request
    * @param response
    *           the servlet response
    */
   protected void flattenAttributeMap(BasicTilesContainer container, TilesRequestContext requestContext, Map resultMap, Definition compositeDefinition, HttpServletRequest request,
         HttpServletResponse response) {
      Iterator iterator = compositeDefinition.getAttributeNames();
      while(iterator.hasNext()) {
         String attributeName = (String)iterator.next();
         Attribute attribute = compositeDefinition.getAttribute(attributeName);
         if(attribute.getValue() == null || !(attribute.getValue() instanceof String)) {
            continue;
         }
         String value = attribute.getValue().toString();
         if(value.startsWith("/")) {
            resultMap.put(attributeName, attribute);
         }
         else if(container.isValidDefinition(value, new Object[] { request, response })) {
            resultMap.put(attributeName, attribute);
            Definition nestedDefinition = container.getDefinitionsFactory().getDefinition(value, requestContext);
            Assert.isTrue(nestedDefinition != compositeDefinition, "Circular nested definition: " + value);
            flattenAttributeMap(container, requestContext, resultMap, nestedDefinition, request, response);
         }
      }
   }

   /**
    * <p>
    * Iterate over dynamically added Tiles attributes (see "Runtime Composition" in the Tiles documentation) and add
    * them to the output Map passed as input.
    * </p>
    * 
    * @param container
    *           the Tiles container
    * @param resultMap
    *           the output Map where attributes of interest are added to.
    * @param request
    *           the Servlet request
    * @param response
    *           the Servlet response
    */
   protected void addRuntimeAttributes(BasicTilesContainer container, Map resultMap, HttpServletRequest request, HttpServletResponse response) {
      AttributeContext attributeContext = container.getAttributeContext(new Object[] { request, response });
      Set attributeNames = new HashSet();
      if(attributeContext.getLocalAttributeNames() != null) {
         attributeNames.addAll(attributeContext.getLocalAttributeNames());
      }
      if(attributeContext.getCascadedAttributeNames() != null) {
         attributeNames.addAll(attributeContext.getCascadedAttributeNames());
      }
      Iterator iterator = attributeNames.iterator();
      while(iterator.hasNext()) {
         String name = (String)iterator.next();
         Attribute attr = attributeContext.getAttribute(name);
         resultMap.put(name, attr);
      }
   }

}
