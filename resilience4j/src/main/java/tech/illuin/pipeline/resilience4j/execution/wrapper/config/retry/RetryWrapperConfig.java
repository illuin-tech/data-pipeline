package tech.illuin.pipeline.resilience4j.execution.wrapper.config.retry;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public final class RetryWrapperConfig
{
    private final RetryStepHandler stepHandler;
    private final RetrySinkHandler sinkHandler;

    private RetryWrapperConfig(RetryStepHandler stepHandler, RetrySinkHandler sinkHandler)
    {
        this.stepHandler = stepHandler;
        this.sinkHandler = sinkHandler;
    }

    public RetryStepHandler stepHandler()
    {
        return stepHandler;
    }

    public RetrySinkHandler sinkHandler()
    {
        return sinkHandler;
    }

    public static final RetryWrapperConfig NOOP_WRAPPER_CONFIG = Builder.builder().build();

    public static class Builder
    {
        private RetryStepHandler stepHandler;
        private RetrySinkHandler sinkHandler;

        public static Builder builder()
        {
            return new Builder();
        }

        public RetryWrapperConfig build()
        {
            return new RetryWrapperConfig(
                this.stepHandler == null ? RetryStepHandler.NOOP : this.stepHandler,
                this.sinkHandler == null ? RetrySinkHandler.NOOP : this.sinkHandler
            );
        }

        public RetryStepHandler stepHandler()
        {
            return stepHandler;
        }

        public Builder setStepHandler(RetryStepHandler stepHandler)
        {
            this.stepHandler = stepHandler;
            return this;
        }

        public RetrySinkHandler sinkHandler()
        {
            return sinkHandler;
        }

        public Builder setSinkHandler(RetrySinkHandler sinkHandler)
        {
            this.sinkHandler = sinkHandler;
            return this;
        }
    }
}
