package org.programmingbasics.my2iu.ll1.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LLParser
{
  Map<String, Map<String, Production>> parsingTable;
  Set<String> nonTerminals;
  
  public LLParser(Map<String, Map<String, Production>> parsingTable, Set<String> nonTerminals)
  {
    this.parsingTable = parsingTable;
    this.nonTerminals = nonTerminals;
  }
  
  int indentation = 0;
  String program = "";
  boolean isStartOfLine = true;
  public void runInteractiveBuilder() throws IOException
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    List<String> parsingStack = new ArrayList<String>();
    parsingStack.add("Program");
    
    while (parsingStack.size() > 0)
    {
      // Remove terminals from top of stack
      while (isTerminal(parsingStack.get(parsingStack.size()-1)))
      {
        String topOfStack = parsingStack.get(parsingStack.size()-1); 
        parsingStack.remove(parsingStack.size()-1);
    
        if (Production.isPrettyPrintToken(topOfStack)) 
        {
          handlePrettyPrint(topOfStack);
          continue;
        }
        
        // Otherwise, remove any terminals and insert it into the stream.
        if (isStartOfLine)
        {
          for (int n = 0; n < indentation; n++)
            program += "  ";
        }
        program += " " + topOfStack;
        isStartOfLine = false;
      }

      System.out.println(program);
      
      // Show options and get next token
      String token = chooseOptions(in, parsingStack);
      if (token == null)
      {
        // For debugging purposes, we allow people to enter a bad option,
        // then we return null, and replay this parsing step.
        continue;
      }
      
      // Parse the token
      while (isNonTerminal(parsingStack.get(parsingStack.size()-1)))
      {
        // Apply the appropriate production
        parseToken(parsingStack, token);
        
        while (Production.isPrettyPrintToken(parsingStack.get(parsingStack.size()-1))) 
        {
          handlePrettyPrint(parsingStack.get(parsingStack.size()-1));
          parsingStack.remove(parsingStack.size()-1);
        }
      }
      assert(parsingStack.get(parsingStack.size()-1).equals(token));
    }
    in.close();
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
  
  private String chooseOptions(BufferedReader in, List<String> parsingStack) throws IOException
  {
    String topOfStack = parsingStack.get(parsingStack.size() - 1);
    List<String> options = new ArrayList<String>(parsingTable.get(topOfStack).keySet());
    List<Integer> validOptions = new ArrayList<Integer>();
    System.out.println(topOfStack);
    for (int n = 0; n < options.size(); n++) {
      // Some expansions aren't legal. Do a speculative parse to see if 
      // the given parse is legal or not.
      if (speculativeParse(parsingStack, options.get(n)))
        validOptions.add(n);
    }
    if (validOptions.size() == 1)
      return options.get(validOptions.get(0));
    for (int validOption: validOptions)
      System.out.println("  " + validOption + " " + options.get(validOption));
    String line = in.readLine();
    int choice;
    try {
      choice = Integer.parseInt(line);
    } catch (NumberFormatException e) {
      return null;
    }
    return options.get(choice);
  }
  
  private void handlePrettyPrint(String topOfStack)
  {
    // Handle any pretty print instructions.        
    if (topOfStack.equals(Production.ENDL))
    {
      program += "\n";
      isStartOfLine = true;
      return;
    } 
    else if (topOfStack.equals(Production.TAB))
    {
      indentation++;
      return;
    }
    else if (topOfStack.equals(Production.UNTAB))
    {
      indentation--;
      return;
    }
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
