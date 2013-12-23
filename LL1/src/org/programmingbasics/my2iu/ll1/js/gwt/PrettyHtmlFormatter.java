package org.programmingbasics.my2iu.ll1.js.gwt;

import org.programmingbasics.my2iu.ll1.generator.PrettyFormatter;
import org.programmingbasics.my2iu.ll1.generator.Production;

import elemental.client.Browser;
import elemental.css.CSSStyleDeclaration.Unit;
import elemental.dom.Document;
import elemental.dom.Element;

public class PrettyHtmlFormatter implements PrettyFormatter
{
  private int indentation = 0;
  private boolean isStartOfLine = true;
  Element programDiv;
  Element currentLineDiv;
  Document doc;
  
  public PrettyHtmlFormatter()
  {
    doc = Browser.getDocument();
    programDiv = doc.createDivElement();
  }
  
  /* (non-Javadoc)
   * @see org.programmingbasics.my2iu.ll1.generator.PrettyFormatter#handlePrettyPrint(java.lang.String)
   */
  @Override
  public void handlePrettyPrint(String topOfStack)
  {
    // Handle any pretty print instructions.        
    if (topOfStack.equals(Production.ENDL))
    {
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

  /* (non-Javadoc)
   * @see org.programmingbasics.my2iu.ll1.generator.PrettyFormatter#insertToken(java.lang.String)
   */
  @Override
  public void insertToken(String token)
  {
    if (isStartOfLine)
    {
      currentLineDiv = doc.createDivElement();
      currentLineDiv.getStyle().setPaddingLeft(indentation * 16, Unit.PX);
      programDiv.appendChild(currentLineDiv);
    }
    currentLineDiv.appendChild(doc.createTextNode(" " + token));
    isStartOfLine = false;
  }
}
