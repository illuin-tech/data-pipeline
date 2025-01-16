package tech.illuin.pipeline.metering.marker;

public interface MDCManager
{
    void put(String key, String val) throws IllegalArgumentException;

    void remove(String key) throws IllegalArgumentException;
}
