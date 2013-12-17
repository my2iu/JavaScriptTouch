package org.programmingbasics.my2iu.ll1.generator;

import java.util.ArrayList;
import java.util.List;

public class Production
{
  public String from;
  public List<String> to;
  public List<String> toFull;
  public boolean isPreferred;
  
  public static String ENDL = "<endl>";
  public static String TAB = "<tab>";
  public static String UNTAB = "<untab>";
  
  
  public Production(String from, String [] toArray)
  {
    this(from, arrayToString(toArray));
  }
  
  public Production(String from, List<String> to)
  {
    this.from = from;
    this.to = to;
    
    // Check if production starts with +, tagging it as a preferred production
    if (to.size() > 0 && to.get(0).equals("+"))
    {
      isPreferred = true;
      to.remove(0);
    }
  }
  
  public static boolean isPrettyPrintToken(String token)
  {
    if (token.equals(ENDL)) return true;
    if (token.equals(TAB)) return true;
    if (token.equals(UNTAB)) return true;
    return false;
  }
  
  public void stripPrettyPrintInstructions()
  {
    // Check for any pretty print tags in the production.
    toFull = new ArrayList<String>();
    toFull.addAll(to);
    List<String> toDelete = new ArrayList<String>();
    for (String s: to)
    {
      if (s.equals(TAB) || s.equals(UNTAB) || s.equals(ENDL))
        toDelete.add(s);
    }
    to.removeAll(toDelete);
  }

  private static List<String> arrayToString(String[] toArray)
  {
    List<String> to = new ArrayList<String>();
    for (String expansion : toArray)
      to.add(expansion);
    return to;
  }
  
  public String toString()
  {
    String toReturn = from + " =>";
    for (String token: to)
      toReturn += " " + token;
    return toReturn;
  }
}
