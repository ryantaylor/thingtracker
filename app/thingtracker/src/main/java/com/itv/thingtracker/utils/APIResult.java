package com.itv.thingtracker.utils;

public class APIResult<T>
{
    private final String Id;
    private T Result;

    public APIResult( String id, T result )
    {
        Id = id;
        Result = result;
    }

    public String getId()
    {
        return Id;
    }

    public T getResult()
    {
        return Result;
    }

    public void setResult( T result )
    {
        Result = result;
    }
}
