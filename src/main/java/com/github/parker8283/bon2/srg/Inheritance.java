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
            success = bake(cls) && success;
        }

        return success;
    }

    public String findMethodOwner(String owner, String name, String desc) {
        IClass cls = classes.get(owner);
        if (cls == null)
            return owner;

        IMethod mtd = cls.methods.get(name + desc);
        if (mtd == null) {
            IMethod parent = findMethod(cls, name, desc);
            if (parent != null) {
                mtd = new IMethod(cls, parent.access, parent.name, parent.desc, parent);
                cls.methods.put(name + desc, mtd);
                return parent.parent.name;
            }
            return owner;
        }

        return mtd.root == null ? owner : mtd.root.parent.name;
    }

    private IMethod findMethod(IClass owner, String name, String desc) {
        if (owner == null)
            return null;

        IMethod mtd = owner.methods.get(name + desc);
        if (mtd != null)
            return mtd;

        for (String intf : owner.interfaces) {
            mtd = findMethod(classes.get(intf), name, desc);
            if (mtd != null && (mtd.access & Opcodes.ACC_PRIVATE) == 0)
                return mtd;
        }

        return findMethod(classes.get(owner.parent), name, desc);
    }

    public String findFieldOwner(String owner, String name, String desc) {
        IClass cls = classes.get(owner);
        if (cls == null)
            return owner;

        IField fld = cls.fields.get(name);
        if (fld == null) {
            IField parent = findField(cls, name, desc);
            if (parent != null) {
                fld = new IField(cls, parent.access, parent.name, parent.desc, parent);
                cls.fields.put(name, fld);
                return parent.parent.name;
            }
            return owner;
        }

        return fld.root == null ? owner : fld.root.parent.name;
    }

    private IField findField(IClass owner, String name, String desc) {
        if (owner == null)
            return null;

        IField fld = owner.fields.get(name);
        if (fld != null)
            return fld;

        for (String intf : owner.interfaces) {
            fld = findField(classes.get(intf), name, desc);

            if (fld != null && (fld.access & Opcodes.ACC_PRIVATE) == 0)
                return fld;
        }

        return findField(classes.get(owner.parent), name, desc);
    }

    private boolean bake(IClass cls) {
        if (cls == null || cls.resolved)
            return true;

        if (!"java/lang/Object".equals(cls.name) && cls.parent != null)
            if (!bake(classes.get(cls.parent)))
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
                if (c.parent != null)
                    addQueue.accept(c.parent);
                c.interfaces.forEach(addQueue::accept);

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
        private final String string;
        private final Set<String> interfaces = new HashSet<>();
        private final Map<String, IField> fields = new HashMap<>();
        private final Map<String, IMethod> methods = new HashMap<>();
        private boolean resolved = false;

        IClass(ClassNode cls) {
            this.name = cls.name;
            this.access = cls.access;
            this.parent = cls.superName;
            this.string = toAccessString(this.access) + " " +
                   ((this.access & Opcodes.ACC_INTERFACE) != 0 ? "interface " : "class ") +
                   ((this.access & Opcodes.ACC_ANNOTATION) != 0 ? "@" : "") +
                   this.name;

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

        @Override
        public String toString() {
            return string;
        }
    }

    private static class IField {
        private final IClass parent;
        private final String name;
        private final int access;
        private final String desc;
        private final String string;
        private IField root;

        IField(IClass parent, FieldNode field) {
            this.parent = parent;
            this.name = field.name;
            this.access = field.access;
            this.desc = field.desc;
            this.string = toAccessString(this.access) + " " + this.parent.name + "." + this.name;
        }

        IField(IClass parent, int access, String name, String desc, IField root) {
            this.parent = parent;
            this.name = name;
            this.access = access;
            this.desc = desc;
            this.root = root;
            this.string = toAccessString(this.access) + " " + this.parent.name + "." + this.name;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private static class IMethod {
        private final IClass parent;
        private final String name;
        private final int access;
        private final String desc;
        private final String string;
        private IMethod root;

        IMethod(IClass parent, MethodNode method) {
            this.parent = parent;
            this.name = method.name;
            this.access = method.access;
            this.desc = method.desc;
            this.string = toAccessString(this.access) + " " + this.parent.name + "." + this.name + this.desc;
        }

        IMethod(IClass parent, int access, String name, String desc, IMethod root) {
            this.parent = parent;
            this.name = name;
            this.access = access;
            this.desc = desc;
            this.root = root;
            this.string = toAccessString(this.access) + " " + this.parent.name + "." + this.name + this.desc;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private static String toAccessString(int access) {
        String ret = "";
        if ((access & Opcodes.ACC_PUBLIC   ) != 0) ret += "public ";
        if ((access & Opcodes.ACC_PROTECTED) != 0) ret += "protected ";
        if ((access & Opcodes.ACC_PRIVATE  ) != 0) ret += "private ";
        if ((access & Opcodes.ACC_FINAL    ) != 0) ret += "final ";
        if ((access & Opcodes.ACC_STATIC   ) != 0) ret += "static ";
        return ret.trim();
    }
}
