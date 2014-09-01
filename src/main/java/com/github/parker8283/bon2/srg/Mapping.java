package com.github.parker8283.bon2.srg;

public class Mapping {
    public enum Type {
        PACKAGE("PK"),
        CLASS("CL"),
        FIELD("FD"),
        METHOD("MD");

        private String code;

        private Type(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public static Type getByCode(String code) {
            for(Type type : Type.values()) {
                if(code.equals(type.getCode())) {
                    return type;
                }
            }
            return null;
        }
    }

    private Type type;
    private String mcpName;
    private String srgName;

    public Mapping(Type type, String mcpName, String srgName) {
        this.type = type;
        this.mcpName = mcpName;
        this.srgName = srgName;
    }

    public Type getType() {
        return type;
    }

    public String getMcpName() {
        return mcpName;
    }

    public String getSrgName() {
        return srgName;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        Mapping mapping = (Mapping)o;

        if(!mcpName.equals(mapping.mcpName)) {
            return false;
        }
        if(!srgName.equals(mapping.srgName)) {
            return false;
        }
        if(type != mapping.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (mcpName != null ? mcpName.hashCode() : 0);
        result = 31 * result + (srgName != null ? srgName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return type.getCode() + ": " + mcpName + " " + srgName;
    }

    @Override
    public Mapping clone() {
        return new Mapping(this.type, this.mcpName, this.srgName);
    }
}
