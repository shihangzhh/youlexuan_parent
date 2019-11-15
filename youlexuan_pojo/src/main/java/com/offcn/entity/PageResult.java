package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    //总记录数
    public Long total;
    //当前页结果集合
    public List rows;

    //快速生成get、set  alrt+insert


    public PageResult() {
    }

    public PageResult(Long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
