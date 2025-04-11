package tech.illuin.pipeline.resilience4j.execution.wrapper.config.circuitbreaker;

/**
 * @author Theo Malka (theo.malka@illuin.tech)
 */
public final class CircuitBreakerWrapperConfig
{
    private final CircuitBreakerStepHandler stepHandler;
    private final CircuitBreakerSinkHandler sinkHandler;

    private CircuitBreakerWrapperConfig(CircuitBreakerStepHandler stepHandler, CircuitBreakerSinkHandler sinkHandler)
    {
        this.stepHandler = stepHandler;
        this.sinkHandler = sinkHandler;
    }

    public CircuitBreakerStepHandler stepHandler()
    {
        return this.stepHandler;
    }

    public CircuitBreakerSinkHandler sinkHandler()
    {
        return this.sinkHandler;
    }

    public static final CircuitBreakerWrapperConfig NOOP_WRAPPER_CONFIG = Builder.builder().build();

    public static class Builder
    {
        private CircuitBreakerStepHandler stepHandler;
        private CircuitBreakerSinkHandler sinkHandler;

        public static Builder builder()
        {
            return new Builder();
        }

        public CircuitBreakerWrapperConfig build()
        {
            return new CircuitBreakerWrapperConfig(
                this.stepHandler == null ? CircuitBreakerStepHandler.NOOP : this.stepHandler,
                this.sinkHandler == null ? CircuitBreakerSinkHandler.NOOP : this.sinkHandler
            );
        }

        public CircuitBreakerStepHandler stepHandler()
        {
            return stepHandler;
        }

        public Builder setStepHandler(CircuitBreakerStepHandler stepHandler)
        {
            this.stepHandler = stepHandler;
            return this;
        }

        public CircuitBreakerSinkHandler sinkHandler()
        {
            return sinkHandler;
        }

        public Builder setSinkHandler(CircuitBreakerSinkHandler sinkHandler)
        {
            this.sinkHandler = sinkHandler;
            return this;
        }
    }
}
