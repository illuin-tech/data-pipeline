package tech.illuin.pipeline.metering.mdc;

import org.slf4j.MDC;

public final class DefaultMDCManager implements MDCManager
{
    @Override
    public void put(String key, String val) throws IllegalArgumentException
    {
        MDC.put(key, val);
    }

    @Override
    public void remove(String key) throws IllegalArgumentException
    {
        MDC.remove(key);
    }
}
