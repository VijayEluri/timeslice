package com.enokinomi.timeslice.web.task.client.ui.impl;

import java.util.ArrayList;
import java.util.List;

import com.enokinomi.timeslice.web.task.client.ui.api.IHotlistPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IHotlistPanelListener;
import com.enokinomi.timeslice.web.task.client.ui_one.api.PrefHelper;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HotlistPanel extends Composite implements IHotlistPanel
{
    public static final class Mode
    {
//        public static final String EditSingle = "Hotlist [delete-single]";
//        public static final String EditMulti = "Hotlist [delete-multi]";
//        public static final String Hotlist = "Hotlist";
    }

    private static final String CookieNamePrefix = "timeslice.hotlist.";

    private final HotlistPanelConstants constants;

    private final FlowPanel vp = new FlowPanel();
    private final Anchor mode;

    private final List<IHotlistPanelListener> listeners = new ArrayList<IHotlistPanelListener>();

    @Override
    public Widget asWidget() { return this; }

    @Override
    public void addHotlistPanelListener(IHotlistPanelListener listener)
    {
        if (null != listener)
        {
            listeners.add(listener);
        }
    }

    @Override
    public void removeHotlistPanelListener(IHotlistPanelListener listener)
    {
        listeners.remove(listener);
    }

    protected void fireHotlistItemClicked(String description)
    {
        for (IHotlistPanelListener listener: listeners)
        {
            listener.hotlistItemClicked(description);
        }
    }

    protected void fireHotlistChanged()
    {
        for (IHotlistPanelListener listener: listeners)
        {
            listener.hotlistChanged();
        }
    }

    @Inject
    HotlistPanel(final HotlistPanelConstants constants)
    {
        this.constants = constants;

        mode = new Anchor(constants.hotlist(), true);

        mode.setTitle("Hotlist panel - click to switch between live and delete mode.");
        mode.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                if (constants.hotlist().equals(mode.getText()) && 1 < vp.getWidgetCount())
                {
                    mode.setText(constants.editDeleteSingle());
                }
                else if (constants.editDeleteSingle().equals(mode.getText()))
                {
                    mode.setText(constants.editDeleteMulti());
                }
                else if (constants.editDeleteMulti().equals(mode.getText()))
                {
                    mode.setText(constants.hotlist());
                }
                else
                {
                    mode.setText(constants.hotlist());
                }
            }
        });

        repopulate();

        initWidget(vp);
    }

    @Override
    public void repopulate()
    {
        vp.clear();

        vp.add(mode);

        int added = 0;

        for (final String name: Cookies.getCookieNames())
        {
            if (name.startsWith(CookieNamePrefix))
            {
                final String description = Cookies.getCookie(name);

                Button button = new Button(description, new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        if (constants.hotlist().equals(mode.getText()))
                        {
                            fireHotlistItemClicked(description);
                        }
                        else
                        {
                            Cookies.removeCookie(name);

                            if (constants.editDeleteSingle().equals(mode.getText()))
                            {
                                mode.setText(constants.hotlist());
                            }

                            repopulate();

                            fireHotlistChanged();
                        }
                    }
                });

                if (added < 10)
                {
                    char digitChar = Character.forDigit((1 + added) % 10, 10);
                    button.setAccessKey(digitChar);
                    button.setHTML("<u>" + digitChar + "</u> " + button.getText());
                }

                vp.add(button);

                ++added;
            }
        }

        if (added <= 0)
        {
            mode.setText(constants.hotlist());
        }
    }

    @Override
    public int getHotlistItemCount()
    {
        return vp.getWidgetCount() - 1;
    }

    @Override
    public void addAsHotlistItem(String name, String description)
    {
        Cookies.setCookie(CookieNamePrefix  + name.hashCode(), description, PrefHelper.createDateSufficientlyInTheFuture());
        repopulate();
        fireHotlistChanged();
    }
}