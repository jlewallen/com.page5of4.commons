package com.page5of4.commons.mvc.jquery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serve jQuery templates as js files, wrapping them in jQuery.template calls. Very handy, allowing you create simple
 * text files with jquery templates inside of them and then load them as standard javascript dependencies. This way, you
 * don't need to use AJAX to load them (requiring you to block until they've loaded) or include them as string literals
 * in your JSPs.
 * 
 * @author jlewallen
 */
public class JQueryTemplateServlet extends HttpServlet {

   private static final Logger logger = LoggerFactory.getLogger(JQueryTemplateServlet.class);

   private static final long serialVersionUID = 1L;

   private static final String EXTENSION = "tmpl";

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      logger.info("GET {} {}", req.getContextPath(), req.getPathInfo());

      ServletContext servletContext = getServletContext();

      String path = req.getPathInfo();
      String real = servletContext.getRealPath(path);
      List<String> candidates = new ArrayList<String>();
      candidates.add(real);
      candidates.add(removeExtension(real, EXTENSION));
      candidates.add(servletContext.getRealPath("/WEB-INF/" + path));
      candidates.add(servletContext.getRealPath("/WEB-INF/" + removeExtension(path, EXTENSION)));

      Set<File> paths = new HashSet<File>();
      for(String candidate : candidates) {
         File target = new File(candidate);
         if(target.isDirectory()) {
            for(File child : target.listFiles(new FilenameFilter() {
               public boolean accept(File dir, String name) {
                  return name.endsWith("." + EXTENSION);
               }
            })) {
               paths.add(child);
            }
         }
         if(target.isFile()) {
            paths.add(target);
         }
      }

      if(paths.size() == 0) {
         throw new ServletException("No template files matching: " + path + "\nConsidered:\n" + StringUtils.join(candidates.toArray(new String[0]), "\n"));
      }

      PrintWriter writer = resp.getWriter();
      for(File file : paths) {
         write(writer, file);
      }
   }

   void write(PrintWriter writer, File file) throws IOException {
      if(!file.canRead()) {
         throw new FileNotFoundException("Unable to read: " + file);
      }

      String name = removeExtension(file.getName(), EXTENSION);
      String template = readFile(file.getPath());

      writer.write("jQuery.template('");
      writer.write(name);
      writer.write("', '");
      writer.write(StringEscapeUtils.escapeJavaScript(template));
      writer.write("');");
      writer.write("\n");
   }

   static String removePathPrefix(String path) {
      return path.substring(path.indexOf(File.separatorChar));
   }

   static String removeExtension(String path, String extension) {
      return path.replaceAll("." + extension + "$", "");
   }

   String readFile(String path) throws IOException {
      StringBuilder writer = new StringBuilder();
      FileReader fileReader = new FileReader(path);
      BufferedReader reader = new BufferedReader(fileReader);
      String line;
      while((line = reader.readLine()) != null) {
         writer.append(line);
         writer.append("\n");
      }
      return writer.toString();
   }
}
