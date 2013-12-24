package org.programmingbasics.my2iu.ll1.js.gwt;

import javax.annotation.Nullable;

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

  private static int TAB_SIZE = 16;
  
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
    insertToken(token, null);
  }
  
  public void insertToken(String token, @Nullable String content)
  {
    if (isStartOfLine)
    {
      currentLineDiv = doc.createDivElement();
      currentLineDiv.getStyle().setPaddingLeft(indentation * TAB_SIZE, Unit.PX);
      programDiv.appendChild(currentLineDiv);
    } 
    else
    {
      currentLineDiv.appendChild(doc.createTextNode(" "));
    }
    
    // Special formatting for certain things
    if (token.length() > 2 && token.startsWith("\"") && token.endsWith("\""))
    {
      Element el = doc.createElement("b");
      el.appendChild(doc.createTextNode(token.substring(1, token.length() - 1)));
      currentLineDiv.appendChild(el);
    }
    else
    {
      if (content != null)
      {
        switch (token)
        {
          case "Identifier":
            currentLineDiv.appendChild(doc.createTextNode("$" + content));
            break;
          case "StringLiteral":
            currentLineDiv.appendChild(doc.createTextNode("\"" + content + "\""));
            break;
          default:
            currentLineDiv.appendChild(doc.createTextNode(content));
            break;
        }
        
      }
      else
      {
        currentLineDiv.appendChild(doc.createTextNode(token));
      }
    }
    isStartOfLine = false;
  }

}
