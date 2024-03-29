package tech.illuin.pipeline.metering;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public final class MeterRegistryKeys
{
    private MeterRegistryKeys() {}

    /* Pipeline Metrics */
    public static final String PIPELINE_RUN_KEY = "pipeline.run";
    public static final String PIPELINE_RUN_TOTAL_KEY = "pipeline.run.total";
    public static final String PIPELINE_RUN_SUCCESS_KEY = "pipeline.run.success";
    public static final String PIPELINE_RUN_FAILURE_KEY = "pipeline.run.failure";
    public static final String PIPELINE_RUN_ERROR_TOTAL_KEY = "pipeline.run.error.total";
    /* Initialization Metrics */
    public static final String PIPELINE_INITIALIZATION_RUN_KEY = "pipeline.initialization.run";
    public static final String PIPELINE_INITIALIZATION_RUN_TOTAL_KEY = "pipeline.initialization.run.total";
    public static final String PIPELINE_INITIALIZATION_RUN_SUCCESS_KEY = "pipeline.initialization.run.success";
    public static final String PIPELINE_INITIALIZATION_RUN_FAILURE_KEY = "pipeline.initialization.run.failure";
    public static final String PIPELINE_INITIALIZATION_ERROR_TOTAL_KEY = "pipeline.initialization.error.total";
    /* Step Metrics */
    public static final String PIPELINE_STEP_RUN_KEY = "pipeline.step.run";
    public static final String PIPELINE_STEP_RUN_TOTAL_KEY = "pipeline.step.run.total";
    public static final String PIPELINE_STEP_RUN_SUCCESS_KEY = "pipeline.step.run.success";
    public static final String PIPELINE_STEP_RUN_FAILURE_KEY = "pipeline.step.run.failure";
    public static final String PIPELINE_STEP_RESULT_TOTAL_KEY = "pipeline.step.result.total";
    public static final String PIPELINE_STEP_ERROR_TOTAL_KEY = "pipeline.step.error.total";
    /* Sink Metrics */
    public static final String PIPELINE_SINK_RUN_KEY = "pipeline.sink.run";
    public static final String PIPELINE_SINK_RUN_TOTAL_KEY = "pipeline.sink.run.total";
    public static final String PIPELINE_SINK_RUN_SUCCESS_KEY = "pipeline.sink.run.success";
    public static final String PIPELINE_SINK_RUN_FAILURE_KEY = "pipeline.sink.run.failure";
    public static final String PIPELINE_SINK_ERROR_TOTAL_KEY = "pipeline.sink.error.total";
}
