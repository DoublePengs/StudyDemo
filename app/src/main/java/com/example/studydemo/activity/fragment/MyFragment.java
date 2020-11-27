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
public class MyFragment extends Fragment {

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
        Log.i("myFragment", "------->> initView");
        final TextView tvContent = v.findViewById(R.id.tv_content);
        tvContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvContent.setText(tvContent.getText().toString() + " 3秒钟过去了");
            }
        }, 3000);

        mBtnCrash = v.findViewById(R.id.btn_crash);

    }

    private void initData() {
        Log.i("myFragment", "------->> initData");

        mBtnCrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = null;
                int length = str.length();
            }
        });

//        try {
            try {
                String str = null;
                int length = str.length();
            } catch (Exception e) {
                Log.i("myFragment", "------->> first catch initData Exception e=" + e);
            }
//        } catch (Exception e) {
//            Log.i("myFragment", "------->> second catch initData Exception e=" + e);
//        }

    }
}
