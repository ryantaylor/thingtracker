package com.itv.thingtracker.exceptions;

public class APIResultException extends Exception
{
    public APIResultException()
    {
        super();
    }

    public APIResultException( String message )
    {
        super( message );
    }

    public APIResultException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public APIResultException( Throwable cause )
    {
        super( cause );
    }
}
