package org.programmingbasics.my2iu.ll1.js.gwt;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.programmingbasics.my2iu.ll1.generator.GrammarReader;
import org.programmingbasics.my2iu.ll1.generator.LL1Generator;
import org.programmingbasics.my2iu.ll1.generator.LLParser;

import com.google.gwt.core.client.EntryPoint;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.CanvasElement;
import elemental.html.CanvasRenderingContext2D;
import elemental.html.Window;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Main implements EntryPoint
{
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
    LL1Generator ll1 = GrammarReader.readGrammarFromString(ResourceLoader.INSTANCE.jsGrammar().getText());
    ll1.generateParser();
    LLParser parser = ll1.createParser();
//      parser.runInteractiveBuilder();

    Browser.getWindow().alert("Hello");
//    Audio.initialize();
//    Screen.switchTo(new TitleScreen());
//    Screen.switchTo(new MainGameScreen(0));
  }
}