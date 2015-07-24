
package com.autonavi.xm.statistics;

public interface StatisticsAgent {

    public void start();

    public void stop();

    public void markPageStart(String page);

    public void markPageEnd(String page);

    public void markEvent(String event);

    public void markEvent(String event, String type);

    public void markEventBegin(String event);

    public void markEventEnd(String event);

    public void markAttribute(String name, long value);

}
