package tech.illuin.pipeline.metering.mdc;

public interface MDCManager
{
    void put(String key, String val) throws IllegalArgumentException;

    void remove(String key) throws IllegalArgumentException;
}
