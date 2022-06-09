package com.glp.asmplugin;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class OnCreateVisitor extends MethodVisitor {

    public OnCreateVisitor(MethodVisitor methodVisitor) {
        super(Opcodes.ASM5, methodVisitor);
    }
    //开始访问方法
    @Override
    public void visitCode() {
        super.visitCode();

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitVarInsn(Opcodes.LSTORE, 1);
    }

    @Override
    public void visitInsn(int opcode) {
        //判断内部操作指令
        //当前指令是RETURN，表示方法内部的代码已经执行完
        if (opcode == Opcodes.RETURN) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(Opcodes.LLOAD, 1);
            mv.visitInsn(Opcodes.LSUB);
            mv.visitVarInsn(Opcodes.LSTORE, 3);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(20, l3);
            mv.visitLdcInsn("TAG");
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("interval:");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(Opcodes.LLOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            mv.visitInsn(Opcodes.POP);
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        //访问结束
    }
}