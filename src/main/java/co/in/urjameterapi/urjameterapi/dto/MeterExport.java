package co.in.urjameterapi.urjameterapi.dto;

import java.util.List;

public class MeterExport {

    private List<MeterDetail> data;
    private int total;

    public MeterExport() {
    }

    public MeterExport(List<MeterDetail> data, int total) {
        this.data = data;
        this.total = total;
    }

    public List<MeterDetail> getData() {
        return data;
    }

    public void setData(List<MeterDetail> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
