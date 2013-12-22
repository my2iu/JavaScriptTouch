package org.programmingbasics.my2iu.ll1.generator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;

import com.google.gwt.core.shared.GwtIncompatible;

public class GrammarReader
{
  String fromNonTerminal;
  LL1Generator ll1;
  
  GrammarReader(LL1Generator ll1)
  {
    this.ll1 = ll1;
  }
  
  @GwtIncompatible 
  private void readData(String filename) throws IOException
  {
    // Start reading in the grammar rules
    FileInputStream inStream = new FileInputStream(filename);
    InputStreamReader reader = new InputStreamReader(inStream, Charset.forName("UTF-8"));
    BufferedReader in = new BufferedReader(reader);
    readData(in);
    reader.close();
    inStream.close();
  }

  void readLine(String line)
  {
    // Skip comments
    if (line.startsWith("#") || line.startsWith("//"))
      return;
    // Skip blank lines
    if (line.matches("^[ \t\r\n]*$"))
      return;
     
    // If it begins with whitespace, then we have a production
    if (line.codePointAt(0) == ' ' || line.codePointAt(0) == '\t')
    {
      String [] tokens = line.trim().split("[ \t]+");
      if (tokens.length == 1 && tokens[0].equals("EPSILON"))
      {
        // Special way to denote expansion to empty string
        ll1.grammar.add(new Production(fromNonTerminal, new String[0]));
        return;
      } 
      else if (tokens[0].equals("EXCEPTIION_PEEK_NO_ACCEPT"))
      {
        String terminal = tokens[1];
        if (!ll1.noAcceptTokenException.containsKey(fromNonTerminal))
          ll1.noAcceptTokenException.put(fromNonTerminal, new HashSet<String>());
        ll1.noAcceptTokenException.get(fromNonTerminal).add(terminal);
        return;
      }
      Production p = new Production(fromNonTerminal, tokens); 
      ll1.grammar.add(p);
      return;
    }
    
    // Otherwise, we have the start of a new rule
    assert(line.trim().endsWith(":"));
    fromNonTerminal  = line.trim().split ("[ \t]+")[0];
  }
  
  @GwtIncompatible 
  void readData(BufferedReader in) throws IOException
  {
    while (true) 
    {
      String line = in.readLine();
      if (line == null) break;
      readLine(line);
    }
  }
  
  @GwtIncompatible 
  public static LL1Generator readGrammar(String filename) throws IOException
  {
    LL1Generator ll1 = new LL1Generator();
    GrammarReader reader = new GrammarReader(ll1);
    reader.readData(filename);
    return ll1;
  }
  
  public static LL1Generator readGrammarFromString(String data)
  {
    LL1Generator ll1 = new LL1Generator();
    GrammarReader reader = new GrammarReader(ll1);
    String [] lines = data.split("\n");
    for (String line: lines)
      reader.readLine(line);
    return ll1;
  }
}
