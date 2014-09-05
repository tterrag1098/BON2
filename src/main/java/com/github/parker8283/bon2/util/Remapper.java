package com.github.parker8283.bon2.util;

import com.github.parker8283.bon2.srg.ClassCollection;
import com.github.parker8283.bon2.srg.Mapping;
import com.github.parker8283.bon2.srg.Repo;
import org.objectweb.asm.tree.*;

public class Remapper {

    public static ClassCollection remap(ClassCollection cc) {
        for (ClassNode classNode : cc.getClasses()) {
            for (MethodNode method : classNode.methods) {
                if (hasRemap(method.name)) {
                    Mapping mapping = getRemap(method.name);
                    method.name = mapping.getMcpName();
                }
                if (method.instructions != null && method.instructions.size() > 0) {
                    for (AbstractInsnNode node : method.instructions.toArray()) {
                        if (node instanceof FieldInsnNode) {
                            FieldInsnNode field = (FieldInsnNode) node;
                            if (hasRemap(field.name)) {
                                Mapping mapping = getRemap(field.name);
                                field.name = mapping.getMcpName();
                            }
                        } else if (node instanceof MethodInsnNode) {
                            MethodInsnNode methodInsn = (MethodInsnNode) node;
                            if (hasRemap(methodInsn.name)) {
                                Mapping mapping = getRemap(methodInsn.name);
                                methodInsn.name = mapping.getMcpName();
                            }
                        }
                    }
                }
            }
            for (FieldNode field : classNode.fields) {
                if (hasRemap(field.name)) {
                    Mapping mapping = getRemap(field.name);
                    field.name = mapping.getMcpName();
                }
            }
        }
        return cc;
    }

    private static boolean hasRemap(String key) {
        return Repo.repo.containsKey(key);
    }

    private static Mapping getRemap(String key) {
        return Repo.repo.get(key);
    }
}
