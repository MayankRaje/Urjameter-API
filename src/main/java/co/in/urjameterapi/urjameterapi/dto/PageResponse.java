package co.in.urjameterapi.urjameterapi.dto;

import java.util.List;

public class PageResponse<T> {

    private List<T> data;
    private Pagination pagination;

    public PageResponse() {
    }

    public PageResponse(List<T> data, Pagination pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
