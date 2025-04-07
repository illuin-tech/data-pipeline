package tech.illuin.pipeline.resilience4j.execution.wrapper.config.timelimiter;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public final class TimeLimiterWrapperConfig
{
    private final TimeLimiterStepHandler stepHandler;
    private final TimeLimiterSinkHandler sinkHandler;

    private TimeLimiterWrapperConfig(TimeLimiterStepHandler stepHandler, TimeLimiterSinkHandler sinkHandler)
    {
        this.stepHandler = stepHandler;
        this.sinkHandler = sinkHandler;
    }

    public TimeLimiterStepHandler stepHandler()
    {
        return this.stepHandler;
    }

    public TimeLimiterSinkHandler sinkHandler()
    {
        return this.sinkHandler;
    }

    public static final TimeLimiterWrapperConfig NOOP_WRAPPER_CONFIG = Builder.builder().build();

    public static class Builder
    {
        private TimeLimiterStepHandler stepHandler;
        private TimeLimiterSinkHandler sinkHandler;

        public static Builder builder()
        {
            return new Builder();
        }

        public TimeLimiterWrapperConfig build()
        {
            return new TimeLimiterWrapperConfig(
                this.stepHandler == null ? TimeLimiterStepHandler.NOOP : this.stepHandler,
                this.sinkHandler == null ? TimeLimiterSinkHandler.NOOP : this.sinkHandler
            );
        }

        public TimeLimiterStepHandler stepHandler()
        {
            return this.stepHandler;
        }

        public Builder setStepHandler(TimeLimiterStepHandler stepHandler)
        {
            this.stepHandler = stepHandler;
            return this;
        }

        public TimeLimiterSinkHandler sinkHandler()
        {
            return this.sinkHandler;
        }

        public Builder setSinkHandler(TimeLimiterSinkHandler sinkHandler)
        {
            this.sinkHandler = sinkHandler;
            return this;
        }
    }
}
