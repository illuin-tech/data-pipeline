package tech.illuin.pipeline.metrics;

import org.slf4j.spi.MDCAdapter;
import tech.illuin.pipeline.metering.mdc.MDCManager;

public final class DebugMDCManager implements MDCManager
{
    private final MDCAdapter mdcAdapter;

    public DebugMDCManager(MDCAdapter mdcAdapter)
    {
        this.mdcAdapter = mdcAdapter;
    }

    @Override
    public void put(String key, String val) throws IllegalArgumentException
    {
        this.mdcAdapter.put(key, val);
    }

    @Override
    public void remove(String key) throws IllegalArgumentException
    {
        this.mdcAdapter.remove(key);
    }
}
