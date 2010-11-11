package com.enokinomi.timeslice.web.task.client.ui_one.impl;

import static com.enokinomi.timeslice.web.core.client.util.TransformUtils.tr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.enokinomi.timeslice.web.appjob.client.core.AppJobCompletion;
import com.enokinomi.timeslice.web.appjob.client.ui.api.IAppJobPanelListener;
import com.enokinomi.timeslice.web.appjob.client.ui.api.IAppJobPanel;
import com.enokinomi.timeslice.web.assign.client.core.AssignedTaskTotal;
import com.enokinomi.timeslice.web.core.client.util.AsyncResult;
import com.enokinomi.timeslice.web.core.client.util.Checks;
import com.enokinomi.timeslice.web.core.client.util.ITransform;
import com.enokinomi.timeslice.web.task.client.controller.api.IController;
import com.enokinomi.timeslice.web.task.client.controller.api.IControllerListener;
import com.enokinomi.timeslice.web.task.client.core.StartTag;
import com.enokinomi.timeslice.web.task.client.core.TaskTotal;
import com.enokinomi.timeslice.web.task.client.core_todo_move_out.BrandInfo;
import com.enokinomi.timeslice.web.task.client.core_todo_move_out.SortDir;
import com.enokinomi.timeslice.web.task.client.ui.api.IHistoryPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IHistoryPanelListener;
import com.enokinomi.timeslice.web.task.client.ui.api.IHotlistPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IHotlistPanelListener;
import com.enokinomi.timeslice.web.task.client.ui.api.IOptionsListener;
import com.enokinomi.timeslice.web.task.client.ui.api.IOptionsPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IOptionsProvider;
import com.enokinomi.timeslice.web.task.client.ui.api.IParamPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IReportPanel;
import com.enokinomi.timeslice.web.task.client.ui.api.IReportPanelListener;
import com.enokinomi.timeslice.web.task.client.ui_one.api.BulkItemListener;
import com.enokinomi.timeslice.web.task.client.ui_one.api.IImportBulkItemsDialog;
import com.enokinomi.timeslice.web.task.client.ui_one.api.ITimesliceApp;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TimesliceApp extends ResizeComposite implements ITimesliceApp
{
    private final TimesliceAppConstants constants;
    private final IController controller;
    private final IHistoryPanel historyPanel;
    private final MultiWordSuggestOracle suggestSource;
    private final SuggestBox taskDescriptionEntry;
    private final HorizontalPanel entryPanel = new HorizontalPanel();
    private final Anchor updateLink;
    private final IHotlistPanel hotlistPanel;
    private final Anchor addHotlink;
    private final Anchor enterLink;
    private final VerticalPanel actionPanel = new VerticalPanel();
    private final Anchor bulkLink;
    private final VerticalPanel idleActionPanel = new VerticalPanel();
    private final Label serverInfoLabel = new Label("[querying]");
    private final DateBox specifiedDateBox = new DateBox();
    private final RadioButton modeRadioSpecify;
    private final RadioButton modeRadioNormal;
    private final IReportPanel reportPanel;
    private final IAppJobPanel appJobPanel;
    private final IOptionsPanel optionsPanel;
    private final HTML issuesLink = new HTML();
    private final Timer timer;
    private final IOptionsProvider options;
    private final Provider<IImportBulkItemsDialog> importBulkItemsDialog;

    private String originalWindowTitle;

    private static final StartTag UnknownTag = new StartTag(null, null, null, "-unknown-", false);

    @Override
    public Widget asWidget() { return this; }

    @Inject
    TimesliceApp(TimesliceAppConstants constants, IController controller, IHistoryPanel historyPanel, IReportPanel reportPanel, IOptionsPanel optionsPanel, IAppJobPanel appJobPanel, Provider<IImportBulkItemsDialog> importBulkItemsDialog, IHotlistPanel hotlistPanel)
    {
        this.constants = constants;
        this.controller = controller;
        this.historyPanel = historyPanel;
        this.reportPanel = reportPanel;
        this.optionsPanel = optionsPanel;
        this.appJobPanel = appJobPanel;
        this.importBulkItemsDialog = importBulkItemsDialog;
        this.hotlistPanel = hotlistPanel;

        suggestSource = new MultiWordSuggestOracle();
        taskDescriptionEntry = new SuggestBox(suggestSource);
        updateLink = new Anchor(constants.updateLabel());
        addHotlink = new Anchor(constants.addToHotlist());
        enterLink = new Anchor(constants.enter());
        bulkLink = new Anchor(constants.bulk());
        modeRadioSpecify = new RadioButton("MODE", constants.specifyDate());
        modeRadioNormal = new RadioButton("MODE", constants.current());

        options = optionsPanel;

        timer = new Timer()
        {
            @Override
            public void run()
            {
                scheduleRefresh();
            }
        };

        initWidget(this.onModuleLoadNop());
    }

    private void updateStartTag(StartTag editedStartTag)
    {
        controller.startEditDescription(editedStartTag);
    }

    private void enterNewStartTag(String description)
    {
        enterNewStartTag(null, description);
    }

    private void enterNewStartTag(String instantString, String description)
    {
        if (description.trim().isEmpty())
        {
            scheduleRefresh();
        }
        else
        {
            controller.startAddItem(instantString, description);
        }
    }

    private void scheduleHotlinkValidation()
    {
        DeferredCommand.addCommand(new Command()
        {
            public void execute()
            {
                boolean descriptionIsEmpty = taskDescriptionEntry.getText().trim().isEmpty();
                actionPanel.setVisible(!descriptionIsEmpty);
                idleActionPanel.setVisible(descriptionIsEmpty);
            }
        });
    }

    private void scheduleHotlistValidation()
    {
        DeferredCommand.addCommand(new Command()
        {
            public void execute()
            {
                hotlistPanel.asWidget().setVisible(0 < hotlistPanel.getHotlistItemCount());
            }
        });
    }
    private void fixSpecifiedDateBox(boolean value)
    {
        specifiedDateBox.setEnabled(value);
        specifiedDateBox.setVisible(value);
    }

    private void refreshTotals()
    {
        IParamPanel params = reportPanel.getParamsPanel();
        refreshTotals(
                params.getStartingTimeRendered(),
                params.getEndingTimeRendered(),
                Arrays.asList(params.getAllowWords().getText().split(",")),
                Arrays.asList(params.getIgnoreWords().getText().split(",")));
    }

    private void refreshTotals(String startingTimeText, String endingTimeText, List<String> allowWords, List<String> ignoreWords)
    {
        controller.startRefreshTotals(
                1000,
                SortDir.desc,
                startingTimeText,
                endingTimeText,
                allowWords,
                ignoreWords);

        controller.startRefreshTotalsAssigned(
                1000,
                SortDir.desc,
                startingTimeText,
                endingTimeText,
                allowWords,
                ignoreWords);
    }

    public Widget onModuleLoadNop()
    {
        originalWindowTitle = Window.getTitle();

        optionsPanel.addOptionsListener(new IOptionsListener()
        {
            public void optionsChanged(IOptionsPanel source)
            {
                timer.cancel();
                if (options.isAutoRefresh())
                {
                    timer.scheduleRepeating(options.getAutoRefreshMs());
                }

                scheduleRefresh();
            }
        });

        historyPanel.addHistoryPanelListener(new IHistoryPanelListener()
        {
            public void interestingThing(String p)
            {
                enterNewStartTag(p);
            }

            public void fireEdited(StartTag editedStartTag)
            {
                updateStartTag(editedStartTag);
            }

            public void fireTimeEdited(StartTag startTag)
            {
                enterNewStartTag(startTag.getInstantString(), startTag.getDescription());
            }

            public void hotlisted(String name, String description)
            {
                hotlistPanel.addAsHotlistItem(name, description);
            }

            @Override
            public void editModeEntered()
            {
                timer.cancel();
            }

            @Override
            public void editModeLeft()
            {
                if (options.isAutoRefresh()) timer.scheduleRepeating(options.getAutoRefreshMs());
            }
        });

        addHotlink.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                if (!taskDescriptionEntry.getText().trim().isEmpty())
                {
                    hotlistPanel.addAsHotlistItem(taskDescriptionEntry.getText(), taskDescriptionEntry.getText());
                    taskDescriptionEntry.setText("");
                    scheduleHotlistValidation();
                    scheduleHotlinkValidation();
                }
            }
        });

        taskDescriptionEntry.setWidth("30em");
        taskDescriptionEntry.setAccessKey('t');
        scheduleHotlinkValidation();


        taskDescriptionEntry.getTextBox().addKeyPressHandler(new KeyPressHandler()
        {
            @Override
            public void onKeyPress(KeyPressEvent event)
            {
                scheduleHotlinkValidation();

                if (KeyCodes.KEY_ESCAPE == event.getCharCode())
                {
                    taskDescriptionEntry.setText("");
                }
                else if (event.isControlKeyDown() && (KeyCodes.KEY_ENTER == event.getCharCode()))
                {
                    enterNewStartTag(taskDescriptionEntry.getText());
                }
                else if (options.isControlSpaceSends() && event.isControlKeyDown() && (' ' == event.getCharCode()))
                {
                    enterNewStartTag(taskDescriptionEntry.getText());
                }
                event.stopPropagation();
            }
        });

        updateLink.setAccessKey('u');
        updateLink.setHref("#");
        updateLink.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                scheduleRefresh();
            }
        });

        enterLink.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                enterNewStartTag(taskDescriptionEntry.getText());
            }
        });

        actionPanel.add(enterLink);
        actionPanel.add(addHotlink);
        actionPanel.setStyleName("ts-actionPanel");

        bulkLink.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                BulkItemListener listener = new BulkItemListener()
                {
                    @Override
                    public void addItems(List<StartTag> items)
                    {
                        controller.startAddItems(items);
                    }
                };

                IImportBulkItemsDialog d = importBulkItemsDialog.get();
                d.addBulkItemListener(listener);
                d.asDialog().show();
                d.removeBulkItemListener(listener);
            }
        });

        idleActionPanel.add(bulkLink);
        idleActionPanel.setStyleName("ts-idlePanel");

        entryPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        entryPanel.setSpacing(5);
        entryPanel.add(updateLink);
        entryPanel.add(new HTML(constants.task(), false));
        entryPanel.add(taskDescriptionEntry);
        entryPanel.add(actionPanel);
        entryPanel.add(idleActionPanel);

        scheduleHotlistValidation();
        hotlistPanel.addHotlistPanelListener(new IHotlistPanelListener()
        {
            public void hotlistItemClicked(String description)
            {
                enterNewStartTag(description);
            }

            public void hotlistChanged()
            {
                scheduleHotlistValidation();
            }
        });

        specifiedDateBox.addValueChangeHandler(new ValueChangeHandler<Date>()
        {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event)
            {
                scheduleRefresh();
            }
        });

        modeRadioSpecify.addValueChangeHandler(new ValueChangeHandler<Boolean>()
                {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event)
                    {
                        fixSpecifiedDateBox(event.getValue());
                        scheduleRefresh();
                    }
                });
        modeRadioNormal.addValueChangeHandler(new ValueChangeHandler<Boolean>()
                {
                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event)
                    {
                        fixSpecifiedDateBox(!event.getValue());
                        scheduleRefresh();
                    }
                });
        modeRadioNormal.setValue(true, true);

        FlowPanel modePanel = new FlowPanel();
        modePanel.add(modeRadioNormal);
        modePanel.add(modeRadioSpecify);
        modePanel.add(specifiedDateBox);

        DockLayoutPanel mainEntryPanel = new DockLayoutPanel(Unit.EM);

        //VerticalPanel mainEntryPanel = new VerticalPanel();
        mainEntryPanel.addNorth(modePanel, 3);
        mainEntryPanel.addSouth(hotlistPanel.asWidget(), 4);
        mainEntryPanel.addSouth(entryPanel, 3);
        mainEntryPanel.add(historyPanel.asWidget());

        //historyPanel.setHeight("30em");
        //historyPanel.setWidth("50em");

        reportPanel.addReportPanelListener(new IReportPanelListener()
        {
            @Override
            public void refreshRequested(String startingTimeText, String endingTimeText, List<String> allowWords, List<String> ignoreWords)
            {
                refreshTotals(startingTimeText, endingTimeText, allowWords, ignoreWords);
            }

            @Override
            public void persistRequested(String persistAsName, String startingTimeText, String endingTimeText, List<String> allowWords, List<String> ignoreWords)
            {
                controller.startPersistTotals(
                        persistAsName,
                        1000,
                        SortDir.desc,
                        startingTimeText,
                        endingTimeText,
                        allowWords,
                        ignoreWords);
            }

            @Override
            public void billeeUpdateRequested(String description, String newBillee)
            {
                controller.startAssignBillee(description, newBillee);
            }
        });

        final TabLayoutPanel tp = new TabLayoutPanel(2, Unit.EM);
        Anchor inputlink = new Anchor(constants.input(), true);
        inputlink.setAccessKey('i');
        tp.add(mainEntryPanel, inputlink);
        Anchor reportslink = new Anchor(constants.reports(), true);
        reportslink.setAccessKey('r');
        tp.add(reportPanel.asWidget(), reportslink);
        Anchor optionslink = new Anchor(constants.options(), true);
        optionslink.setAccessKey('o');
        tp.add(optionsPanel.asWidget(), optionslink);
        Anchor jobsLink = new Anchor(constants.jobs(), true);
        jobsLink.setAccessKey('j');
        tp.add(appJobPanel.asWidget(), jobsLink);

        tp.selectTab(0);

        Anchor logoutAnchor = new Anchor(constants.logout());
        logoutAnchor.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                controller.logout();
            }
        });

        serverInfoLabel.addClickHandler(new ClickHandler()
        {
            @Override
            public void onClick(ClickEvent event)
            {
                serverInfoLabel.setText("[querying...]");
                controller.serverInfo();
            }
        });
        controller.serverInfo();

        HorizontalPanel buildLabelBox = new HorizontalPanel();
        buildLabelBox.setSpacing(15);
        updateIssuesLink("#");
        buildLabelBox.add(issuesLink);
        buildLabelBox.add(logoutAnchor);
        buildLabelBox.add(serverInfoLabel);

        final DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
        dockPanel.addSouth(buildLabelBox, 4);
        dockPanel.add(tp);

        controller.addControllerListener(new IControllerListener()
            {
                @Override
                public void authenticated()
                {
                    // start auto-refresh
                    if (options.isAutoRefresh()) timer.scheduleRepeating(options.getAutoRefreshMs());
                    scheduleRefresh();
                }

                @Override
                public void unauthenticated(boolean retry)
                {
                    // notify ? blank stuff ?
                    // stop auto-refresh
                    timer.cancel();
                    if (retry) scheduleRefresh();
                }

                public void onAddItemDone(AsyncResult<Void> result)
                {
                    handleAddItemDone(result);
                }

                public void onRefreshItemsDone(AsyncResult<List<StartTag>> result)
                {
                    handleRefreshItemsDone(result);
                }

                @Override
                public void onRefreshTotalsDone(AsyncResult<List<TaskTotal>> result)
                {
                    if (result.isError())
                    {
                        GWT.log("got error back: " + result.getThrown().getMessage(), result.getThrown());
                    }
                    else
                    {
                        reportPanel.setResults(result.getReturned());
                    }
                }

                @Override
                public void onRefreshTotalsAssignedDone(AsyncResult<List<AssignedTaskTotal>> result)
                {
                    if (result.isError())
                    {
                        GWT.log("got error back: " + result.getThrown().getMessage(), result.getThrown());
                    }
                    else
                    {
                        reportPanel.setResultsAssigned(result.getReturned());
                    }
                }

                @Override
                public void onPersistTotalsDone(AsyncResult<String> result)
                {
                    if (result.isError())
                    {
                        GWT.log("got error back: " + result.getThrown().getMessage(), result.getThrown());
                    }
                    else
                    {
                        // TODO: show download link in browser.
                        reportPanel.setPersisted(result.getReturned());
                    }
                }

                @Override
                public void serverInfoRecieved(String info)
                {
                    serverInfoLabel.setText(info);
                }

                @Override
                public void onAssignBilleeDone(AsyncResult<Void> result)
                {
                    if (result.isError())
                    {
                        GWT.log("got error back: " + result.getThrown().getMessage(), result.getThrown());
                    }
                    else
                    {
                        refreshTotals();
                    }
                }

                @Override
                public void onAllBilleesDone(AsyncResult<List<String>> asyncResult)
                {
                    if (asyncResult.isError())
                    {
                        GWT.log("Error during refreshing all billees: " + asyncResult.getThrown().getMessage());
                    }
                    else
                    {
                        reportPanel.setBillees(asyncResult.getReturned());
                    }
                }

                @Override
                public void onListAvailableJobsDone(AsyncResult<List<String>> result)
                {
                    if (result.isError())
                    {
                        GWT.log("Listing server-side jobs failed: " + result.getThrown().getMessage());
                        appJobPanel.redisplayJobIds(new ArrayList<String>());
                    }
                    else
                    {
                        appJobPanel.redisplayJobIds(result.getReturned());
                    }
                }

                @Override
                public void onPerformJobDone(AsyncResult<AppJobCompletion> result)
                {
                    if (result.isError())
                    {
                        appJobPanel.addResult("-", "call failed", result.getThrown().getMessage());
                        GWT.log("Server-side job failed: " + result.getThrown().getMessage());
                    }
                    else
                    {
                        AppJobCompletion returned = result.getReturned();
                        appJobPanel.addResult(returned.getJobId(), returned.getStatus(), returned.getDescription());
                    }
                }

                @Override
                public void onBranded(AsyncResult<BrandInfo> result)
                {
                    if (!result.isError())
                    {
                        handleBrandInfo(result.getReturned());
                    }
                    else
                    {
                        GWT.log("Leaving unbranded.");
                    }
                }
            });

        appJobPanel.addListener(new IAppJobPanelListener()
        {
            @Override
            public void appJobRequested(String jobId)
            {
                controller.startPerformJob(jobId);
            }

            @Override
            public void appJobListRefreshRequested()
            {
                controller.startListAvailableJobs();
            }
        });

        if (options.isAutoRefresh()) timer.scheduleRepeating(options.getAutoRefreshMs());

        scheduleRefresh();
        controller.startListAvailableJobs();
        controller.startGetBranding();
        controller.startGetAllBillees();

        return dockPanel;
    }

    private void updateIssuesLink(String issuesHref)
    {
        issuesLink.setHTML("<a href=\"" + issuesHref + "\" target=\"_blank\">Feedback / RFEs / Bugs</a>");
    }

    protected void handleBrandInfo(BrandInfo brandInfo)
    {
        updateIssuesLink(brandInfo.getIssueHref());
    }

    private void handleRefreshItemsDone(AsyncResult<List<StartTag>> result)
    {
        if (!result.isError())
        {
            ArrayList<StartTag> items = new ArrayList<StartTag>(result.getReturned());

            historyPanel.clear(false);
            historyPanel.addItems(items);

            ArrayList<String> descriptions = tr(items, new ArrayList<String>(), new ITransform<StartTag, String>()
            {
                @Override
                public String apply(StartTag r)
                {
                    return r.getDescription();
                }
            });

            updateSuggestSource(descriptions);
            historyPanel.setSuggestWords(descriptions);

            Window.setTitle(
                options.isCurrentTaskInTitlebar()
                    ? renderTitlebar(Checks.mapNullTo(findCurrentStartTag(items), UnknownTag).getDescription())
                    : originalWindowTitle);
        }
        else
        {
            showError(result);

//            messagePanel.add(new AcknowledgableMessagePanel("No refresh happened: " + result.getThrown().getMessage()));
        }
    }

    private StartTag findCurrentStartTag(ArrayList<StartTag> items)
    {
        String now = IParamPanel.MachineFormat.format(new Date());

        for (int i = 0; i < items.size(); ++i)
        {
            if (now.compareTo(items.get(i).getInstantString()) >= 0)
            {
                return items.get(i);
            }
        }

        return null;
    }

    private void updateSuggestSource(ArrayList<String> items)
    {
        suggestSource.clear();
        suggestSource.addAll(items);
    }

    private void showError(AsyncResult<?> result)
    {
        String tmsg = "(nothing thrown)";

        Throwable t = result.getThrown();
        if (null != t) tmsg = t.getMessage();

        Label label = new Label(tmsg);

        VerticalPanel vp = new VerticalPanel();
        vp.add(label);

        DialogBox msgBox = new DialogBox(true);
        msgBox.setWidget(vp);
        msgBox.show();

        GWT.log("showed message: " + tmsg, null);
    }

    private void handleAddItemDone(AsyncResult<Void> result)
    {
        if (!result.isError())
        {
//            messagePanel.add(new AcknowledgableMessagePanel("Item added."));
            taskDescriptionEntry.setText("");
            scheduleRefresh();
            scheduleHotlinkValidation();
        }
        else
        {
            showError(result);
//            messagePanel.add(new AcknowledgableMessagePanel("No item added."));
        }

//        newItemForm.setFormEnabled(true);
    }

    private void scheduleRefresh()
    {
        DeferredCommand.addCommand(new Command()
        {
            public void execute()
            {
                String starting = IParamPanel.MachineFormat.format(new Date(new Date().getTime() - options.getMaxSeconds() * 1000));
                String ending = null;

                if (modeRadioSpecify.getValue() && null != specifiedDateBox.getValue())
                {
                    Date specifiedDate = specifiedDateBox.getValue();
                    Date beginningOfSpecifiedDay = floorDate(specifiedDate);
                    Date untilEndOfSpecifiedDay = new Date(beginningOfSpecifiedDay.getTime() + 1000*3600*24);

                    starting = IParamPanel.MachineFormat.format(beginningOfSpecifiedDay);
                    ending = IParamPanel.MachineFormat.format(untilEndOfSpecifiedDay);
                }

                controller.startRefreshItems(
                        options.getMaxSize(),
                        starting,
                        ending);
            }

            /**
             * Helps when date-pickers return noon, and you want start-of-day 00:00.
             *
             * @param specifiedDate
             * @return
             */
            @SuppressWarnings("deprecation")
            private Date floorDate(Date specifiedDate)
            {
                Date floor = new Date(specifiedDate.getTime());
                floor.setHours(0);
                floor.setMinutes(0);
                floor.setSeconds(0);
                return floor;
            }
        });
    }

    public String renderTitlebar(String currentTaskDescription)
    {
        return options.getTitleBarTemplate().replaceAll(IOptionsProvider.CurrentTaskToken, currentTaskDescription);
    }
}