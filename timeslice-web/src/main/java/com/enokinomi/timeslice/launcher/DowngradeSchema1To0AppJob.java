package com.enokinomi.timeslice.launcher;

import java.sql.Connection;

import com.enokinomi.timeslice.timeslice.SchemaDetector;
import com.enokinomi.timeslice.timeslice.SchemaDuty;
import com.enokinomi.timeslice.web.gwt.server.rpc.AppJob;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class DowngradeSchema1To0AppJob implements AppJob
{
    private final String jobId = "downgrade-data-1-0";
    private final Connection conn;

    @Inject
    public DowngradeSchema1To0AppJob(@Named("tsConnection") Connection conn)
    {
        this.conn = conn;
    }

    @Override
    public String getJobId()
    {
        return jobId;
    }

    @Override
    public String perform()
    {
        SchemaDetector detector = new SchemaDetector();
        Integer versionPreUpgrade = detector.detectSchema(conn);

        SchemaDuty upgradeDuty = new SchemaDuty("migration-sql-1-to-0.sql");

        upgradeDuty.createSchema(conn);

        Integer versionPostUpgrade = detector.detectSchema(conn);

        return String.format("Upgrading result: version before -> after: %s -> %s", versionPreUpgrade, versionPostUpgrade);
    }

}