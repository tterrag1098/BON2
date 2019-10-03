package com.github.parker8283.bon2.util;

import java.util.Map;

import org.objectweb.asm.tree.*;

import com.github.parker8283.bon2.data.IProgressListener;
import com.github.parker8283.bon2.srg.ClassCollection;

public class Remapper {
    public static ClassCollection remap(Map<String, String> renames, ClassCollection cc, IProgressListener progress) {
        progress.start(cc.getClasses().size(), "Remapping");
        int classesRemapped = 0;
        progress.setMax(cc.getClasses().size());
        for(ClassNode classNode : cc.getClasses()) {
            for(MethodNode method : classNode.methods) {
                if(method.instructions != null && method.instructions.size() > 0) {
                    for(AbstractInsnNode node : method.instructions.toArray()) {
                        if(node instanceof FieldInsnNode) {
                            FieldInsnNode field = (FieldInsnNode)node;
                            String mapping = renames.get(field.name);
                            if(mapping != null)
                                field.name = mapping;
                        } else if(node instanceof MethodInsnNode) {
                            MethodInsnNode methodInsn = (MethodInsnNode)node;
                            String mapping = renames.get(methodInsn.name);
                            if (mapping != null)
                                methodInsn.name = mapping;
                        }
                    }
                }

                String mapping = renames.get(method.name);
                if(mapping != null)
                    method.name = mapping;
            }
            for(FieldNode field : classNode.fields) {
                String mapping = renames.get(field.name);
                if(mapping != null)
                    field.name = mapping;
            }
            progress.setProgress(++classesRemapped);
        }
        return cc;
    }
}
