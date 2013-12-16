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
        if (tokens.length == 1 && tokens[0].equals("EPSILON"))
        {
          // Special way to denote expansion to empty string
          grammar.add(new Production(fromNonTerminal, new String[0]));
          continue;
        }
        grammar.add(new Production(fromNonTerminal, tokens));
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

  private void runInteractiveBuilder()
  {
    List<String> parsingStack = new ArrayList<String>();
    parsingStack.add("Program");
    
    // Show options
    showOptions(parsingStack.get(parsingStack.size()-1));
  }

  private void showOptions(String topOfStack)
  {
    int n = 0;
    for (String option: parsingTable.get(topOfStack).keySet())
    {
      System.out.println(n + " " + option);
      n++;
    }
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
    System.out.println(firstTerminals);
    System.out.println(followsTerminals);
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
            if (!firsts.contains(token))
            {
              firsts.add(token);
              isChanged = true;
            }
            expandsToEmpty = false;
            break;
          }
          // For non-terminals, we consider them without EMPTY first
          Set<String> tokenFirsts = firstTerminals.get(token);
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
