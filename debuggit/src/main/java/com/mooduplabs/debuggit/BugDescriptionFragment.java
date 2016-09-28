package com.mooduplabs.debuggit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BugDescriptionFragment extends Fragment {

    public static final String POSITION = "position";
    public static final int RECORD_PERMISSIONS_REQUEST = 145;
    private static final int PRIORITY_LOW_INDEX = 0;
    private static final int PRIORITY_MEDIUM_INDEX = 1;
    private static final int PRIORITY_HIGH_INDEX = 2;
    private static final int KIND_BUG_INDEX = 0;
    private static final int KIND_ENHANCEMENT_INDEX = 1;

    private LinearLayout itemsContainer;
    private MontserratTextView[] kindButtons;
    private MontserratTextView[] priorityButtons;
    private MontserratEditText bugTitle;

    private MontserratEditText stepsToReproduce;
    private MontserratEditText actualBehaviour;
    private MontserratEditText expectedBehaviour;

    private MediaPlayer mediaPlayer;

    private View lastPlayButton;
    private TextWatcher contentTextWatcher;

    public static BugDescriptionFragment newInstance(int position) {
        BugDescriptionFragment fragment = new BugDescriptionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, position);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        if(bugTitle != null) {
            bugTitle.removeTextChangedListener(contentTextWatcher);
        }
        if(actualBehaviour != null) {
            actualBehaviour.removeTextChangedListener(contentTextWatcher);
            stepsToReproduce.removeTextChangedListener(contentTextWatcher);
            expectedBehaviour.removeTextChangedListener(contentTextWatcher);
        }
        super.onDestroyView();
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
                view = inflater.inflate(R.layout.fragment_br_bug_description_page1, container, false);
                initFirstPage(view);
                initReport();
                break;
            default:
                view = inflater.inflate(R.layout.fragment_br_bug_description_page2, container, false);
                initSecondPage(view);
                initReportContent();
                break;
        }
        return view;
    }


    private void initFirstPage(View view) {
        mediaPlayer = new MediaPlayer();
        initBugTitle(view);
        initRecordButton(view);
        initBugKindButtons(view);
        initBugPriorityButtons(view);
        initTextWatchers(true);
    }

    private void initBugTitle(View view) {
        bugTitle = (MontserratEditText) view.findViewById(R.id.bug_title);
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
    }

    private void initRecordButton(View view) {
        ImageView recordButton = (ImageView) view.findViewById(R.id.record_button);
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
                        public void onFailed(final boolean canceled) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!canceled) {
                                        ConfirmationDialog.newInstance(getString(R.string.br_upload_audio_failed), true).show(getChildFragmentManager(), ConfirmationDialog.TAG);
                                    }
                                    v.setSelected(!v.isSelected());
                                }
                            });
                        }
                    }).show(getChildFragmentManager(), AudioCaptureFragment.TAG);
                }
            }
        });
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
                                priority = Constants.PRIORITY_LOW;
                            } else if (v.getId() == R.id.priority_medium_button) {
                                priority = Constants.PRIORITY_MEDIUM;
                            } else {
                                priority = Constants.PRIORITY_HIGH;
                            }
                            DebuggIt.getInstance().getReport().setPriority(priority);
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
        Report report = DebuggIt.getInstance().getReport();

        for (String screenshotUrl : report.getScreensUrls()) {
            addScreenshotMiniature(itemsContainer, screenshotUrl);
        }

        for (String audioUrl : report.getAudioUrls()) {
            addAudioMiniature(itemsContainer, audioUrl);
        }

        RelativeLayout itemAddNewScreenshot = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_br_new_screenshot, itemsContainer, false);
        itemAddNewScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rootView = getActivity().findViewById(android.R.id.content);
                ImageView reportButton = (ImageView) rootView.findViewById(R.id.report_button);
                reportButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.next_screenshoot, null));
                ((DialogFragment) getParentFragment()).dismiss();
            }
        });

        itemsContainer.addView(itemAddNewScreenshot);
    }

    private void initReport() {
        Report report = DebuggIt.getInstance().getReport();
        initTitle(report);
        initButtonsState(report);
    }

    private void initTitle(Report report) {
        if (!report.getTitle().isEmpty()) {
            bugTitle.setText(report.getTitle());
            bugTitle.setSelection(report.getTitle().length());
        }
    }

    private void initButtonsState(Report report) {
        initKindButtonState(report);
        initPriorityButtonState(report);
    }

    private void initPriorityButtonState(Report report) {
        if (!report.getPriority().isEmpty()) {
            priorityButtons[getSelectedPriorityButtonIndex(report)].setSelected(true);
        } else {
            priorityButtons[PRIORITY_MEDIUM_INDEX].setSelected(true);
            report.setPriority(Constants.BitBucket.PRIORITY_MAJOR);
        }
    }

    private void initKindButtonState(Report report) {
        if (!report.getKind().isEmpty()) {
            kindButtons[getSelectedKindButtonIndex(report)].setSelected(true);
        } else {
            kindButtons[KIND_BUG_INDEX].setSelected(true);
            report.setKind(Constants.KIND_BUG);
        }
    }

    private int getSelectedKindButtonIndex(Report report) {
        return report.getKind().equalsIgnoreCase(Constants.KIND_BUG) ? KIND_BUG_INDEX : KIND_ENHANCEMENT_INDEX;
    }

    private int getSelectedPriorityButtonIndex(Report report) {
        if (report.getPriority().equalsIgnoreCase(Constants.PRIORITY_LOW)) {
            return PRIORITY_LOW_INDEX;
        } else if (report.getPriority().equalsIgnoreCase(Constants.PRIORITY_MEDIUM)) {
            return PRIORITY_MEDIUM_INDEX;
        }
        return PRIORITY_HIGH_INDEX;
    }

    private void initReportContent() {
        Report report = DebuggIt.getInstance().getReport();
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
        final RelativeLayout itemScreenParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_br_screenshot, parent, false);
        ImageView itemScreenshot = (ImageView) itemScreenParent.findViewById(R.id.item_screenshot_image);
        ImageView itemScreenshotRemove = (ImageView) itemScreenParent.findViewById(R.id.item_screenshot_close);

        new DownloadImagesTask(itemScreenshot).execute(screenUrl);
        itemScreenshotRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(itemScreenParent);
                parent.invalidate();
                DebuggIt.getInstance().getReport().getScreensUrls().remove(screenUrl);
                ApiClient.postEvent(getContext(), ApiClient.EventType.SCREENSHOT_REMOVED);
            }
        });

        parent.addView(itemScreenParent);
    }

    private void addAudioMiniature(final ViewGroup parent, final String audioUrl) {
        final RelativeLayout itemAudioParent = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_br_audio, parent, false);
        ImageButton playButton = (ImageButton) itemAudioParent.findViewById(R.id.item_audio_button);
        final ImageView itemAudioRemove = (ImageView) itemAudioParent.findViewById(R.id.item_audio_close);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());

                if (v.isSelected()) {
                    playFromUrl(v, audioUrl);
                    ApiClient.postEvent(getContext(), ApiClient.EventType.AUDIO_PLAYED);
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
                DebuggIt.getInstance().getReport().getAudioUrls().remove(audioUrl);
                ApiClient.postEvent(getContext(), ApiClient.EventType.AUDIO_REMOVED);
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
        final LoadingDialog dialog = LoadingDialog.newInstance(getString(R.string.br_loading_dialog_message_play_audio));
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
                            DebuggIt.getInstance().getReport().setKind(v.getId() == R.id.kind_bug_button ? Constants.KIND_BUG : Constants.KIND_ENHANCEMENT);
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
        initTextWatchers(false);
    }

    private void initTextWatchers(boolean firstPage) {
        contentTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(bugTitle != null && s == bugTitle.getEditableText()) {
                    DebuggIt.getInstance().getReport().setTitle(bugTitle.getText().toString());
                } else if(s == stepsToReproduce.getEditableText()) {
                    DebuggIt.getInstance().getReport().setStepsToReproduce(stepsToReproduce.getText().toString());
                } else if(s == actualBehaviour.getEditableText()) {
                    DebuggIt.getInstance().getReport().setActualBehaviour(actualBehaviour.getText().toString());
                } else {
                    DebuggIt.getInstance().getReport().setExpectedBehaviour(expectedBehaviour.getText().toString());
                }
            }
        };
        if(firstPage) {
            bugTitle.addTextChangedListener(contentTextWatcher);
        } else {
            stepsToReproduce.addTextChangedListener(contentTextWatcher);
            actualBehaviour.addTextChangedListener(contentTextWatcher);
            expectedBehaviour.addTextChangedListener(contentTextWatcher);
        }
    }

    protected class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;

        protected DownloadImagesTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bmp;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp) {
                    return bmp;
                }
            } catch(Exception e) {
                bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
