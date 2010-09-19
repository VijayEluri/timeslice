package com.enokinomi.timeslice.web.gwt.client.controller;

import java.util.ArrayList;
import java.util.List;

import com.enokinomi.timeslice.web.gwt.client.beans.AppJobCompletion;
import com.enokinomi.timeslice.web.gwt.client.beans.AssignedTaskTotal;
import com.enokinomi.timeslice.web.gwt.client.beans.BrandInfo;
import com.enokinomi.timeslice.web.gwt.client.beans.StartTag;
import com.enokinomi.timeslice.web.gwt.client.beans.TaskTotal;
import com.enokinomi.timeslice.web.gwt.client.entry.AsyncResult;


/**
 * Provides listener management part of IController.
 *
 * @author dbacon
 *
 */
public abstract class BaseController implements IController
{
    private List<IControllerListener> listeners = new ArrayList<IControllerListener>();

    public void addControllerListener(IControllerListener listener)
    {
        if (listener != null)
        {
            listeners.add(listener);
        }
    }

    public void removeControllerListener(IControllerListener listener)
    {
        if (listener != null)
        {
            listeners.remove(listener);
        }
    }

    protected void fireServerInfoRecieved(String info)
    {
        for (IControllerListener listener: listeners)
        {
            listener.serverInfoRecieved(info);
        }
    }

    protected void fireAuthenticated()
    {
        for (IControllerListener listener: listeners)
        {
            listener.authenticated();
        }
    }

    protected void fireUnauthenticated(boolean retry)
    {
        for (IControllerListener listener: listeners)
        {
            listener.unauthenticated(retry);
        }
    }

    protected void fireRefreshItemsDone(AsyncResult<List<StartTag>> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onRefreshItemsDone(result);
        }
    }

    protected void fireRefreshTotalsDone(AsyncResult<List<TaskTotal>> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onRefreshTotalsDone(result);
        }
    }

    protected void fireRefreshTotalsAssignedDone(AsyncResult<List<AssignedTaskTotal>> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onRefreshTotalsAssignedDone(result);
        }
    }

    protected void firePersistTotalsDone(AsyncResult<String> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onPersistTotalsDone(result);
        }
    }

    protected void fireAddItemDone(AsyncResult<Void> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onAddItemDone(result);
        }
    }

    protected void fireAssignBilleeDone(AsyncResult<Void> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onAssignBilleeDone(result);
        }
    }

    protected void fireListAvailableJobsDone(AsyncResult<List<String>> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onListAvailableJobsDone(result);
        }
    }

    protected void firePerformJobDone(AsyncResult<AppJobCompletion> asyncResult)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onPerformJobDone(asyncResult);
        }
    }

    protected void fireBranded(AsyncResult<BrandInfo> result)
    {
        for (IControllerListener listener: listeners)
        {
            listener.onBranded(result);
        }
    }

}
