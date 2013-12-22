package org.programmingbasics.my2iu.ll1.js.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.resources.client.ClientBundle.Source;

public interface ResourceLoader extends ClientBundle
{
  public static final ResourceLoader INSTANCE = GWT.create(ResourceLoader.class);
  
  @Source("../javascript.txt")
  TextResource jsGrammar();
}
