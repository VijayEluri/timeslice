package com.enokinomi.timeslice.web.core.client.util;

public class NotAuthenticException extends ServiceException
{
    private static final long serialVersionUID = 1L;

    public NotAuthenticException()
    {
        super();
    }

    public NotAuthenticException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotAuthenticException(String message)
    {
        super(message);
    }

    public NotAuthenticException(Throwable cause)
    {
        super(cause);
    }

}
