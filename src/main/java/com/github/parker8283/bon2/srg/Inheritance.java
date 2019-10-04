package com.github.parker8283.bon2.srg;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.github.parker8283.bon2.data.IProgressListener;

@SuppressWarnings("unused")
public class Inheritance {

    private Map<String, IClass> classes = new HashMap<>();

    public int addTree(ClassCollection collection, boolean merge, int base, IProgressListener progress) {
        for (ClassNode node : collection.getClasses()) {
            progress.setProgress(++base);
            IClass cls = new IClass(node);
            IClass existing = classes.putIfAbsent(cls.name, cls);
            if (existing != null && merge)
                existing.merge(cls);
        }
        return base;
    }

    public boolean bake(IProgressListener progress) {
        progress.start(classes.size(), "Baking inheritance");
        int idx = 1;
        boolean success = true;
        for (IClass cls : classes.values()) {
            progress.setProgress(idx++);
            success = success && bake(cls);
        }

        return success;
    }

    public String findMethodOwner(String owner, String name, String desc) {
        IClass cls = classes.get(owner);
        if (cls == null)
            return owner;

        IMethod mtd = cls.methods.get(name + desc);
        if (mtd == null)
            return owner;

        return mtd.root == null ? owner : mtd.root.parent.name;
    }

    public String findFieldOwner(String owner, String name, String desc) {
        IClass cls = classes.get(owner);
        if (cls == null)
            return owner;

        IField fld = cls.fields.get(name);
        if (fld == null)
            return owner;

        return fld.root == null ? owner : fld.root.parent.name;
    }

    private boolean bake(IClass cls) {
        if (cls == null || cls.resolved)
            return true;

        if (!"java/lang/Object".equals(cls.name) && cls.parent != null)
            if (bake(classes.get(cls.parent)))
                return false;

        for (String intf : cls.interfaces)
            if (!bake(classes.get(intf)))
                return false;

        for (IMethod mtd : cls.methods.values()) {
            if ("<init>".equals(mtd.name) || "<cinit>".equals(mtd.name) ||
                ((mtd.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0)) {
                mtd.root = mtd;
                continue;
            }

            IMethod override = null;
            Queue<IClass> que = new ArrayDeque<>();
            Set<String> processed = new HashSet<>();
            Consumer<String> addQueue = name -> {
                if (!processed.contains(name)) {
                    IClass ci = classes.get(name);
                    if (ci != null)
                        que.add(ci);
                    processed.add(name);
                }
            };

            if (cls.parent != null)
                addQueue.accept(cls.parent);
            cls.interfaces.forEach(addQueue::accept);

            while (!que.isEmpty()) {
                IClass c = que.poll();
                if (cls.parent != null)
                    addQueue.accept(cls.parent);
                cls.interfaces.forEach(addQueue::accept);

                IMethod m = c.methods.get(mtd.name + mtd.desc);
                if (m == null || (m.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) != 0)
                    continue;
                override = m;
            }

            mtd.root = override == null ? mtd : override;
        }

        cls.resolved = true;

        return true;
    }

    private static class IClass {
        private final String name;
        private final int access;
        private final String parent;
        private final Set<String> interfaces = new HashSet<>();
        private final Map<String, IField> fields = new HashMap<>();
        private final Map<String, IMethod> methods = new HashMap<>();
        private boolean resolved = false;

        IClass(ClassNode cls) {
            this.name = cls.name;
            this.access = cls.access;
            this.parent = cls.superName;
            if (cls.interfaces != null)
                this.interfaces.addAll(cls.interfaces);

            if (cls.fields != null) {
                for (FieldNode fld : cls.fields)
                    fields.put(fld.name, new IField(this, fld));
            }

            if (cls.methods != null) {
                for (MethodNode mtd : cls.methods)
                    methods.put(mtd.name + mtd.desc, new IMethod(this, mtd));
            }
        }

        void merge(IClass other) {
            this.interfaces.addAll(other.interfaces);
            other.fields.forEach((k,v) -> this.fields.putIfAbsent(k, v));
            other.methods.forEach((k,v) -> this.methods.putIfAbsent(k, v));
        }
    }

    private static class IField {
        private final IClass parent;
        private final String name;
        private final int access;
        private final String desc;
        private IField root;

        IField(IClass parent, FieldNode field) {
            this.parent = parent;
            this.name = field.name;
            this.access = field.access;
            this.desc = field.desc;
        }
    }

    private static class IMethod {
        private final IClass parent;
        private final String name;
        private final int access;
        private final String desc;
        private IMethod root;

        IMethod(IClass parent, MethodNode method) {
            this.parent = parent;
            this.name = method.name;
            this.access = method.access;
            this.desc = method.desc;
        }
    }
}
