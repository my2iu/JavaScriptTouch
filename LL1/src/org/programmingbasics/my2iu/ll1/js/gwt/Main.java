package org.programmingbasics.my2iu.ll1.js.gwt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
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
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
    LL1Generator ll1 = GrammarReader.readGrammarFromString(ResourceLoader.INSTANCE.jsGrammar().getText());
    ll1.generateParser();
    parser = ll1.createParser();
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
    Document doc = Browser.getDocument();
    Element choicesPanel = doc.getElementById("choices");
    Element programPanel = doc.getElementById("program");
    choicesPanel.setInnerHTML("");
    
    if (parser.parsingStack.size() > 0)
    {
      parser.automatchTerminals();
      programPanel.setTextContent(parser.program);
      
      List<String> options = parser.findValidOptions();
      
      for (final String choice: options)
      {
        SpanElement span = doc.createSpanElement();
        span.appendChild(doc.createTextNode(choice));
        AnchorElement anchor = doc.createAnchorElement();
        anchor.setHref("#");
        anchor.appendChild(span);
        anchor.addEventListener("click", new EventListener() {
          @Override
          public void handleEvent(Event evt)
          {
            evt.preventDefault();
            evt.stopPropagation();
            parser.fullParseToken(choice);
            updateDisplayAndShowOptions();
          }}, false);
        
        choicesPanel.appendChild(anchor);
        choicesPanel.appendChild(doc.createTextNode(" "));
      }
//      System.out.println(program);
//      
//      // Show options and get next token
//      String token = chooseOptions(in, parsingStack);
//      if (token == null)
//      {
//        // For debugging purposes, we allow people to enter a bad option,
//        // then we return null, and replay this parsing step.
//        continue;
//      }
      
//      parser.fullParseToken(token);
    }
    
  }
}