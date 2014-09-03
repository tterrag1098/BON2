package com.github.parker8283.bon2.util;

import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Mapping;
import com.github.parker8283.bon2.srg.Repo;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Remapper {

    public static ClassCollection remap(ClassCollection cc) {
        for (ClassNode classNode : cc.getClasses()) {
            for (MethodNode method : classNode.methods) {
                remapMethod(method);
                if (method.instructions != null) {
                    for (AbstractInsnNode node = method.instructions.getFirst(); node != null; node.getNext()) {
                        if (node instanceof FieldInsnNode) {
                            remapFieldInsn((FieldInsnNode) node);
                        } //TODO
                    }
                }
            }
        }
    }

    private static void remapMethod(MethodNode method) {
        if (Repo.repo.containsKey(method.name)) {
            Mapping mapping = Repo.repo.get(method.name);
            method.name = mapping.getMcpName().substring(0, mapping.getMcpName().indexOf(' '));
        }
    }

    private static void remapFieldInsn(FieldInsnNode field) {
        if (Repo.repo.containsKey(field.name)) {
            Mapping mapping = Repo.repo.get(field.name);
            field.name = mapping.getMcpName();
        }
    }
}
