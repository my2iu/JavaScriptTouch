package org.programmingbasics.my2iu.ll1.js.gwt.client;

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
    Browser.getWindow().alert("Hello");
//    Audio.initialize();
//    Screen.switchTo(new TitleScreen());
//    Screen.switchTo(new MainGameScreen(0));
  }
}