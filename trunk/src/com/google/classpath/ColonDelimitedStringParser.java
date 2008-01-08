package com.google.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Splits up a string on colons or semicolons for pulling out as a list of
 * Files, Strings, or URLs.
 *
 * User: jwolter
 * Date: Dec 13, 2007
 */
public class ColonDelimitedStringParser {
    private final String DELIMITER_REGEX = "(:|;)";
    private List<String> list = new ArrayList<String>();

    public ColonDelimitedStringParser(String str) {
      if (str != null) {
        list.addAll(Arrays.asList(str.split(DELIMITER_REGEX)));
      }
    }

    public List<URL> getListOfURLs() {
      List<URL> classpath = new ArrayList<URL>();
      try {
        for (String path : list) {
          URL url = new File(path).toURI().toURL();
          classpath.add(url);
        }
      } catch (MalformedURLException e) {
        throw new RuntimeException("Error parsing classpath. " + e.getMessage());
      }
      return classpath;
    }

    public List<File> getListOfFiles() {
      List<File> classpath = new ArrayList<File>();
      for (String path : list) {
        File file = new File(path);
        classpath.add(file);
      }
      return classpath;
    }

    public List<String> getListOfStrings() {
        return list;
    }
}
