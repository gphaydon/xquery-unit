/*
 * Copyright (c) 2007, Mark Logic Corporation. All Rights Reserved.
 */

/**
 *
 */
package com.marklogic.ps.test;

/**
 * This exception is thrown when an error occurs attempting to work with an xquery REST service.
 * @author Will LaForest
 * @since Mar 12, 2007
 */
public class XQueryXmlHttpException extends RuntimeException
{
    private int httpStatusCode;

    public XQueryXmlHttpException(int httpStatusCode)
    {
        this.httpStatusCode = httpStatusCode;
    }

    public XQueryXmlHttpException(int httpStatusCode, String message)
    {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public XQueryXmlHttpException(int httpStatusCode, String message, Throwable cause)
    {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public XQueryXmlHttpException(int httpStatusCode, Throwable cause)
    {
        super(cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode()
    {
        return httpStatusCode;
    }
}
