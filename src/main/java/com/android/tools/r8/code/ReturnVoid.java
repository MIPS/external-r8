// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.code;

import com.android.tools.r8.ir.conversion.IRBuilder;

public class ReturnVoid extends Format10x {

  public static final int OPCODE = 0xe;
  public static final String NAME = "ReturnVoid";
  public static final String SMALI_NAME = "return-void";

  ReturnVoid(int high, BytecodeStream stream) {
    super(high, stream);
  }

  public ReturnVoid() {}

  public String getName() {
    return NAME;
  }

  public String getSmaliName() {
    return SMALI_NAME;
  }

  public int getOpcode() {
    return OPCODE;
  }

  @Override
  public int[] getTargets() {
    return EXIT_TARGET;
  }

  @Override
  public void buildIR(IRBuilder builder) {
    builder.addReturn();
  }
}
