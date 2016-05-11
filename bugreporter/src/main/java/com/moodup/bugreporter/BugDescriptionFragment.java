package com.moodup.bugreporter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BugDescriptionFragment extends Fragment {

    public static BugDescriptionFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        BugDescriptionFragment fragment = new BugDescriptionFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initViews(inflater, container, getArguments().getInt("position"));
    }

    private View initViews(LayoutInflater inflater, ViewGroup container, int position) {
        View view;
        switch(position) {
            case 0:
                view = inflater.inflate(R.layout.fragment_bug_description_page1, container, false);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_bug_description_page2, container, false);
                break;
        }

        return view;
    }
}
