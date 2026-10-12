package co.in.urjameterapi.urjameterapi.dto;

import java.math.BigDecimal;

public class Transformer {

    private String code;
    private String name;
    private String feederCode;
    private BigDecimal capacityKva;

    public Transformer() {
    }

    public Transformer(String code, String name, String feederCode, BigDecimal capacityKva) {
        this.code = code;
        this.name = name;
        this.feederCode = feederCode;
        this.capacityKva = capacityKva;
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

    public String getFeederCode() {
        return feederCode;
    }

    public void setFeederCode(String feederCode) {
        this.feederCode = feederCode;
    }

    public BigDecimal getCapacityKva() {
        return capacityKva;
    }

    public void setCapacityKva(BigDecimal capacityKva) {
        this.capacityKva = capacityKva;
    }
}
