// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.ir.conversion.CallGraph;
import com.android.tools.r8.shaking.ProguardConfigurationRule;
import com.android.tools.r8.shaking.ProguardTypeMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InternalOptions {

  public final DexItemFactory itemFactory;

  public InternalOptions() {
    itemFactory = new DexItemFactory();
  }

  public InternalOptions(DexItemFactory factory) {
    assert factory != null;
    itemFactory = factory;
  }

  public final int NOT_SPECIFIED = -1;

  public boolean printTimes = false;
  // Skipping optimizations.
  public boolean skipDebugInfoOpt = false;
  public boolean skipDebugLineNumberOpt = false;
  public boolean skipClassMerging = true;

  // Number of threads to use while processing the dex files.
  public int numberOfThreads = NOT_SPECIFIED;
  // Print smali disassembly.
  public boolean useSmaliSyntax = false;
  // Verbose output.
  public boolean verbose = false;
  // Silencing output.
  public boolean quiet = false;

  public List<String> methodsFilter = ImmutableList.of();
  public int minApiLevel = Constants.DEFAULT_ANDROID_API;
  public List<String> logArgumentsFilter = ImmutableList.of();

  // Defines interface method rewriter behavior.
  public OffOrAuto interfaceMethodDesugaring = OffOrAuto.Off;
  // Defines try-with-resources rewriter behavior.
  public OffOrAuto tryWithResourcesDesugaring = OffOrAuto.Off;

  // Application writing mode.
  public OutputMode outputMode = OutputMode.Indexed;

  public boolean useTreeShaking = true;
  public boolean printUsage = false;
  public Path printUsageFile = null;

  public boolean printCfg = false;
  public String printCfgFile;
  public boolean printSeeds;
  public Path seedsFile;
  public boolean printMapping;
  public Path printMappingFile;
  public boolean printMainDexList;
  public Path printMainDexListFile;
  public boolean ignoreMissingClasses = false;
  public boolean skipMinification = false;
  public String packagePrefix = "";
  public boolean allowAccessModification = true;
  public boolean inlineAccessors = true;
  public boolean removeSwitchMaps = true;
  public boolean disableAssertions = true;
  public final OutlineOptions outline = new OutlineOptions();
  public boolean debugKeepRules = false;
  public final AttributeRemovalOptions attributeRemoval = new AttributeRemovalOptions();
  public boolean allowParameterName = false;

  public boolean debug = false;
  public final TestingOptions testing = new TestingOptions();

  // TODO(zerny): These stateful dictionaries do not belong here.
  public List<String> classObfuscationDictionary = ImmutableList.of();
  public List<String> obfuscationDictionary = ImmutableList.of();

  public ImmutableList<ProguardConfigurationRule> mainDexKeepRules = ImmutableList.of();
  public boolean minimalMainDex;
  public ImmutableList<ProguardConfigurationRule> keepRules = ImmutableList.of();
  public ImmutableSet<ProguardTypeMatcher> dontWarnPatterns = ImmutableSet.of();

  public String warningInvalidParameterAnnotations = null;

  public boolean printWarnings() {
    boolean printed = false;
    if (warningInvalidParameterAnnotations != null) {
      System.out.println("Warning: " + warningInvalidParameterAnnotations);
      printed = true;
    }
    return printed;
  }

  public boolean hasMethodsFilter() {
    return methodsFilter.size() > 0;
  }

  public boolean methodMatchesFilter(DexEncodedMethod method) {
    // Not specifying a filter matches all methods.
    if (!hasMethodsFilter()) {
      return true;
    }
    // Currently the filter is simple string equality on the qualified name.
    String qualifiedName = method.qualifiedName();
    return methodsFilter.indexOf(qualifiedName) >= 0;
  }

  public boolean methodMatchesLogArgumentsFilter(DexEncodedMethod method) {
    // Not specifying a filter matches no methods.
    if (logArgumentsFilter.size() == 0) {
      return false;
    }
    // Currently the filter is simple string equality on the qualified name.
    String qualifiedName = method.qualifiedName();
    return logArgumentsFilter.indexOf(qualifiedName) >= 0;
  }

  public static class OutlineOptions {

    public boolean enabled = true;
    public static final String className = "r8.GeneratedOutlineSupport";
    public String methodPrefix = "outline";
    public int minSize = 3;
    public int maxSize = 99;
    public int threshold = 20;
  }

  public static class TestingOptions {
    public Function<List<DexEncodedMethod>, List<DexEncodedMethod>> irOrdering;
  }

  public static class AttributeRemovalOptions {

    public static final String INNER_CLASSES = "InnerClasses";
    public static final String ENCLOSING_METHOD = "EnclosingMethod";
    public static final String SIGNATURE = "Signature";
    public static final String EXCEPTIONS = "Exceptions";
    public static final String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
    public static final String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
    public static final String RUNTIME_INVISBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
    public static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS =
        "RuntimeVisibleParameterAnnotations";
    public static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS =
        "RuntimeInvisibleParameterAnnotations";
    public static final String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
    public static final String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS =
        "RuntimeInvisibleTypeAnnotations";
    public static final String ANNOTATION_DEFAULT = "AnnotationDefault";

    public boolean innerClasses = false;
    public boolean enclosingMethod = false;
    public boolean signature = false;
    public boolean exceptions = false;
    public boolean sourceDebugExtension = false;
    public boolean runtimeVisibleAnnotations = false;
    public boolean runtimeInvisibleAnnotations = false;
    public boolean runtimeVisibleParameterAnnotations = false;
    public boolean runtimeInvisibleParamterAnnotations = false;
    public boolean runtimeVisibleTypeAnnotations = false;
    public boolean runtimeInvisibleTypeAnnotations = false;
    public boolean annotationDefault = false;

    private AttributeRemovalOptions() {

    }

    public static AttributeRemovalOptions filterOnlySignatures() {
      AttributeRemovalOptions result = new AttributeRemovalOptions();
      result.applyPattern("*");
      result.signature = false;
      return result;
    }

    /**
     * Implements ProGuards attribute matching rules.
     *
     * @see <a href="https://www.guardsquare.com/en/proguard/manual/attributes">ProGuard manual</a>.
     */
    private boolean update(boolean previous, String text, String[] patterns) {
      for (String pattern : patterns) {
        if (previous) {
          return true;
        }
        if (pattern.charAt(0) == '!') {
          if (matches(pattern, 1, text, 0)) {
            break;
          }
        } else {
          previous = matches(pattern, 0, text, 0);
        }
      }
      return previous;
    }

    private boolean matches(String pattern, int patternPos, String text, int textPos) {
      while (patternPos < pattern.length()) {
        char next = pattern.charAt(patternPos++);
        if (next == '*') {
          while (textPos < text.length()) {
            if (matches(pattern, patternPos, text, textPos++)) {
              return true;
            }
          }
          return patternPos >= pattern.length();
        } else {
          if (textPos >= text.length() || text.charAt(textPos) != next) {
            return false;
          }
          textPos++;
        }
      }
      return textPos == text.length();
    }

    public void applyPattern(String pattern) {
      String[] patterns = pattern.split(",");
      innerClasses = update(innerClasses, INNER_CLASSES, patterns);
      enclosingMethod = update(enclosingMethod, ENCLOSING_METHOD, patterns);
      signature = update(signature, SIGNATURE, patterns);
      exceptions = update(exceptions, EXCEPTIONS, patterns);
      sourceDebugExtension = update(sourceDebugExtension, SOURCE_DEBUG_EXTENSION, patterns);
      runtimeVisibleAnnotations = update(runtimeVisibleAnnotations, RUNTIME_VISIBLE_ANNOTATIONS,
          patterns);
      runtimeInvisibleAnnotations = update(runtimeInvisibleAnnotations,
          RUNTIME_INVISBLE_ANNOTATIONS, patterns);
      runtimeVisibleParameterAnnotations = update(runtimeVisibleParameterAnnotations,
          RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS, patterns);
      runtimeInvisibleParamterAnnotations = update(runtimeInvisibleParamterAnnotations,
          RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS, patterns);
      runtimeVisibleTypeAnnotations = update(runtimeVisibleTypeAnnotations,
          RUNTIME_VISIBLE_TYPE_ANNOTATIONS, patterns);
      runtimeInvisibleTypeAnnotations = update(runtimeInvisibleTypeAnnotations,
          RUNTIME_INVISIBLE_TYPE_ANNOTATIONS, patterns);
      annotationDefault = update(annotationDefault, ANNOTATION_DEFAULT, patterns);
    }

    public void ensureValid(boolean isMinifying) {
      if (innerClasses && !enclosingMethod) {
        throw new CompilationError("Attribute InnerClasses requires EnclosingMethod attribute. "
            + "Check -keepattributes directive.");
      } else if (!innerClasses && enclosingMethod) {
        throw new CompilationError("Attribute EnclosingMethod requires InnerClasses attribute. "
            + "Check -keepattributes directive.");
      } else if (signature && !innerClasses) {
        throw new CompilationError("Attribute Signature requires InnerClasses attribute. Check "
            + "-keepattributes directive.");
      } else if (signature && isMinifying) {
        // TODO(38188583): Allow this once we can minify signatures.
        throw new CompilationError("Attribute Signature cannot be kept when minifying. "
            + "Check -keepattributes directive.");
      }
    }
  }

  public boolean canUseInvokePolymorphic() {
    return minApiLevel >= Constants.ANDROID_O_API;
  }

  public boolean canUseInvokeCustom() {
    return minApiLevel >= Constants.ANDROID_O_API;
  }

  public boolean canUseDefaultAndStaticInterfaceMethods() {
    return minApiLevel >= Constants.ANDROID_N_API;
  }

  public boolean canUseLongCompareAndObjectsNonNull() {
    return minApiLevel >= Constants.ANDROID_K_API;
  }

  public boolean canUseSuppressedExceptions() {
    return minApiLevel >= Constants.ANDROID_K_API;
  }

  public boolean canUsePrivateInterfaceMethods() {
    return minApiLevel >= Constants.ANDROID_N_API;
  }

  // APIs for accessing parameter names annotations are not available before Android O, thus does
  // not emit them to avoid wasting space in Dex files because runtimes before Android O will ignore
  // them.
  public boolean canUseParameterNameAnnotations() {
    return minApiLevel >= Constants.ANDROID_O_API;
  }
}
