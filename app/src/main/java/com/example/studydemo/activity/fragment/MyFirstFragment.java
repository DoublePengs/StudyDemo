package com.example.studydemo.activity.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studydemo.R;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/11/5
 */
public class MyFirstFragment extends Fragment {

    private Button mBtnCrash;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
    }

    private void initView(View v) {
        Log.i("myFirstFragment", "------->> initView");
        TextView tvContent = v.findViewById(R.id.tv_content);
        tvContent.setText("开始的第一个 Fragment");

        mBtnCrash = v.findViewById(R.id.btn_crash);
        mBtnCrash.setVisibility(View.GONE);

    }

    private void initData() {
        Log.i("myFirstFragment", "------->> initData");

    }
}
