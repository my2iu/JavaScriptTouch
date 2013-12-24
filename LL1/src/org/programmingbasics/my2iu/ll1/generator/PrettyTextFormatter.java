package org.programmingbasics.my2iu.ll1.generator;

public class PrettyTextFormatter implements PrettyFormatter
{
  private int indentation = 0;
  private String program = "";
  private boolean isStartOfLine = true;
  
  public String getProgramText()
  {
    return program;
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
      program += "\n";
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
      for (int n = 0; n < indentation; n++)
        program += "  ";
    }
    program += " " + token;
    isStartOfLine = false;
  }
  
  public void insertToken(String token, String content)
  {
    if (isStartOfLine)
    {
      for (int n = 0; n < indentation; n++)
        program += "  ";
    }
    program += " " + (content != null ? content : token);
    isStartOfLine = false;
  }
}
