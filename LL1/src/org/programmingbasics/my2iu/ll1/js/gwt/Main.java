package org.programmingbasics.my2iu.ll1.js.gwt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.programmingbasics.my2iu.ll1.generator.GrammarReader;
import org.programmingbasics.my2iu.ll1.generator.LL1Generator;
import org.programmingbasics.my2iu.ll1.generator.LLParser;
import org.programmingbasics.my2iu.ll1.generator.Production;

import com.google.gwt.core.client.EntryPoint;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.AnchorElement;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;
import elemental.html.FormElement;
import elemental.html.InputElement;
import elemental.html.SpanElement;
import elemental.html.Window;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint
{
  LLParser parser;
  Document doc;
  PrettyHtmlFormatter programFormatter = new PrettyHtmlFormatter();
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
    doc = Browser.getDocument();
    LL1Generator ll1 = GrammarReader.readGrammarFromString(ResourceLoader.INSTANCE.jsGrammar().getText());
    ll1.generateParser();
    parser = ll1.createParser(programFormatter);
//      parser.runInteractiveBuilder();
    Element programPanel = doc.getElementById("program");
    programPanel.appendChild(programFormatter.programDiv);

    
    setupParser();
    
//    Browser.getWindow().alert("Hello");
//    Audio.initialize();
//    Screen.switchTo(new TitleScreen());
//    Screen.switchTo(new MainGameScreen(0));
  }
  
  public void setupParser()
  {
    parser.parsingStack.add("Program");
    updateDisplayAndShowOptions();
  }

  /**
   * 
   * @param token
   * @return true if terminal successfully inserted and parsing may proceed
   *   false if awaiting extra input before insertion of terminal can be completed 
   */
  boolean doTerminalInsertion(final String token)
  {
    switch (token)
    {
      case "Identifier":
      case "NumericLiteral":
      case "StringLiteral":
      case "IdentifierName":
      case "LabelledStatement":
      case "Label":
        askForTextInput(token);
        return false;
      default:
        programFormatter.insertToken(token);
        return true;
    }
  }
  
  void askForTextInput(final String token)
  {
    Element choicesPanel = doc.getElementById("choices");
    choicesPanel.setInnerHTML("");
    
    final InputElement textField = doc.createInputElement();
    textField.setValue("");
//    if (token.equals("NumericLiteral"))
//      textField.setType("number");
    FormElement form = doc.createFormElement();
    form.setClassName("textinput");
    form.appendChild(textField);
    form.addEventListener("submit", new EventListener() {
      @Override public void handleEvent(Event evt)
      {
        evt.preventDefault();
        evt.stopPropagation();
        String text = textField.getValue();
        programFormatter.insertToken(token, text);
        updateDisplayAndShowOptions();
      }}, false);
    choicesPanel.appendChild(form);
    textField.focus();
  }
  
  public void updateDisplayAndShowOptions()
  {
    Element choicesPanel = doc.getElementById("choices");
    choicesPanel.setInnerHTML("");
    
    if (parser.parsingStack.size() > 0)
    {
      String terminalAtTop = parser.nextRealTerminal();
      while (terminalAtTop != null)
      {
        assert(!Production.isPrettyPrintToken(terminalAtTop)); 
        // Otherwise, remove any terminals and insert it into the stream.
        if (!doTerminalInsertion(terminalAtTop))
        {
          // Insertion of this terminal is blocking further parsing
          // because it is asking for additional input.
          return;
        }
        
        terminalAtTop = parser.nextRealTerminal();
      }
      
      // IE11 erased all the elements inside the program panel if you
      // set innerHTML = '', even if you have pointers to the contained
      // objects.
//      programPanel.setInnerHTML("");
//      programPanel.appendChild(programFormatter.programDiv);
      
      List<String> options = parser.findValidOptions();
      
      if (options.size() == 1)
      {
        handleInput(options.get(0));
      }
      else 
      {
        Collections.sort(options, new Comparator<String>() {
          @Override
          public int compare(String o1, String o2)
          {
            int choice1 = ChoiceOrdering.TERMINAL_ORDER.containsKey(o1)
                ? ChoiceOrdering.TERMINAL_ORDER.get(o1) : Integer.MAX_VALUE;
            int choice2 = ChoiceOrdering.TERMINAL_ORDER.containsKey(o2)
                ? ChoiceOrdering.TERMINAL_ORDER.get(o2) : Integer.MAX_VALUE;
            return Integer.compare(choice1, choice2);
          }});
        for (final String choice: options)
        {
          choicesPanel.appendChild(createChoiceButton(choice));
          choicesPanel.appendChild(doc.createTextNode(" "));
        }
      }
    }
  }
  
  public Element createChoiceButton(final String choice)
  {
    Element div = doc.createDivElement();
    String buttonText = choice;
    if (renamedTokens.containsKey(buttonText))
    {
      buttonText = renamedTokens.get(buttonText);
    }
    else if (buttonText.length() > 2 && buttonText.startsWith("\"") && buttonText.endsWith("\""))
    {
      buttonText = buttonText.substring(1, buttonText.length() - 1);
    }
    div.appendChild(doc.createTextNode(buttonText));
    AnchorElement anchor = (AnchorElement)doc.createElement("A");
    anchor.setHref("#");
    anchor.appendChild(div);
    anchor.setClassName("choicebutton");
    anchor.addEventListener("click", new EventListener() {
      @Override public void handleEvent(Event evt)
      {
        evt.preventDefault();
        evt.stopPropagation();
        handleInput(choice);
      }}, false);
    return anchor;
  }
  
  public void handleInput(final String token)
  {
    parser.fullParseToken(token);
    updateDisplayAndShowOptions();
  }
  
  static Map<String, String> renamedTokens = new HashMap<>();
  static {
    renamedTokens.put("Identifier", "$...");
    renamedTokens.put("IdentifierName", "Property");
    renamedTokens.put("StringLiteral", "\"...\"");
    renamedTokens.put("NumericLiteral", "0.0");
    renamedTokens.put("RegularExpressionLiteral", "/.../");
    renamedTokens.put("LabelledStatement", "label:");
  }
}