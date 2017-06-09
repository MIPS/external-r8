// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.debug;

import org.apache.harmony.jpda.tests.framework.jdwp.Value;
import org.junit.Test;

/**
 * Tests local variable information.
 */
public class LocalsTest extends DebugTestBase {

  public static final String SOURCE_FILE = "Locals.java";

  @Test
  public void testNoLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "noLocals";
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 8),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 9),
        checkNoLocal(),
        run());
  }

  @Test
  public void testUnusedLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "unusedLocals";
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 12),
        checkNoLocal(),
        stepOver(),
        checkLine(SOURCE_FILE, 13),
        checkLocal("i", Value.createInt(Integer.MAX_VALUE)),
        run());
  }

  @Test
  public void testConstantLocal() throws Throwable {
    final String className = "Locals";
    final String methodName = "constantLocals";
    Value pValue = Value.createInt(10);
    Value cValue = Value.createInt(5);
    Value vValue = Value.createInt(pValue.getIntValue() + cValue.getIntValue());

    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 17),
        checkLocal("p", pValue),
        stepOver(),
        checkLine(SOURCE_FILE, 18),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        stepOver(),
        checkLine(SOURCE_FILE, 19),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        checkLocal("v", vValue),
        run());
  }

  @Test
  public void testConstantLocalWithUpdate() throws Throwable {
    final String className = "Locals";
    final String methodName = "constantLocals";
    Value pValue = Value.createInt(10);
    Value cValue = Value.createInt(5);
    Value newValue = Value.createInt(5);
    Value vValue = Value.createInt(pValue.getIntValue() + newValue.getIntValue());

    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 17),
        checkLocal("p", pValue),
        stepOver(),
        checkLine(SOURCE_FILE, 18),
        checkLocal("p", pValue),
        checkLocal("c", cValue),
        setLocal("c", newValue),
        checkLocal("c", newValue),  // we should see the updated value
        stepOver(),
        checkLine(SOURCE_FILE, 19),
        checkLocal("p", pValue),
        checkLocal("c", newValue),
        checkLocal("v", vValue),
        run());
  }

  @Test
  public void testZeroLocals() throws Throwable {
    final String className = "Locals";
    final String methodName = "zeroLocals";
    final Value newValueForI = Value.createInt(10);
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 23),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 24),
        checkLocal("i", Value.createInt(0)),
        setLocal("i", newValueForI),
        stepOver(),
        checkLine(SOURCE_FILE, 25),
        checkLocal("i", newValueForI),
        checkLocal("f", Value.createFloat(0)),
        run());
  }

  @Test
  public void testNoFlowOptimization() throws Throwable {
    final String className = "Locals";
    final String methodName = "noFlowOptimization";
    final Value oldValueForI = Value.createInt(0);
    final Value newValueForI = Value.createInt(10);
    runDebugTest(className,
        breakpoint(className, methodName),
        run(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 29),
        checkNoLocal(),
        stepOver(),
        checkMethod(className, methodName),
        checkLine(SOURCE_FILE, 30),
        checkLocal("i", oldValueForI),
        setLocal("i", newValueForI),
        stepOver(),
        checkLine(SOURCE_FILE, 33),
        checkLocal("i", newValueForI),
        run());
  }

}
