package com.example.timesheet.framework.web.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Data
public class PageRequest {

    private Integer page;

    private Integer size;

    private ArrayNode filter;

    private String[] sort;

    private String graphql;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public ArrayNode getFilter() {
        return filter;
    }

    public void setFilter(ArrayNode filter) {
        this.filter = filter;
    }

    public String[] getSort() {
        return sort;
    }

    public void setSort(String[] sort) {
        this.sort = sort;
    }

    public String getGraphql() {
        return graphql;
    }

    public void setGraphql(String graphql) {
        this.graphql = graphql;
    }
}
