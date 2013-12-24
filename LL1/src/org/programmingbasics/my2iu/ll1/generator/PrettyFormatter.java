package org.programmingbasics.my2iu.ll1.generator;

public interface PrettyFormatter
{
  public void handlePrettyPrint(String topOfStack);

  public void insertToken(String token);
  
  public void insertToken(String token, String content);

}