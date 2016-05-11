package com.campus.gomotion.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.campus.gomotion.chart.EvaluateChart;

/**
 * Author: zhong.zhou
 * Date: 16/5/10
 * Email: muxin_zg@163.com
 */
public class EvaluateFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new EvaluateChart().execute(getActivity());
    }
}
