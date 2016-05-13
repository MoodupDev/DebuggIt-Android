package com.moodup.bugreporter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.ButterKnife;

public class BugDescriptionFragment extends Fragment {

    public static final String POSITION = "position";

    private LinearLayout itemsContainer;
    private ImageView recordButton;
    private ReportButton[] kindButtons;
    private ReportButton[] priorityButtons;
    private MontserratEditText bugTitle;

    private MontserratEditText stepsToReproduce;
    private MontserratEditText actualBehaviour;
    private MontserratEditText expectedBehaviour;

    private MediaPlayer mediaPlayer;

    public static BugDescriptionFragment newInstance(int position) {
        BugDescriptionFragment fragment = new BugDescriptionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);

        fragment.setArguments(bundle);
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
        mediaPlayer = new MediaPlayer();
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
                AudioCaptureFragment.newInstance(new AudioCaptureFragment.AudioRecordListener() {
                    @Override
                    public void onRecordUploaded(String audioUrl) {
                        addAudioMiniature(itemsContainer, audioUrl);
                    }

                    @Override
                    public void onFailed() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Audio upload failed!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).show(getChildFragmentManager(), AudioCaptureFragment.TAG);
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
        itemsContainer = ButterKnife.findById(view, R.id.bug_items_container);
        BugReporter reporter = BugReporter.getInstance();

        for (String screenshotUrl : reporter.getReport().getScreensUrls()) {
            addScreenshotMiniature(itemsContainer, screenshotUrl);
        }

        for (String audioUrl : reporter.getReport().getAudioUrls()) {
            addAudioMiniature(itemsContainer, audioUrl);
        }

        RelativeLayout itemAddNewScreenshot = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_new_screenshot, itemsContainer, false);
        itemAddNewScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DialogFragment) getParentFragment()).dismiss();
            }
        });

        itemsContainer.addView(itemAddNewScreenshot);
    }

    private void addScreenshotMiniature(final ViewGroup parent, final String screenUrl) {
        final RelativeLayout itemScreenParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_screenshot, parent, false);
        ImageView itemScreenshot = ButterKnife.findById(itemScreenParent, R.id.item_screenshot_image);
        ImageView itemScreenshotRemove = ButterKnife.findById(itemScreenParent, R.id.item_screenshot_close);

        Picasso.with(getActivity()).load(screenUrl).into(itemScreenshot);
        itemScreenshotRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(itemScreenParent);
                parent.invalidate();
                BugReporter.getInstance().getReport().getScreensUrls().remove(screenUrl);
            }
        });

        parent.addView(itemScreenParent);
    }

    private void addAudioMiniature(final ViewGroup parent, final String audioUrl) {
        final RelativeLayout itemAudioParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_audio, parent, false);
        ImageButton playButton = ButterKnife.findById(itemAudioParent, R.id.item_audio_button);
        final ImageView itemAudioRemove = ButterKnife.findById(itemAudioParent, R.id.item_audio_close);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());

                if (v.isSelected()) {
                    playFromUrl(v, audioUrl);
                } else {
                    stopPlaying();
                }
            }
        });

        itemAudioRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(itemAudioParent);
                parent.invalidate();
                BugReporter.getInstance().getReport().getAudioUrls().remove(audioUrl);
            }
        });

        parent.addView(itemAudioParent, 0);
    }

    private void playFromUrl(final View playView, String url) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playView.setSelected(false);
                mediaPlayer.reset();
            }
        });
    }

    public void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.reset();
        }
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
