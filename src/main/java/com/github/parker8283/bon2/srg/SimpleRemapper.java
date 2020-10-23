package com.github.parker8283.bon2.srg;

import java.util.Map;

import org.objectweb.asm.commons.Remapper;

public class SimpleRemapper extends Remapper {
    private final Map<String, String> renames;
    public SimpleRemapper(Map<String, String> renames) {
        this.renames = renames;
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String descriptor) {
        return mcp(name);
    }

    @Override //We don't remap these because they are not mapped by PG.. I think...
    public String mapInvokeDynamicMethodName(final String name, final String descriptor) {
      return name;
    }

    @Override
    public String mapFieldName(final String owner, final String name, final String descriptor) {
      return mcp(name);
    }

    @Override //We don't remap, as we don't have this data
    public String mapPackageName(final String name) {
      return name;
    }

    @Override //We don't remap, as we don't have this data
    public String mapModuleName(final String name) {
        return name;
    }

    @Override //We don't remap, as we don't have this data
    public String map(final String internalName) {
        return internalName;
    }

    private String mcp(String name) {
        return renames.getOrDefault(name, name);
    }
}
