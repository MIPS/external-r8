// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.code;

import com.android.tools.r8.code.InvokeSuperRange;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.ir.conversion.DexBuilder;
import com.android.tools.r8.ir.optimize.Inliner.InlineAction;
import com.android.tools.r8.ir.optimize.InliningOracle;
import java.util.List;

public class InvokeSuper extends InvokeMethod {

  public InvokeSuper(DexMethod target, Value result, List<Value> arguments) {
    super(target, result, arguments);
  }

  @Override
  public Type getType() {
    return Type.SUPER;
  }

  @Override
  protected String getTypeString() {
    return "Super";
  }

  @Override
  public DexEncodedMethod computeSingleTarget(AppInfoWithSubtyping appInfo) {
    return appInfo.lookupDirectTarget(getInvokedMethod());
  }

  @Override
  public void buildDex(DexBuilder builder) {
    com.android.tools.r8.code.Instruction instruction;
    int argumentRegisters = requiredArgumentRegisters();
    builder.requestOutgoingRegisters(argumentRegisters);
    if (needsRangedInvoke(builder)) {
      assert argumentsConsecutive(builder);
      int firstRegister = argumentRegisterValue(0, builder);
      instruction = new InvokeSuperRange(firstRegister, argumentRegisters, getInvokedMethod());
    } else {
      int[] individualArgumentRegisters = new int[5];
      int argumentRegistersCount = fillArgumentRegisters(builder, individualArgumentRegisters);
      instruction = new com.android.tools.r8.code.InvokeSuper(
          argumentRegistersCount,
          getInvokedMethod(),
          individualArgumentRegisters[0],  // C
          individualArgumentRegisters[1],  // D
          individualArgumentRegisters[2],  // E
          individualArgumentRegisters[3],  // F
          individualArgumentRegisters[4]); // G
    }
    addInvokeAndMoveResult(instruction, builder);
  }

  public boolean identicalNonValueParts(Instruction other) {
    if (!other.isInvokeSuper()) {
      return false;
    }
    return super.identicalNonValueParts(other);
  }

  @Override
  public boolean isInvokeSuper() {
    return true;
  }

  @Override
  public InvokeSuper asInvokeSuper() {
    return this;
  }

  @Override
  public InlineAction computeInlining(InliningOracle decider) {
    return decider.computeForInvokeSuper(this);
  }
}
