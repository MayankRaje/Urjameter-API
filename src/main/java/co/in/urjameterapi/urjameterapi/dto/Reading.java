package co.in.urjameterapi.urjameterapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reading {

    private LocalDateTime timestamp;
    private BigDecimal kwh;
    private BigDecimal kvah;
    private BigDecimal voltageR;

    public Reading() {
    }

    public Reading(LocalDateTime timestamp, BigDecimal kwh, BigDecimal kvah, BigDecimal voltageR) {
        this.timestamp = timestamp;
        this.kwh = kwh;
        this.kvah = kvah;
        this.voltageR = voltageR;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getKwh() {
        return kwh;
    }

    public void setKwh(BigDecimal kwh) {
        this.kwh = kwh;
    }

    public BigDecimal getKvah() {
        return kvah;
    }

    public void setKvah(BigDecimal kvah) {
        this.kvah = kvah;
    }

    public BigDecimal getVoltageR() {
        return voltageR;
    }

    public void setVoltageR(BigDecimal voltageR) {
        this.voltageR = voltageR;
    }
}
