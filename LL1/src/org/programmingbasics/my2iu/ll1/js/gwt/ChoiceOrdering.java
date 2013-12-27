package org.programmingbasics.my2iu.ll1.js.gwt;

import java.util.HashMap;
import java.util.Map;

public class ChoiceOrdering
{
  static final String[] ordering = new String[] {
    "\";\"", "\");\"", "\",\"",  

    "Identifier", "IdentifierName", "NumericLiteral", "StringLiteral",
    "\"null\"", "\"true\"", "\"false\"", "\"this\"",  

    "\"}\"", "\"{\"",
    "\")\"", "\"(\"",
    "\"]\"", "\"[\"", 
    "\".\"", 
    "\"var\"",

    
    "\"=\"", 
    
    "\"==\"", "\"!=\"", "\"<\"", "\">\"", "\">=\"", "\"<=\"", 
    "\"&&\"", "\"||\"", "\"!\"", 
    "\"===\"", "\"!==\"",    
    
    "\"++\"", "\"--\"", 
    "\"?\"", "\":\"",    
    "\"+\"", "\"-\"",  "\"*\"", "\"/\"", "\"%\"", 
    
    "\"^\"", "\"<<\"", "\">>\"", "\"~\"", "\"&\"", "\">>>\"", "\"|\"",       
    
    "\"return\"", "\"break\"", "\"continue\"", 
    "\"function\"", 
    "\"if\"", "\"else_if\"", "\"else\"",      
    "\"for\"", "\"for...in\"", 
    "\"while\"",
    "\"do\"",
    "\"switch\"", "\"default\"", "\"case\"",  
    "\"try\"", "\"catch\"", "\"finally\"",  
    "\"throw\"",  
    "\"new\"", "\"delete\"", 

    "\"typeof\"", "\"instanceof\"", 
    "\"void\"", "\"in\"",   
    

    "\"+=\"", "\"-=\"", "\"<<=\"", "\"%=\"", "\"^=\"", "\">>>=\"", 
    "\"&=\"", "\">>=\"", "\"|=\"", "\"*=\"", "\"/=\"",   

    "\"get\"", "\"set\"", "RegularExpressionLiteral",  
    
    "LabelledStatement", "Label", "\"debugger\"", "\"with\"", 
  };
  
  public final static Map<String, Integer> TERMINAL_ORDER = new HashMap<String, Integer>();
  static {
    for (int n = 0; n < ordering.length; n++)
      TERMINAL_ORDER.put(ordering[n], n);
  }
}
