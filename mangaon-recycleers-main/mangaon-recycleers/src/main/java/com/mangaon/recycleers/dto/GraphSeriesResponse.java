package com.mangaon.recycleers.dto;

import com.mangaon.recycleers.model.GraphDataPoint;
import java.util.List;

public class GraphSeriesResponse {

    private String item;
    private String name;
    private String period;
    private List<GraphDataPoint> data;

    public GraphSeriesResponse() {}

    public GraphSeriesResponse(String name, String period, List<GraphDataPoint> data) {
        this.name   = name;
        this.period = period;
        this.data   = data;
        this.item   = name;
    }

    public String getItem()              { return item; }
    public void setItem(String item)     { this.item = item; }
    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }
    public String getPeriod()            { return period; }
    public void setPeriod(String period) { this.period = period; }
    public List<GraphDataPoint> getData(){ return data; }
    public void setData(List<GraphDataPoint> data) { this.data = data; }
}