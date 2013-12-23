package org.programmingbasics.my2iu.ll1.js.gwt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.programmingbasics.my2iu.ll1.generator.GrammarReader;
import org.programmingbasics.my2iu.ll1.generator.LL1Generator;
import org.programmingbasics.my2iu.ll1.generator.LLParser;

import com.google.gwt.core.client.EntryPoint;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.AnchorElement;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;
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
  
  public void updateDisplayAndShowOptions()
  {
    Element choicesPanel = doc.getElementById("choices");
    Element programPanel = doc.getElementById("program");
    choicesPanel.setInnerHTML("");
    
    if (parser.parsingStack.size() > 0)
    {
      parser.automatchTerminals();
      programPanel.setInnerHTML("");
      programPanel.appendChild(programFormatter.programDiv);
      
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
            return Integer.compare(ChoiceOrdering.TERMINAL_ORDER.get(o1), 
                ChoiceOrdering.TERMINAL_ORDER.get(o2));
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
    if (buttonText.length() > 2 && buttonText.startsWith("\"") && buttonText.endsWith("\""))
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
  
  public void handleInput(String token)
  {
    parser.fullParseToken(token);
    updateDisplayAndShowOptions();
  }
}