package com.moodup.bugreporter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

public class BugDescriptionFragment extends Fragment {

    public static final String POSITION = "position";
    public static final int RECORD_PERMISSIONS_REQUEST = 145;

    private LinearLayout itemsContainer;
    private ImageView recordButton;
    private MontserratTextView[] kindButtons;
    private MontserratTextView[] priorityButtons;
    private MontserratEditText bugTitle;

    private MontserratEditText stepsToReproduce;
    private MontserratEditText actualBehaviour;
    private MontserratEditText expectedBehaviour;

    private MediaPlayer mediaPlayer;

    private View lastPlayButton;

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
                initReport();
                break;
            default:
                view = inflater.inflate(R.layout.fragment_bug_description_page2, container, false);
                initSecondPage(view);
                initReportContent();
                break;
        }
        return view;
    }


    private void initFirstPage(View view) {
        mediaPlayer = new MediaPlayer();
        bugTitle = (MontserratEditText) view.findViewById(R.id.bug_title);
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
        bugTitle.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    return true;
                }
                return false;
            }
        });
        recordButton = (ImageView) view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!areRecordPermissionsGranted(getActivity())) {
                    requestRecordPermissions(getActivity());
                } else {
                    v.setSelected(!v.isSelected());
                    AudioCaptureFragment.newInstance(new AudioCaptureFragment.AudioRecordListener() {
                        @Override
                        public void onRecordUploaded(String audioUrl) {
                            addAudioMiniature(itemsContainer, audioUrl);
                            v.setSelected(!v.isSelected());
                        }

                        @Override
                        public void onFailed() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), R.string.upload_audio_failed, Toast.LENGTH_LONG).show();
                                    v.setSelected(!v.isSelected());
                                }
                            });
                        }
                    }).show(getChildFragmentManager(), AudioCaptureFragment.TAG);
                }
            }
        });

        kindButtons = new MontserratTextView[] {
                (MontserratTextView) view.findViewById(R.id.kind_bug_button),
                (MontserratTextView) view.findViewById(R.id.kind_enhancement_button)
        };

        initBugKindButtons(view);
        initBugPriorityButtons(view);
    }

    public void requestRecordPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[] { Manifest.permission.RECORD_AUDIO },
                RECORD_PERMISSIONS_REQUEST);
    }

    public boolean areRecordPermissionsGranted(Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void initBugPriorityButtons(View view) {
        priorityButtons = new MontserratTextView[] {
                (MontserratTextView) view.findViewById(R.id.priority_low_button),
                (MontserratTextView) view.findViewById(R.id.priority_medium_button),
                (MontserratTextView) view.findViewById(R.id.priority_high_button),
        };

        for (MontserratTextView priorityButton : priorityButtons) {
            priorityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()) {
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
                }
            });
        }

        initReportItems(view);
    }

    private void initReportItems(View view) {
        itemsContainer = (LinearLayout) view.findViewById(R.id.bug_items_container);
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
                View rootView = getActivity().findViewById(android.R.id.content);
                ImageView reportButton = (ImageView) rootView.findViewById(R.id.report_button);
                reportButton.setImageDrawable(getResources().getDrawable(R.drawable.next_screenshoot));
                ((DialogFragment) getParentFragment()).dismiss();
            }
        });

        itemsContainer.addView(itemAddNewScreenshot);
    }

    private void initReport() {
        Report report = BugReporter.getInstance().getReport();
        if (!report.getTitle().isEmpty()) {
            bugTitle.setText(report.getTitle());
            bugTitle.setSelection(report.getTitle().length());
        }
        if (!report.getKind().isEmpty()) {
            kindButtons[(report.getKind().equalsIgnoreCase(BitBucket.KIND_BUG) ? 0 : 1)].setSelected(true);
        } else {
            kindButtons[0].setSelected(true);
            report.setKind(BitBucket.KIND_BUG);
        }
        if (!report.getPriority().isEmpty()) {
            int index;
            if (report.getPriority().equalsIgnoreCase(BitBucket.PRIORITY_MINOR)) {
                index = 0;
            } else if (report.getPriority().equalsIgnoreCase(BitBucket.PRIORITY_MAJOR)) {
                index = 1;
            } else {
                index = 2;
            }
            priorityButtons[index].setSelected(true);
        } else {
            priorityButtons[1].setSelected(true);
            report.setPriority(BitBucket.PRIORITY_MAJOR);
        }
    }

    private void initReportContent() {
        Report report = BugReporter.getInstance().getReport();
        if (!report.getActualBehaviour().isEmpty()) {
            actualBehaviour.setText(report.getActualBehaviour());
        }
        if(!report.getStepsToReproduce().isEmpty()) {
            stepsToReproduce.setText(report.getStepsToReproduce());
        }
        if(!report.getExpectedBehaviour().isEmpty()) {
            expectedBehaviour.setText(report.getExpectedBehaviour());
        }
    }

    private void addScreenshotMiniature(final ViewGroup parent, final String screenUrl) {
        final RelativeLayout itemScreenParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_screenshot, parent, false);
        ImageView itemScreenshot = (ImageView) itemScreenParent.findViewById(R.id.item_screenshot_image);
        ImageView itemScreenshotRemove = (ImageView) itemScreenParent.findViewById(R.id.item_screenshot_close);

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
        ImageButton playButton = (ImageButton) itemAudioParent.findViewById(R.id.item_audio_button);
        final ImageView itemAudioRemove = (ImageView) itemAudioParent.findViewById(R.id.item_audio_close);

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
        if(mediaPlayer.isPlaying() && lastPlayButton != null) {
            stopPlaying();
            lastPlayButton.setSelected(false);
            playView.setSelected(false);
            return;
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final LoadingDialog dialog = LoadingDialog.newInstance(getString(R.string.loading_dialog_message_play_audio));
        dialog.show(getChildFragmentManager(), LoadingDialog.TAG);
        lastPlayButton = playView;
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                dialog.dismiss();
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
        kindButtons = new MontserratTextView[] {
                (MontserratTextView) view.findViewById(R.id.kind_bug_button),
                (MontserratTextView) view.findViewById(R.id.kind_enhancement_button)
        };
        for (MontserratTextView kindButton : kindButtons) {
            kindButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!v.isSelected()) {
                        v.setSelected(!v.isSelected());
                        if (v.isSelected()) {
                            BugReporter.getInstance().getReport().setKind(v.getId() == R.id.kind_bug_button ? BitBucket.KIND_BUG : BitBucket.KIND_ENHANCEMENT);
                        }
                        deselectOtherButtons(v, kindButtons);
                    }
                }
            });
        }
    }

    private void deselectOtherButtons(View selected, MontserratTextView[] buttons) {
        for (MontserratTextView button : buttons) {
            if (button.getId() != selected.getId()) {
                button.setSelected(false);
            }
        }
    }

    private void initSecondPage(View view) {
        stepsToReproduce = (MontserratEditText) view.findViewById(R.id.steps_text);
        actualBehaviour = (MontserratEditText) view.findViewById(R.id.actual_behaviour_text);
        expectedBehaviour = (MontserratEditText) view.findViewById(R.id.expected_behaviour_text);
        initTextWatchers();
    }

    private void initTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s == stepsToReproduce.getEditableText()) {
                    BugReporter.getInstance().getReport().setStepsToReproduce(stepsToReproduce.getText().toString());
                } else if(s == actualBehaviour.getEditableText()) {
                    BugReporter.getInstance().getReport().setActualBehaviour(actualBehaviour.getText().toString());
                } else {
                    BugReporter.getInstance().getReport().setExpectedBehaviour(expectedBehaviour.getText().toString());
                }
            }
        };
        stepsToReproduce.addTextChangedListener(watcher);
        actualBehaviour.addTextChangedListener(watcher);
        expectedBehaviour.addTextChangedListener(watcher);
    }

}
