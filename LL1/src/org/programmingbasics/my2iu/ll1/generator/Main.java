package org.programmingbasics.my2iu.ll1.generator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main
{
  public static String EMPTY = "";
  
  String fromNonTerminal;
  List<Production> grammar = new ArrayList<Production>();
  Set<String> nonTerminals = new HashSet<String>();
  Map<String, Set<String>> firstTerminals = new HashMap<String, Set<String>>();
  Map<String, Set<String>> followsTerminals = new HashMap<String, Set<String>>();
  Map<String, Map<String, Production>> parsingTable = new HashMap<String, Map<String, Production>>();
  Map<String, Set<String>> noAcceptTokenException = new HashMap<String, Set<String>>();
  Set<Production> preferredProduction = new HashSet<Production>();
  
  boolean isNonTerminal(String token)
  {
    return nonTerminals.contains(token);
  }
  
  boolean isTerminal(String token)
  {
    return !isNonTerminal(token);
  }
  
  private void readData() throws IOException
  {
    // Start reading in the grammar rules
    FileInputStream inStream = new FileInputStream("grammars/javascript.txt");
    InputStreamReader reader = new InputStreamReader(inStream, Charset.forName("UTF-8"));
    BufferedReader in = new BufferedReader(reader);
    while (true) 
    {
      String line = in.readLine();
      if (line == null) break;
      
      // Skip comments
      if (line.startsWith("#") || line.startsWith("//"))
        continue;
      // Skip blank lines
      if (line.matches("^[ \t\r\n]*$"))
        continue;
       
      // If it begins with whitespace, then we have a production
      if (Character.isWhitespace(line.codePointAt(0)))
      {
        String [] tokens = line.trim().split("[ \t]+");
        boolean isPreferred = false;
        if (tokens.length == 1 && tokens[0].equals("EPSILON"))
        {
          // Special way to denote expansion to empty string
          grammar.add(new Production(fromNonTerminal, new String[0]));
          continue;
        } 
        else if (tokens[0].equals("EXCEPTIION_PEEK_NO_ACCEPT"))
        {
          String terminal = tokens[1];
          if (!noAcceptTokenException.containsKey(fromNonTerminal))
            noAcceptTokenException.put(fromNonTerminal, new HashSet<String>());
          noAcceptTokenException.get(fromNonTerminal).add(terminal);
          continue;
        }
        else if (tokens[0].equals("+"))
        {
          isPreferred = true;
          String [] oldTokens = tokens;
          tokens = new String[oldTokens.length - 1];
          System.arraycopy(oldTokens, 1, tokens, 0, tokens.length);
        }
        Production p = new Production(fromNonTerminal, tokens); 
        grammar.add(p);
        if (isPreferred)
          preferredProduction.add(p);
        continue;
      }
      
      // Otherwise, we have the start of a new rule
      assert(line.trim().endsWith(":"));
      fromNonTerminal  = line.trim().split ("[ \t]+")[0];
    }
    reader.close();
    inStream.close();
  }
  
  public void go() throws IOException
  {
    readData();
    
    calculateNonTerminals();
    expandOptionalTokens();
    calculateFirsts();
    calculateFollows();
    createLLParsingTable();

    printProductions();
    runInteractiveBuilder();
  }

  private void runInteractiveBuilder() throws IOException
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    List<String> parsingStack = new ArrayList<String>();
    parsingStack.add("Program");
    String program = "";
    
    
    while (parsingStack.size() > 0)
    {
      // Remove terminals from top of stack
      while (isTerminal(parsingStack.get(parsingStack.size()-1)))
      {
        program += " " + parsingStack.get(parsingStack.size()-1);
        parsingStack.remove(parsingStack.size()-1);
      }

      System.out.println(program);
      
      // Show options and get next token
      String token = chooseOptions(in, parsingStack);
      
      // Parse the token
      while (isNonTerminal(parsingStack.get(parsingStack.size()-1)))
      {
        // Apply the appropriate production
        parseToken(parsingStack, token);
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
    }
    return stack.get(stack.size()-1).equals(token);
  }
  
  private boolean parseToken(List<String> parsingStack, String token)
  {
    String topOfStack = parsingStack.get(parsingStack.size()-1);
    Production p = parsingTable.get(topOfStack).get(token);
    if (p == null) return false;
    parsingStack.remove(parsingStack.size()-1);
    for (int n = p.to.size() - 1; n >= 0; n--)
    {
      parsingStack.add(p.to.get(n));
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
    int choice = Integer.parseInt(line);
    return options.get(choice);
  }
  
  private void printProductions()
  {
    // Print Productions
    for (Production p: grammar)
    {
      System.out.print(p.from);
      System.out.print(" =>");
      for (String token: p.to)
      {
        System.out.print(" ");
        System.out.print(token);
      }
      System.out.println();
    }
    printBigMap(firstTerminals);
    printBigMap(followsTerminals);
  }

  private void printBigMap(Map<String, Set<String>> map)
  {
    System.out.println("{");
    for (Map.Entry<String, Set<String>> entry: map.entrySet())
      System.out.println("  " + entry.getKey() + " : " + entry.getValue());
    System.out.println("}");
  }
  
  private boolean isNoAcceptException(String nonTerminal, String terminal)
  {
    if (noAcceptTokenException.containsKey(nonTerminal))
      return noAcceptTokenException.get(nonTerminal).contains(terminal);
    return false;
  }
  
  private void createLLParsingTable()
  {
    for (String token : nonTerminals)
    {
      parsingTable.put(token, new HashMap<String, Production>());
    }
    
    for (Production p: grammar)
    {
      boolean hasEmptyPath = true;
      for (String token: p.to)
      {
        if (isTerminal(token))
        {
          hasEmptyPath = false;
          addParsingRule(p.from, token, p);
          break;
        }
        for (String first : firstTerminals.get(token))
        {
          if (first.equals(EMPTY)) continue;
//          if (isNoAcceptException(p.from, first)) continue;
          addParsingRule(p.from, first, p);
        }
        if (!firstTerminals.get(token).contains(EMPTY))
        {
          hasEmptyPath = false;
          break;
        }
      }
      if (hasEmptyPath)
      {
        // It's possible to go through all the rules getting an EMPTY
        for (String follow : followsTerminals.get(p.from))
        {
          addParsingRule(p.from, follow, p);
        }
      }
    }
  }

  private void addParsingRule(String from, String token, Production p)
  {
    if (parsingTable.get(from).containsKey(token))
    {
      // We have a conflict. See if we have a preferred rule.
      if (preferredProduction.contains(parsingTable.get(from).get(token))
          && !preferredProduction.contains(p)) 
      {
        // Existing rule is preferred, go with that.
        return;
      }
      if (!preferredProduction.contains(parsingTable.get(from).get(token))
          && preferredProduction.contains(p))
      {
        // New rule is preferred, replace it.
        parsingTable.get(from).put(token, p);
        return;
      }
      System.err.println("LL parsing conflict with token " + token + " with rules ");
      System.err.println("  " + p);
      System.err.println("  " + parsingTable.get(from).get(token));
    }
    parsingTable.get(from).put(token, p);
  }

  private void calculateFirsts()
  {
    for (String token : nonTerminals)
    {
      firstTerminals.put(token, new HashSet<String>());
    }
    
    boolean isChanged = true;
    while (isChanged)
    {
      isChanged = false;
      for (Production p: grammar)
      {
        Set<String> firsts = firstTerminals.get(p.from);
        if (p.to.isEmpty())
        {
          firsts.add(EMPTY);
          continue;
        }
        boolean expandsToEmpty = false;
        for (String token: p.to)
        {
          // Terminals are simply added as is.
          if (isTerminal(token))
          {
            if (!firsts.contains(token) && !isNoAcceptException(p.from, token))
            {
              firsts.add(token);
              isChanged = true;
            }
            expandsToEmpty = false;
            break;
          }
          // For non-terminals, we consider them without EMPTY first
          Set<String> tokenFirsts = new HashSet<String>(firstTerminals.get(token));
          if (noAcceptTokenException.containsKey(p.from))
            tokenFirsts.removeAll(noAcceptTokenException.get(p.from));
          boolean hasEmpty = tokenFirsts.contains(EMPTY);
          if (hasEmpty)
          {
            tokenFirsts = new HashSet<String>(tokenFirsts);
            tokenFirsts.remove(EMPTY);
          }
          if (!firsts.containsAll(tokenFirsts))
          {
            firsts.addAll(tokenFirsts);
            isChanged = true;
          }
          // If we have an epsilon, then we need to gather the possible firsts from the
          // next token as well. Otherwise, move on to the next rule.
          if (!hasEmpty) 
          {
            expandsToEmpty = false;
            break;
          }
        }
        if (expandsToEmpty)
        {
          // We went through all the tokens, and it's possible for the non-terminal to
          // expand to nothing.
          // TODO: I'm not 100% sure what to do here.
          if (!firsts.contains(EMPTY))
          {
            isChanged = true;
            firsts.add(EMPTY);
          }
        }
      }
    }
  }

  private void calculateFollows()
  {
    for (String token : nonTerminals)
    {
      followsTerminals.put(token, new HashSet<String>());
    }
    
    boolean isChanged = true;
    while (isChanged)
    {
      isChanged = false;
      for (Production p: grammar)
      {
        for (int n = 0; n < p.to.size() - 1; n++)
        {
          if (isTerminal(p.to.get(n))) continue;
          Set<String> follows = followsTerminals.get(p.to.get(n));
          for (int i = n+1; i < p.to.size(); i++)
          {
            String next = p.to.get(i);
            if (isTerminal(next))
            {
              if (!follows.contains(next)) isChanged = true;
              follows.add(next);
              break;
            }
            // Non-terminal
            boolean hasEmpty = false;
            for (String token: firstTerminals.get(next))
            {
              if (token.equals(EMPTY)) 
              {
                hasEmpty = true;
                // TODO: This is different from Wikipedia. Are you sure?
              } 
              else 
              {
                if (!follows.contains(token)) isChanged = true;
                follows.add(token);
              }
            }
            if (!hasEmpty)
              break;
            // Everything after the non-terminal can expand into EMPTY
            if (i == p.to.size() - 1)
            {
              if (!follows.containsAll(followsTerminals.get(p.from)))
              {
                isChanged = true;
                follows.addAll(followsTerminals.get(p.from));
              }
            }
            // Move on to the next token since its first symbols might
            // apply to this one too.
          }
          
        }
        // If the last token is a non-terminal, it simply takes the following
        // of the parent.
        if (!p.to.isEmpty() && isNonTerminal(p.to.get(p.to.size()-1))) 
        {
          Set<String> follows = followsTerminals.get(p.to.get(p.to.size()-1));
          if (!follows.containsAll(followsTerminals.get(p.from)))
          {
            isChanged = true;
            follows.addAll(followsTerminals.get(p.from));
          }
        }
      }
    }
  }
  
  private void calculateNonTerminals()
  {
    for (Production p: grammar) 
    {
      nonTerminals.add(p.from);
    }
  }
  
  private void expandOptionalTokens()
  {
    List<Production> toDelete = new ArrayList<Production>();
    List<Production> toAdd = new ArrayList<Production>();
    List<Production> rules = grammar;
    while (rules.size() > 0)
    {
      for (Production p: rules)
      {
        for (int n = 0; n < p.to.size(); n++)
        {
          String token = p.to.get(n);
          // We have an optional token.
          if (token.endsWith("?"))
          {
            toDelete.add(p);
            Production with = new Production(p.from, new ArrayList<String>(p.to));
            Production without = new Production(p.from, new ArrayList<String>(p.to));
            with.to.set(n, token.substring(0, token.length() - 1));
            without.to.remove(n);
            toAdd.add(with);
            toAdd.add(without);
            break;
          }
        }
      }
      grammar.addAll(toAdd);
      rules = new ArrayList<Production>();
      rules.addAll(toAdd);
      toAdd.clear();
    }
    grammar.removeAll(toDelete);
  }
  
  public static void main(String [] args) throws IOException
  {
    new Main().go();
  }
}
