package tech.illuin.pipeline.generic.pipeline;

import tech.illuin.pipeline.step.result.Result;

/**
 * @author Pierre Lecerf (pierre.lecerf@illuin.tech)
 */
public class TestResult extends Result
{
    private final String type;
    private final String status;

    public TestResult(String status)
    {
        this(null, status);
    }
    public TestResult(String type, String status)
    {
        this.type = type == null ? this.getClass().getName() : type;
        this.status = status;
    }

    public String status()
    {
        return status;
    }

    @Override
    public String type()
    {
        return this.type;
    }
}
