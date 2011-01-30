package com.enokinomi.timeslice.web.task.server.impl;

import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.enokinomi.timeslice.lib.task.api.StartTag;
import com.enokinomi.timeslice.lib.task.api.TaskTotalMember;
import com.enokinomi.timeslice.lib.util.ITransform;
import com.enokinomi.timeslice.web.task.client.core.TaskTotal;


public class ServerToClient
{
    ServerToClient()
    {
    }

    public static ITransform<StartTag, com.enokinomi.timeslice.web.task.client.core.StartTag> createStartTagTx(final int tzOffsetMinutes)
    {
        return new ITransform<StartTag, com.enokinomi.timeslice.web.task.client.core.StartTag>()
        {
            @Override
            public com.enokinomi.timeslice.web.task.client.core.StartTag apply(StartTag r)
            {
                DateTimeZone tzOffsetMs = DateTimeZone.forOffsetMillis(tzOffsetMinutes*60*1000);

                return new com.enokinomi.timeslice.web.task.client.core.StartTag(
                        r.getWhen().toDateTime(tzOffsetMs).toString(),
                        r.getUntil() == null ? null : r.getUntil().toDateTime(tzOffsetMs).toString(),
                        r.getUntil() == null ? null : new Double(new Duration(r.getWhen(), r.getUntil()).toDuration().getMillis()),
                        r.getWhat(),
                        r.getWhen().isBeforeNow()
                        );
            }
        };
    }

    public static ITransform<TaskTotalMember, TaskTotal> createTaskTotal(final int tzOffsetMinutes)
    {
        return new ITransform<TaskTotalMember, TaskTotal>()
        {
            @Override
            public TaskTotal apply(TaskTotalMember r)
            {
                return new TaskTotal(
                        r.getWho(),
                        r.getMillis() / 1000. / 60. / 60.,
                        r.getPercentage(),
                        r.getWhat());
            }
        };
    }
}
