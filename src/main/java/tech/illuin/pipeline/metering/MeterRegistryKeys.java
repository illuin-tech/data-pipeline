package tech.illuin.pipeline.metering;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class MeterRegistryKeys
{
    /* Pipeline Metrics */
    public static final String PIPELINE_RUN_KEY = "pipeline.run";
    public static final String PIPELINE_RUN_SUCCESS_KEY = "pipeline.run.success";
    public static final String PIPELINE_RUN_FAILURE_KEY = "pipeline.run.failure";
    /* Initialization Metrics */
    public static final String PIPELINE_INITIALIZATION_RUN_KEY = "pipeline.initialization.run";
    public static final String PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY = "pipeline.initialization.run.success";
    public static final String PIPELINE_INITIALIZATION_RUN_FAILURE_KEY = "pipeline.initialization.run.failure";
    /* Step Metrics */
    public static final String PIPELINE_STEP_RUN_KEY = "pipeline.step.run";
    public static final String PIPELINE_STEP_RUN_SUCCESS_KEY = "pipeline.step.run.success";
    public static final String PIPELINE_STEP_RUN_FAILURE_KEY = "pipeline.step.run.failure";
    /* Sink Metrics */
    public static final String PIPELINE_SINK_RUN_KEY = "pipeline.sink.run";
    public static final String PIPELINE_SINK_RUN_SUCCESS_KEY = "pipeline.sink.run.success";
    public static final String PIPELINE_SINK_RUN_FAILURE_KEY = "pipeline.sink.run.success";
}
