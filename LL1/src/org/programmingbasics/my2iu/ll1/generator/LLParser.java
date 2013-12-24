package org.programmingbasics.my2iu.ll1.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GwtIncompatible;

public class LLParser
{
  final Map<String, Map<String, Production>> parsingTable;
  final Set<String> nonTerminals;
  final PrettyFormatter formatter;
  
  public List<String> parsingStack = new ArrayList<String>();
  
  public LLParser(Map<String, Map<String, Production>> parsingTable, Set<String> nonTerminals, PrettyFormatter formatter)
  {
    this.parsingTable = parsingTable;
    this.nonTerminals = nonTerminals;
    this.formatter = formatter;
  }
  
  @GwtIncompatible public static void runInteractiveBuilder(LL1Generator ll1) throws IOException
  {
    PrettyTextFormatter formatter = new PrettyTextFormatter();
    LLParser parser = ll1.createParser(formatter);

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    parser.parsingStack.add("Program");
    
    while (parser.parsingStack.size() > 0)
    {
      parser.automatchTerminals();
      
      System.out.println(formatter.getProgramText());
      
      // Show options and get next token
      String token = chooseOptions(parser, in, parser.parsingStack);
      if (token == null)
      {
        // For debugging purposes, we allow people to enter a bad option,
        // then we return null, and replay this parsing step.
        continue;
      }
      
      parser.fullParseToken(token);
    }
    in.close();
  }

  @GwtIncompatible 
  private static String chooseOptions(LLParser parser, BufferedReader in, List<String> parsingStack) throws IOException
  {
    List<String> options = parser.findValidOptions(parsingStack);
    if (options.size() == 1)
      return options.get(0);
    for (int n = 0; n < options.size(); n++)
      System.out.println("  " + n + " " + options.get(n));
    String line = in.readLine();
    int choice;
    try {
      choice = Integer.parseInt(line);
    } catch (NumberFormatException e) {
      return null;
    }
    return options.get(choice);
  }

  public void fullParseToken(String token)
  {
    // Parse the token
    while (isNonTerminal(parsingStack.get(parsingStack.size()-1)))
    {
      // Apply the appropriate production
      parseToken(parsingStack, token);
      
      while (Production.isPrettyPrintToken(parsingStack.get(parsingStack.size()-1))) 
      {
        formatter.handlePrettyPrint(parsingStack.get(parsingStack.size()-1));
        parsingStack.remove(parsingStack.size()-1);
      }
    }
    assert(parsingStack.get(parsingStack.size()-1).equals(token));
  }

  public void automatchTerminals()
  {
    String terminalAtTop = topTerminal();
    while (terminalAtTop != null)
    {
      if (Production.isPrettyPrintToken(terminalAtTop)) 
        formatter.handlePrettyPrint(terminalAtTop);
      else 
        // Otherwise, remove any terminals and insert it into the stream.
        formatter.insertToken(terminalAtTop);
      
      terminalAtTop = topTerminal();
    }
  }

  public String nextRealTerminal()
  {
    String terminalAtTop = topTerminal();
    while (terminalAtTop != null)
    {
      if (Production.isPrettyPrintToken(terminalAtTop)) 
        formatter.handlePrettyPrint(terminalAtTop);
      else 
      {
        // Otherwise, remove any terminals and insert it into the stream.
        return terminalAtTop;
      }
      
      terminalAtTop = topTerminal();
    }
    return null;
  }

  
  /**
   * @return terminal at top of stack or null if only a non-terminal is there
   */
  public String topTerminal()
  {
    // Remove terminals from top of stack
    if (isTerminal(parsingStack.get(parsingStack.size()-1)))
    {
      String topOfStack = parsingStack.get(parsingStack.size()-1); 
      parsingStack.remove(parsingStack.size()-1);
      return topOfStack;
    }
    return null;
  }
  
  private boolean speculativeParse(List<String> parsingStack, String token)
  {
    // Copy the stack
    List<String> stack = new ArrayList<String>();
    stack.addAll(parsingStack);
    while (isNonTerminal(stack.get(stack.size()-1)))
    {
      // Apply the appropriate production
      if (!parseToken(stack, token))
        return false;
      if (stack.isEmpty())
        return false;

      // Strip pretty print tokens
      while (Production.isPrettyPrintToken(stack.get(stack.size()-1)))
        stack.remove(stack.size() - 1);
    }
    return stack.get(stack.size()-1).equals(token);
  }
  
  private boolean parseToken(List<String> parsingStack, String token)
  {
    String topOfStack = parsingStack.get(parsingStack.size()-1);
    Production p = parsingTable.get(topOfStack).get(token);
    if (p == null) return false;
    parsingStack.remove(parsingStack.size()-1);
    for (int n = p.toFull.size() - 1; n >= 0; n--)
    {
      parsingStack.add(p.toFull.get(n));
    }
    return true;
  }

  public List<String> findValidOptions()
  {
    return findValidOptions(parsingStack);
  }
  
  private List<String> findValidOptions(List<String> parsingStack)
  {
    String topOfStack = parsingStack.get(parsingStack.size() - 1);
    List<String> options = new ArrayList<String>(parsingTable.get(topOfStack).keySet());
    List<String> validOptions = new ArrayList<String>();
    System.out.println(topOfStack);
    for (int n = 0; n < options.size(); n++) {
      // Some expansions aren't legal. Do a speculative parse to see if 
      // the given parse is legal or not.
      if (speculativeParse(parsingStack, options.get(n)))
        validOptions.add(options.get(n));
    }
    return validOptions;
  }
  
  boolean isNonTerminal(String token)
  {
    return nonTerminals.contains(token);
  }
  
  boolean isTerminal(String token)
  {
    return !isNonTerminal(token);
  }
}
