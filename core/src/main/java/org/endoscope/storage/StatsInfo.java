package org.endoscope.storage;

import java.beans.Transient;
import java.util.Date;

public class StatsInfo {
    private String name;
    private Date fromDate;
    private Date toDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatsInfo)) return false;

        StatsInfo statsInfo = (StatsInfo) o;

        if (name != null ? !name.equals(statsInfo.name) : statsInfo.name != null) return false;
        if (fromDate != null ? !fromDate.equals(statsInfo.fromDate) : statsInfo.fromDate != null) return false;
        return toDate != null ? toDate.equals(statsInfo.toDate) : statsInfo.toDate == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
        return result;
    }

    @Transient
    public boolean inRange(Date from, Date to){
        return  from != null && to != null
                && getFromDate() != null && getToDate() != null
                && from.getTime() <= getFromDate().getTime()
                && to.getTime() >= getFromDate().getTime();
    }
}
