package com.javax0.aptools;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * An abstract annotation factory that can create XXXTool class instances
 * used to manipulate the XXX data. For example the method
 * {@link #string(String s)} will create a {@link StringTool} that can be used
 * to manage/manipulate the string passed as argument to the method
 * {@link #string(String s)}.
 * <p>
 * Even though this class is abstract there are no methods that are abstract.
 * This class is abstract because this class is not supposed to be used directly
 * only through the extension classes: {@link The}, {@link InThe},
 * {@link GetThe} ...
 *
 * @author Peter Verhas
 */
public abstract class AbstractToolFactory {
  public static ElementTool element(final Element element) {
    return new ElementTool(new Aptools(element));
  }

  /**
   * Create a {@link AnnotationTool} that can be used to manage the element.
   */
  public static AnnotationTool annotation(final AnnotationMirror annotationMirror) {
    return new AnnotationTool(new Aptools(annotationMirror));
  }

  /**
   * Create a {@link MethodTool} that can be used to manage the element.
   */
  public static MethodTool method(final ExecutableElement methodElement) {
    return new MethodTool(new Aptools(methodElement));
  }

  /**
   * Create a new {@link StringTool} to manage the string.
   * 
   * @param s
   *          the string to be managed
   * @return the new {@link StringTool}
   */
  public static StringTool string(final String s) {
    return new StringTool(new Aptools(s));
  }
}
