package com.moodup.bugreporter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;

public class BugDescriptionFragment extends Fragment {

    public static final String POSITION = "position";

    private ImageView recordButton;
    private ReportButton[] kindButtons;
    private ReportButton[] priorityButtons;
    private MontserratEditText bugTitle;

    private MontserratEditText stepsToReproduce;
    private MontserratEditText actualBehaviour;
    private MontserratEditText expectedBehaviour;

    public static BugDescriptionFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        BugDescriptionFragment fragment = new BugDescriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initViews(inflater, container, getArguments().getInt(POSITION));
    }

    private View initViews(LayoutInflater inflater, ViewGroup container, int position) {
        View view;
        switch (position) {
            case 0:
                view = inflater.inflate(R.layout.fragment_bug_description_page1, container, false);
                initFirstPage(view);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_bug_description_page2, container, false);
                initSecondPage(view);
                break;
        }
        return view;
    }

    private void initFirstPage(View view) {
        bugTitle = ButterKnife.findById(view, R.id.bug_title);
        bugTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BugReporter.getInstance().getReport().setTitle(bugTitle.getText().toString());
            }
        });
        recordButton = ButterKnife.findById(view, R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AudioCaptureFragment().show(getChildFragmentManager(), AudioCaptureFragment.TAG);
            }
        });

        kindButtons = new ReportButton[]{
                ButterKnife.findById(view, R.id.kind_bug_button),
                ButterKnife.findById(view, R.id.kind_enhancement_button)
        };

        initBugKindButtons(view);
        initBugPriorityButtons(view);
    }

    private void initBugPriorityButtons(View view) {
        priorityButtons = new ReportButton[]{
                ButterKnife.findById(view, R.id.priority_low_button),
                ButterKnife.findById(view, R.id.priority_medium_button),
                ButterKnife.findById(view, R.id.priority_high_button)
        };

        for (ReportButton priorityButton : priorityButtons) {
            priorityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) {
                        String priority;
                        if (v.getId() == R.id.priority_low_button) {
                            priority = BitBucket.PRIORITY_MINOR;
                        } else if (v.getId() == R.id.priority_medium_button) {
                            priority = BitBucket.PRIORITY_MAJOR;
                        } else {
                            priority = BitBucket.PRIORITY_CRITICAL;
                        }
                        BugReporter.getInstance().getReport().setPriority(priority);
                    }
                    deselectOtherButtons(v, priorityButtons);
                }
            });
        }

        initReportItems(view);
    }

    private void initReportItems(View view) {
        final LinearLayout itemsContainer = ButterKnife.findById(view, R.id.bug_items_container);
        BugReporter reporter = BugReporter.getInstance();

        for (String screenshotUrl : reporter.getReport().getScreensUrls()) {
            RelativeLayout itemScreenParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_screenshot, itemsContainer, false);
            final ImageView itemScreenshot = ButterKnife.findById(itemScreenParent, R.id.item_screenshot_image);
            ImageView itemScreenshotRemove = ButterKnife.findById(itemScreenParent, R.id.item_screenshot_close);

            Picasso.with(getActivity()).load(screenshotUrl).into(itemScreenshot);
            itemScreenshotRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemsContainer.removeView(itemScreenshot);
                }
            });

            itemsContainer.addView(itemScreenParent);
        }

        for (String audioUrl : reporter.getReport().getAudioUrls()) {
            RelativeLayout itemAudioParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_audio, itemsContainer, false);
            itemsContainer.addView(itemAudioParent);
        }

        RelativeLayout itemAddNewScreenshot = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_new_screenshot, itemsContainer, false);
        itemAddNewScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DrawFragment().show(getActivity().getSupportFragmentManager(), DrawFragment.TAG);
            }
        });

        itemsContainer.addView(itemAddNewScreenshot);
    }

    private void initBugKindButtons(View view) {
        kindButtons = new ReportButton[]{
                ButterKnife.findById(view, R.id.kind_bug_button),
                ButterKnife.findById(view, R.id.kind_enhancement_button)
        };
        for (ReportButton kindButton : kindButtons) {
            kindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) {
                        BugReporter.getInstance().getReport().setKind(v.getId() == R.id.kind_bug_button ? BitBucket.KIND_BUG : BitBucket.KIND_ENHANCEMENT);
                    }
                    deselectOtherButtons(v, kindButtons);
                }
            });
        }
    }

    private void deselectOtherButtons(View selected, ReportButton[] buttons) {
        for (ReportButton button : buttons) {
            if (button.getId() != selected.getId()) {
                button.setSelected(false);
            }
        }
    }

    private void initSecondPage(View view) {
        stepsToReproduce = ButterKnife.findById(view, R.id.steps_text);
        actualBehaviour = ButterKnife.findById(view, R.id.actual_behaviour_text);
        expectedBehaviour = ButterKnife.findById(view, R.id.expected_behaviour_text);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BugReporter.getInstance().getReport().setContent(getContent());
            }
        };
        stepsToReproduce.addTextChangedListener(textWatcher);
        actualBehaviour.addTextChangedListener(textWatcher);
        expectedBehaviour.addTextChangedListener(textWatcher);
    }

    private String getContent() {
        // TODO: 11.05.2016 format content from 2nd screen to markdown template
        return "**Steps to reproduce**: " + stepsToReproduce.getText().toString() + "\n\n" +
                "**Actual behaviour**: " + actualBehaviour.getText().toString() + "\n\n" +
                "**Expected behaviour**: " + expectedBehaviour.getText().toString() + "\n\n";
    }

}
