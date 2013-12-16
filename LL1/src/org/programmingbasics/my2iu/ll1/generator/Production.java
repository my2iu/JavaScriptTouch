package org.programmingbasics.my2iu.ll1.generator;

import java.util.ArrayList;
import java.util.List;

public class Production
{
  public String from;
  public List<String> to;
  
  public Production(String from, String [] toArray)
  {
    this.from = from;
    to = new ArrayList<String>();
    for (String expansion : toArray)
    {
      to.add(expansion);
    }
  }
  
  public Production(String from, List<String> to)
  {
    this.from = from;
    this.to = to;
  }
  
  public String toString()
  {
    String toReturn = from + " =>";
    for (String token: to)
      toReturn += " " + token;
    return toReturn;
  }
}
