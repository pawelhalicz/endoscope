package org.endoscope.storage;

import org.endoscope.core.Stat;

import java.util.ArrayList;
import java.util.List;

public class StatDetails {
    private Stat merged;
    private List<StatHistory> histogram = new ArrayList<>();

    public StatDetails() {
        merged = new Stat();
    }

    public StatDetails(Stat merged) {
        this.merged = merged;
    }

    public Stat getMerged() {
        return merged;
    }

    public void setMerged(Stat merged) {
        this.merged = merged;
    }

    public List<StatHistory> getHistogram() {
        return histogram;
    }

    public void setHistogram(List<StatHistory> histogram) {
        this.histogram = histogram;
    }
}
