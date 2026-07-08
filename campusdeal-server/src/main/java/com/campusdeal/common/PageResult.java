package com.campusdeal.common;

import java.util.List;

public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResult() {}

    public PageResult(List<T> list, long total, int page, int size, int totalPages) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResult<>(list, total, page, size, totalPages);
    }
}
