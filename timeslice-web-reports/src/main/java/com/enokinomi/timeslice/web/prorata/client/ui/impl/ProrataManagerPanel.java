package com.enokinomi.timeslice.web.prorata.client.ui.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.enokinomi.timeslice.web.core.client.util.ListenerManager;
import com.enokinomi.timeslice.web.core.client.util.Registration;
import com.enokinomi.timeslice.web.prorata.client.core.Group;
import com.enokinomi.timeslice.web.prorata.client.core.GroupComponent;
import com.enokinomi.timeslice.web.prorata.client.presenter.api.IProrataManagerPresenter;
import com.enokinomi.timeslice.web.prorata.client.presenter.impl.ProrataManagerPresenter;
import com.enokinomi.timeslice.web.prorata.client.ui.api.IProrataManagerPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProrataManagerPanel extends Composite implements IProrataManagerPanel
{
    private static ProrataManagerPanelUiBinder uiBinder = GWT.create(ProrataManagerPanelUiBinder.class);
    interface ProrataManagerPanelUiBinder extends UiBinder<Widget, ProrataManagerPanel> { }

    @UiField protected FlexTable groupInfoTable;
    @UiField protected TextArea rulesTextArea;
    @UiField protected Button rulesLoadButton;
    @UiField protected Button rulesRemoveAllButton;

    private final TextBox groupBox = new TextBox();
    private final TextBox targetBox = new TextBox();
    private final TextBox weightBox = new TextBox();
    private final Button b;

    private final ProrataManagerPanelConstants constants = GWT.create(ProrataManagerPanelConstants.class);

    private final ListenerManager<Listener> listenerMgr = new ListenerManager<Listener>();

    private static String groupsToString(List<Group> result)
    {
        StringBuilder sb = new StringBuilder();
        for (Group group: result)
        {
            for (GroupComponent comp: group.getComponents())
            {
                sb.append(group.getName())
                    .append("|").append(comp.getName())
                    .append("|").append(comp.getWeight())
                    .append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public void setGroupInfo(List<Group> result)
    {
        updateGroupInfoTable(result, groupsToString(result));
    }

    public static List<Registration> bind(final IProrataManagerPanel ui, final IProrataManagerPresenter presenter)
    {
        List<Registration> result = new ArrayList<Registration>();

        result.add(presenter.addListener(new ProrataManagerPresenter.Listener()
        {
            @Override
            public void allGroupInfoChanged(List<Group> result)
            {
                ui.setGroupInfo(result);
            }

            @Override
            public void addComplete(String group, String name)
            {
                ui.resetInput();
            }

            @Override
            public void removeComplete(String group, String name)
            {
                ui.setGroupInfo(presenter.getGroupInfo());
            }

            @Override
            public void tasksUpdated()
            {
            }

            @Override
            public void addFailed(String msg)
            {
            }
        }));

        result.add(ui.addListener(new IProrataManagerPanel.Listener()
        {
            @Override
            public void addGroupRequested(String name, String target, Double weight)
            {
                presenter.addGroupComponent(name, target, weight);
            }

            @Override
            public void rulesLoadRequested(String text)
            {
                presenter.loadAllRules(text);
            }

            @Override
            public void removeParsedRulesRequested(String text)
            {
                presenter.removeParsedRules(text);
            }

            @Override
            public void removeGroupRequested(String groupName, String name)
            {
                presenter.removeGroupComponent(groupName, name);
            }
        }));

        return result;
    }

    @Override
    public Registration addListener(Listener l) { return listenerMgr.addListener(l); }

    protected void fireAddButtonClicked(String name, String target, Double weight)
    {
        for (Listener l: listenerMgr.getListeners())
        {
            l.addGroupRequested(name, target, weight);
        }
    }

    protected void fireRulesLoadRequested(String text)
    {
        for (Listener l: listenerMgr.getListeners())
        {
            l.rulesLoadRequested(text);
        }
    }

    protected void fireRemoveParseRulesRequested(String text)
    {
        for (Listener listener: listenerMgr.getListeners())
        {
            listener.removeParsedRulesRequested(text);
        }
    }

    @Inject
    ProrataManagerPanel()
    {
        b = new Button(constants.addNew());
        b.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                fireAddButtonClicked(groupBox.getText(), targetBox.getText(), Double.valueOf(weightBox.getText()));
            }
        });

        groupBox.addKeyUpHandler(new KeyUpHandler()
        {
            @Override
            public void onKeyUp(KeyUpEvent event)
            {
                setBButtonEnabled();
            }
        });

        targetBox.addKeyUpHandler(new KeyUpHandler()
        {
            @Override
            public void onKeyUp(KeyUpEvent event)
            {
                setBButtonEnabled();
            }

        });

        weightBox.setText("1");
        weightBox.setWidth("2em");

        setBButtonEnabled();

        initWidget(uiBinder.createAndBindUi(this));
    }

    public void resetInput()
    {
        groupBox.setText("");
        targetBox.setText("");
        weightBox.setText("1");
    }

    private void setBButtonEnabled()
    {
        b.setEnabled(!targetBox.getText().trim().isEmpty() && !groupBox.getText().trim().isEmpty());
    }

    @UiHandler("rulesLoadButton")
    protected void onClick_rulesLoadButton(ClickEvent e)
    {
        fireRulesLoadRequested(rulesTextArea.getText());
    }


    @UiHandler("rulesRemoveAllButton")
    protected void onClick_rulesRemoveAllButton(ClickEvent e)
    {
        fireRemoveParseRulesRequested(rulesTextArea.getText());
    }

    @Override
    public void clear()
    {
        updateGroupInfoTable(Arrays.<Group>asList(), "");
    }

    @Override
    public void updateGroupInfoTable(List<Group> result, String stringRepr)
    {
        rulesTextArea.setText(stringRepr);

        groupInfoTable.removeAllRows();

        int row = 0;

        for (Group group: result)
        {
            final String groupName = group.getName();

            final TextBox targetBox = new TextBox();
            final TextBox weightBox = new TextBox();
            weightBox.setText("1");
            weightBox.setWidth("2em");

            final Button addButton = new Button(constants.add(), new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    fireAddButtonClicked(groupName, targetBox.getText(), Double.valueOf(weightBox.getText()));
                }

            });

            targetBox.addKeyUpHandler(new KeyUpHandler()
            {
                @Override
                public void onKeyUp(KeyUpEvent event)
                {
                    addButton.setEnabled(!targetBox.getText().trim().isEmpty());
                }
            });
            addButton.setEnabled(!targetBox.getText().trim().isEmpty());


            groupInfoTable.setText(row, 0, groupName);
            groupInfoTable.setWidget(row, 1, targetBox);
            groupInfoTable.setWidget(row, 2, weightBox);
            groupInfoTable.setWidget(row, 3, addButton);
            ++row;

            for (final GroupComponent component: group.getComponents())
            {
                int col = 1;
                groupInfoTable.setText(row, col++, component.getName());
                groupInfoTable.setText(row, col++, component.getWeight().toString());
                Anchor anchor = new Anchor(constants.deleteTextIcon());
                groupInfoTable.setWidget(row, col++, anchor);
                anchor.addClickHandler(new ClickHandler()
                {
                    @Override
                    public void onClick(ClickEvent event)
                    {
                        fireRemoveGroupRequested(component.getGroupName(), component.getName());
                    }
                });

                ++row;
            }
        }

        groupInfoTable.setWidget(row, 0, groupBox);
        groupInfoTable.setWidget(row, 1, targetBox);
        groupInfoTable.setWidget(row, 2, weightBox);
        groupInfoTable.setWidget(row, 3, b);

        ++row;
    }

    protected void fireRemoveGroupRequested(String groupName, String name)
    {
        for (Listener l: listenerMgr.getListeners())
        {
            l.removeGroupRequested(groupName, name);
        }
    }

}
