// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.IndexedItemCollection;
import com.android.tools.r8.dex.MixedSectionCollection;
import com.android.tools.r8.graph.DexValue.DexValueAnnotation;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueMethod;
import com.android.tools.r8.graph.DexValue.DexValueNull;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.graph.DexValue.DexValueType;
import java.util.ArrayList;
import java.util.List;

public class DexAnnotation extends DexItem {
  // Dex system annotations.
  // See https://source.android.com/devices/tech/dalvik/dex-format.html#system-annotation
  private static final String ANNOTATION_DEFAULT_DESCRIPTOR =
      "Ldalvik/annotation/AnnotationDefault;";
  private static final String ENCLOSING_CLASS_DESCRIPTOR = "Ldalvik/annotation/EnclosingClass;";
  private static final String ENCLOSING_METHOD_DESCRIPTOR = "Ldalvik/annotation/EnclosingMethod;";
  private static final String INNER_CLASS_DESCRIPTOR = "Ldalvik/annotation/InnerClass;";
  private static final String MEMBER_CLASSES_DESCRIPTOR = "Ldalvik/annotation/MemberClasses;";
  private static final String METHOD_PARAMETERS_DESCRIPTOR = "Ldalvik/annotation/MethodParameters;";
  private static final String SIGNATURE_DESCRIPTOR = "Ldalvik/annotation/Signature;";
  private static final String SOURCE_DEBUG_EXTENSION = "Ldalvik/annotation/SourceDebugExtension;";
  private static final String THROWS_DESCRIPTOR = "Ldalvik/annotation/Throws;";

  public static final int VISIBILITY_BUILD = 0x00;
  public static final int VISIBILITY_RUNTIME = 0x01;
  public static final int VISIBILITY_SYSTEM = 0x02;
  public final int visibility;
  public final DexEncodedAnnotation annotation;

  public DexAnnotation(int visibility, DexEncodedAnnotation annotation) {
    this.visibility = visibility;
    this.annotation = annotation;
  }

  @Override
  public int hashCode() {
    return visibility + annotation.hashCode() * 3;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof DexAnnotation) {
      DexAnnotation o = (DexAnnotation) other;
      return (visibility == o.visibility) && annotation.equals(o.annotation);
    }
    return false;
  }

  @Override
  public String toString() {
    return visibility + " " + annotation;
  }

  @Override
  public void collectIndexedItems(IndexedItemCollection indexedItems) {
    annotation.collectIndexedItems(indexedItems);
  }

  @Override
  void collectMixedSectionItems(MixedSectionCollection mixedItems) {
    mixedItems.add(this);
  }

  public static DexAnnotation createEnclosingClassAnnotation(DexType enclosingClass,
      DexItemFactory factory) {
    return createSystemValueAnnotation(ENCLOSING_CLASS_DESCRIPTOR, factory,
        new DexValueType(enclosingClass));
  }

  public static DexAnnotation createEnclosingMethodAnnotation(DexMethod enclosingMethod,
      DexItemFactory factory) {
    return createSystemValueAnnotation(ENCLOSING_METHOD_DESCRIPTOR, factory,
        new DexValueMethod(enclosingMethod));
  }

  public static boolean isEnclosingClassAnnotation(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(ENCLOSING_CLASS_DESCRIPTOR);
  }

  public static boolean isEnclosingMethodAnnotation(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(ENCLOSING_METHOD_DESCRIPTOR);
  }

  public static boolean isEnclosingAnnotation(DexAnnotation annotation) {
    return isEnclosingClassAnnotation(annotation) || isEnclosingMethodAnnotation(annotation);
  }

  public static boolean isInnerClassesAnnotation(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(MEMBER_CLASSES_DESCRIPTOR)
        || annotation.annotation.type.toDescriptorString().equals(INNER_CLASS_DESCRIPTOR);
  }

  public static DexAnnotation createInnerClassAnnotation(String clazz, int access,
      DexItemFactory factory) {
    return new DexAnnotation(VISIBILITY_SYSTEM,
        new DexEncodedAnnotation(factory.createType(INNER_CLASS_DESCRIPTOR),
            new DexAnnotationElement[]{
                new DexAnnotationElement(
                    factory.createString("accessFlags"),
                    DexValueInt.create(access)),
                new DexAnnotationElement(
                    factory.createString("name"),
                    (clazz == null)
                        ? DexValueNull.NULL
                        : new DexValueString(factory.createString(clazz)))
            }));
  }

  public static DexAnnotation createMemberClassesAnnotation(List<DexType> classes,
      DexItemFactory factory) {
    DexValue[] values = new DexValue[classes.size()];
    for (int i = 0; i < classes.size(); i++) {
      values[i] = new DexValueType(classes.get(i));
    }
    return createSystemValueAnnotation(MEMBER_CLASSES_DESCRIPTOR, factory,
        new DexValueArray(values));
  }

  public static DexAnnotation createSourceDebugExtensionAnnotation(DexValue value,
      DexItemFactory factory) {
    return new DexAnnotation(VISIBILITY_SYSTEM,
        new DexEncodedAnnotation(factory.createType(SOURCE_DEBUG_EXTENSION),
            new DexAnnotationElement[] {
              new DexAnnotationElement(factory.createString("value"), value)
            }));
  }

  public static DexAnnotation createMethodParametersAnnotation(DexValue[] names,
      DexValue[] accessFlags, DexItemFactory factory) {
    assert names.length == accessFlags.length;
    return new DexAnnotation(VISIBILITY_SYSTEM,
        new DexEncodedAnnotation(factory.createType(METHOD_PARAMETERS_DESCRIPTOR),
            new DexAnnotationElement[]{
                new DexAnnotationElement(
                    factory.createString("names"),
                    new DexValueArray(names)),
                new DexAnnotationElement(
                    factory.createString("accessFlags"),
                    new DexValueArray(accessFlags))
            }));
  }

  public static DexAnnotation createAnnotationDefaultAnnotation(DexType type,
      List<DexAnnotationElement> defaults, DexItemFactory factory) {
    return createSystemValueAnnotation(ANNOTATION_DEFAULT_DESCRIPTOR, factory,
        new DexValueAnnotation(
            new DexEncodedAnnotation(type,
                defaults.toArray(new DexAnnotationElement[defaults.size()])))
    );
  }

  public static DexAnnotation createSignatureAnnotation(String signature, DexItemFactory factory) {
    return createSystemValueAnnotation(SIGNATURE_DESCRIPTOR, factory,
        compressSignature(signature, factory));
  }

  public static DexAnnotation createThrowsAnnotation(DexValue[] exceptions,
      DexItemFactory factory) {
    return createSystemValueAnnotation(THROWS_DESCRIPTOR, factory, new DexValueArray(exceptions));
  }

  private static DexAnnotation createSystemValueAnnotation(String desc, DexItemFactory factory,
      DexValue value) {
    return new DexAnnotation(VISIBILITY_SYSTEM,
        new DexEncodedAnnotation(factory.createType(desc), new DexAnnotationElement[] {
            new DexAnnotationElement(factory.createString("value"), value)
        }));
  }

  public static boolean isThrowingAnnotation(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(THROWS_DESCRIPTOR);
  }

  public static boolean isSignatureAnnotation(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(SIGNATURE_DESCRIPTOR);
  }


  public static boolean isSourceDebugExtension(DexAnnotation annotation) {
    return annotation.annotation.type.toDescriptorString().equals(SOURCE_DEBUG_EXTENSION);
  }

  /**
   * As a simple heuristic for compressing a signature by splitting on fully qualified class names
   * and make them individual part. All other parts of the signature are simply grouped and separate
   * the names.
   * For examples, "()Ljava/lang/List<Lfoo/bar/Baz;>;" splits into:
   * <pre>
   *   ["()", "Ljava/lang/List<", "Lfoo/bar/Baz;", ">;"]
   * </pre>
   */
  private static DexValue compressSignature(String signature, DexItemFactory factory) {
    final int length = signature.length();
    List<DexValue> parts = new ArrayList<>();

    for (int at = 0; at < length; /*at*/) {
      char c = signature.charAt(at);
      int endAt = at + 1;
      if (c == 'L') {
        // Scan to ';' or '<' and consume them.
        while (endAt < length) {
          c = signature.charAt(endAt);
          if (c == ';' || c == '<') {
            endAt++;
            break;
          }
          endAt++;
        }
      } else {
        // Scan to 'L' without consuming it.
        while (endAt < length) {
          c = signature.charAt(endAt);
          if (c == 'L') {
            break;
          }
          endAt++;
        }
      }

      parts.add(toDexValue(signature.substring(at, endAt), factory));
      at = endAt;
    }

    return new DexValueArray(parts.toArray(new DexValue[parts.size()]));
  }

  private static DexValue toDexValue(String string, DexItemFactory factory) {
    return new DexValueString(factory.createString(string));
  }
}
