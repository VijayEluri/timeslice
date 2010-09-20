package com.enokinomi.timeslice.lib.assign;

import com.google.inject.AbstractModule;

public class AssignModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(ITagStore.class).to(HsqldbTagStore.class).asEagerSingleton();

        // this may move down, so more modules can use it.
        bind(INowProvider.class).to(RealtimeNowProvider.class);
    }

}