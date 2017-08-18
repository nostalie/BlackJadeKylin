package org.nostalie.auto.pojo;

/**
 * Created by nostalie on 17-8-18.
 */
public class RowBounds {
    private int limit;
    private int offset;

    public RowBounds(){}

    public RowBounds(int limit,int offset){
        this.limit = limit;
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "RowBounds{" +
                "limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
