package com.javax0.aptools;

import com.javax0.fluflu.Fluentize;
import com.javax0.fluflu.Transition;

import javax.lang.model.element.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Fluentize()
class Aptools {
    private static final String JAVA_LANG_PREFIX = "java.lang.";
    private Element element;
    private ExecutableElement methodElement;
    private AnnotationMirror annotationMirror;
    private String s;
    private String[] argumentNames = null;

    Aptools(final String s) {
        this.s = s;
    }

    Aptools(final ExecutableElement methodElement) {
        this.methodElement = methodElement;
    }

    Aptools(final Element element) {
        this.element = element;
    }

    public Aptools(final AnnotationMirror annotationMirror) {
        this.annotationMirror = annotationMirror;
    }

    /**
     * If the string starts with {@code java.lang.} then remove that prefix. If
     * the start of the string is different then the argument builder will not
     * be modified.
     *
     * @param declaration the string in the builder that will be modified
     */
    private static void removeJavaLang(final StringBuilder declaration) {
        if (declaration.indexOf(JAVA_LANG_PREFIX) == 0) {
            declaration.delete(0, JAVA_LANG_PREFIX.length());
        }
    }

    /**
     * Convert the declaration from array declaration to variable argument
     * declaration. The last two characters of the declaration, which is
     * assumed to be '[' and ']' are removed and ' ... ' is appended in the
     * place.
     * <p>
     * For example is the declaration contains {@code "Object[]"} then it will be
     * converted to {@code "Object ... "}.
     *
     * @param declaration the builder containing the type declaration
     */
    private static void convertArrayToVararg(final StringBuilder declaration) {
        declaration.delete(declaration.length() - 2, declaration.length()).append(" ... ");
    }

    @Transition(from = "MethodTool", end = true)
    public String getJavadoc() {
        final var processingEnv = Environment.get();
        var javaDoc = processingEnv.getElementUtils().getDocComment(methodElement);
        if (javaDoc == null) {
            javaDoc = "";
        }
        return javaDoc;
    }

    /**
     * Get the number of the parameters of the method.
     */
    @Transition(from = "MethodTool", end = true)
    public int getTheNumberOfParameters() {
        return methodElement.getParameters().size();
    }

    /**
     * Get an array of lists containing the annotations of the arguments.
     */
    @Transition(from = "MethodTool", end = true)
    public List<? extends AnnotationMirror>[] getParameterAnnotations() {
        final var annotationMirrorss = new List[getTheNumberOfParameters()];
        int i = 0;
        for (final VariableElement parameterElement : methodElement.getParameters()) {
            annotationMirrorss[i++] = parameterElement.getAnnotationMirrors();
        }
        return annotationMirrorss;
    }

    /**
     * Get an argument name that can be used as the {@code i}-th argument.
     *
     * @param i zero based index of the argument
     * @return the name of the argument
     */
    @Transition(from = "MethodTool", end = true)
    public String getArgumentName(final int i) {
        return getArgumentNames()[i];
    }

    private String[] getArgumentNames() {
        if (argumentNames == null) {
            argumentNames = new String[getTheNumberOfParameters()];
            int j = 0;
            for (final VariableElement parameterElement : methodElement.getParameters()) {
                argumentNames[j++] = parameterElement.getSimpleName().toString();
            }
        }
        return argumentNames;
    }

    /**
     * Return a string that can be used in a Java code as the argument list for
     * the method.
     *
     * @return the parameter list as string
     */
    @Transition(from = "MethodTool", end = true)
    public String createParamList() {
        return String.join(", ", getArgumentNames());
    }

    @Transition(from = "MethodTool", end = true)
    public String getReturnType() {
        return methodElement.getReturnType().toString();
    }

    @Transition(from = "MethodTool", end = true)
    public String getName() {
        return methodElement.getSimpleName().toString();
    }

    private boolean thisIsTheLastArgument(final int i) {
        return i == getTheNumberOfParameters() - 1;
    }

    /**
     * Create and return the string that can be used in the generated Java source
     * code as argument list (argument types and argument names separated by
     * commas).
     */
    @Transition(from = "MethodTool", end = true)
    public String createArgList() {
        final var arglist = new StringBuilder();
        var sep = "";
        int i = 0;
        for (final var parameterElement : methodElement.getParameters()) {
            final var declaration = new StringBuilder(parameterElement.asType().toString());
            removeJavaLang(declaration);
            if (methodElement.isVarArgs() && thisIsTheLastArgument(i)) {
                convertArrayToVararg(declaration);
            }
            arglist.append(sep).append(declaration).append(" ").append(getArgumentName(i));
            sep = ", ";
            i++;
        }
        return arglist.toString();
    }

    /**
     * Get the name of the enclosing class or package.
     *
     * @param kind should be {@link ElementKind#PACKAGE} or {@link ElementKind#CLASS}.
     * @return the qualified name of the enclosing package or simple name of the class
     */
    private String getKindName(final ElementKind kind) {
        var e = element;
        while (e != null && e.getKind() != kind) {
            e = e.getEnclosingElement();
        }
        if (e == null) {
            throw new IllegalArgumentException("Cannot find the kind '"
                    + kind.toString()
                    + "' for this element: "
                    + element.toString());
        }
        if (kind.equals(ElementKind.PACKAGE)) {
            return ((PackageElement) e).getQualifiedName().toString();
        } else {
            return e.getSimpleName().toString();
        }
    }

    /**
     * Get the list of the methods from the element.
     *
     * @return list of the method elements in form of ExecutableElements
     */
    @Transition(from = "ElementTool", end = true)
    public List<ExecutableElement> getMethods() {
        final List<ExecutableElement> methodElements = new LinkedList<>();
        for (final Element methodElement : element.getEnclosedElements()) {
            if (methodElement.getKind().equals(ElementKind.METHOD)) {
                methodElements.add((ExecutableElement) methodElement);
            }
        }
        return methodElements;
    }

    /**
     * Get the name of the class that this element represents.
     */
    @Transition(from = "ElementTool", end = true)
    public String getClassName() {
        return getKindName(ElementKind.CLASS);
    }

    /**
     * True if this element is abstract.
     */
    @Transition(from = "ElementTool", end = true, name = "isAbstract")
    public boolean elementIsAbstract() {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

    /**
     * True if the method is abstract.
     */
    @Transition(from = "MethodTool", end = true, name = "isAbstract")
    public boolean methodIsAbstract() {
        return methodElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    /**
     * Get the name of the package that this element (class) is in.
     */
    @Transition(from = "ElementTool", end = true)
    public String getPackageName() {
        return getKindName(ElementKind.PACKAGE);
    }

    /**
     * Get the named annotation from this element.
     *
     * @param annotationFullyQualifiedName the name of the annotation containing the package and the class
     *                                     name of the annotation interface.
     */
    @Transition(from = "ElementTool", end = true)
    public AnnotationMirror getTheAnnotation(final String annotationFullyQualifiedName) {
        for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationFullyQualifiedName)) {
                return annotationMirror;
            }
        }
        return null;
    }

    /**
     * True if the element has the annotation of the given name.
     *
     * @param annotationFullyQualifiedName the name of the annotation containing the package and the class
     *                                     name of the annotation interface.
     */
    @Transition(from = "ElementTool", end = true)
    public boolean hasAnnotation(final String annotationFullyQualifiedName) {
        return getTheAnnotation(annotationFullyQualifiedName) != null;
    }

    private AnnotationValue getAnnotationValue(final String name) {
        final Map<? extends ExecutableElement, ? extends AnnotationValue> map = annotationMirror.getElementValues();
        for (final ExecutableElement annotationElement : map.keySet()) {
            if (annotationElement.getSimpleName().toString().equals(name)) {
                return map.get(annotationElement);
            }
        }
        return null;
    }

    /**
     * Get the value of the annotation as string. This is the default string, when
     * an annotation does not have many parameters, only one. If there are more
     * than one parameters than this is the one named `value`.
     *
     * @return the string specified as value.
     */
    @Transition(from = "AnnotationTool", end = true)
    public String getStringValue() {
        return getStringValue("value");
    }

    @Transition(from = "AnnotationTool", end = true)
    public String getStringValue(final String name) {
        final AnnotationValue av = getAnnotationValue(name);
        final String value;
        if (av == null) {
            value = null;
        } else {
            value = GetThe.string(av.toString()).unquoted();
        }
        return value;
    }

    /**
     * Removes the quotes from the string, thus {@code "apple"} becomes
     * {@code apple}, or writing the same in Java notation {@code "\"apple\""}
     * becomes {@code "apple"}.
     * <p>
     * Note that this method simply chops off the first and the last characters.
     * Thus {@code apple} will become {@code ppl}. If there are less than two
     * characters in the string then RuntimeException will happen.
     *
     * @return the string without the quotes.
     */
    @Transition(from = "StringTool", end = true)
    public String unquoted() {
        return s.substring(1, s.length() - 1);
    }

    /**
     * Replace the placeholders with the actual strings.
     * <p>
     * This method is to be used to replace placeholders in templates with the
     * actual values. Placeholders are strings in the template that start and end
     * with the {@code #} character and there is a name between the two {@code #}
     * characters. For example {@code #packageName#} is a typical placeholder that
     * is presumably replaced by the actual name of a package.
     * <p>
     * The odd arguments of the method are interpreted as placeholder names, the
     * even arguments, each following a placeholder name are the actual values.
     * The placeholder names <b>should not</b> contain the {@code #} characters.
     * They will be taken care of this method. Only the names of the placeholders
     * are to be specified.
     * <p>
     * If there are odd number of argument strings then RuntimeException will be
     * thrown.
     *
     * @param arg to search and replace
     * @return the string replaced substrings
     */
    @Transition(from = "StringTool", end = true)
    public String replace(final String... arg) {
        if (arg.length % 2 != 0)
            throw new RuntimeException("replace was called with odd number of strings");
        String result = s;
        for (int i = 0; i < arg.length; i += 2) {
            result = result.replaceAll("#" + arg[i] + "#", arg[i + 1]);
        }
        return result;
    }

    /**
     * Calculate the prefix from the name of the package. This is the string that
     * has to be prepended in front of the name of the class to get the fully
     * qualified class name that can be used in source files.
     *
     * @return the package name usable as prefix
     */
    @Transition(from = "StringTool", end = true)
    public String makePrefix() {
        return s == null ? "" : s + ".";
    }
}
