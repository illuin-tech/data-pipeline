package tech.illuin.pipeline.metering.marker;

import java.util.Map;
import java.util.stream.Stream;

public abstract class MDCMarker
{
    private final MDCManager mdc;

    protected MDCMarker(MDCManager mdc)
    {
        this.mdc = mdc;
    }

    public abstract Map<String, String> compileMarkers();

    public final void setMDC()
    {
        this.compileMarkers().forEach(this.mdc::put);
    }

    public final void setMDC(Exception exception)
    {
        this.mdc.put("error", exception.getClass().getName());
    }

    public final void unsetMDC()
    {
        Stream.concat(
            this.compileMarkers().keySet().stream(),
            Stream.of("error")
        ).forEach(this.mdc::remove);
    }
}
