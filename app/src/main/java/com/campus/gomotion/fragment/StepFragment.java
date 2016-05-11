package com.campus.gomotion.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.campus.gomotion.chart.IChart;
import com.campus.gomotion.chart.StepChart;

public class StepFragment extends Fragment {

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new StepChart().execute(getActivity());
    }


}
