package co.in.urjameterapi.urjameterapi.dto;


public class HierarchyEntry {

    private String level;
    private String code;
    private String name;

    public HierarchyEntry() {
    }

    public HierarchyEntry(String level, String code, String name) {
        this.level = level;
        this.code = code;
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
