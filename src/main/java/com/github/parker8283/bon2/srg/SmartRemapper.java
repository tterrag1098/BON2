package com.github.parker8283.bon2.srg;

import java.util.Map;

import org.objectweb.asm.commons.Remapper;

import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.IMappingFile.IClass;

public class SmartRemapper extends Remapper {
    private final Map<String, String> renames;
    private final Inheritance inh;
    private final IMappingFile srg;

    public SmartRemapper(Map<String, String> renames, Inheritance inh, IMappingFile srg) {
        this.renames = renames;
        this.inh = inh;
        this.srg = srg;
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String descriptor) {
        final String realOwner = this.inh.findMethodOwner(owner, name, descriptor);
        IClass cls = this.srg.getClass(realOwner);
        if (cls == null) cls = this.srg.getClass(owner);
        return mcp(cls == null ? name : cls.remapMethod(name, descriptor));
    }

    @Override
    public String mapFieldName(final String owner, final String name, final String descriptor) {
        final String realOwner = this.inh.findFieldOwner(owner, name, descriptor);
        IClass cls = this.srg.getClass(realOwner);
        if (cls == null) cls = this.srg.getClass(owner);
        return mcp(cls == null ? name : cls.remapField(name));
    }

    @Override
    public String map(final String name) {
        return this.srg.remapClass(name);
    }

    @Override //We don't remap these because they are not mapped by PG.. I think...
    public String mapInvokeDynamicMethodName(final String name, final String descriptor) {
      return name;
    }

    @Override //We don't remap, as we don't have this data, Note, this is NOT class package names, this is module package name
    public String mapPackageName(final String name) {
      return name;
    }

    @Override //We don't remap, as we don't have this data
    public String mapModuleName(final String name) {
        return name;
    }

    private String mcp(String name) {
        return renames.getOrDefault(name, name);
    }
}
