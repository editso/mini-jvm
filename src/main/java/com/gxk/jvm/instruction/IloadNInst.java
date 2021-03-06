package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;

public class IloadNInst implements Instruction {
  public final int n;

  public IloadNInst(int n) {
    this.n = n;
  }

  @Override
  public int offset() {
    return 2;
  }

  @Override
  public void execute(Frame frame) {
    Integer tmp = frame.getInt(n);
    frame.pushInt(tmp);
  }
}
