package com.ly.circularprogressbar;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ly.circularprogressbar.widget.CircularProgressBar;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private CircularProgressBar mCircularProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mCircularProgressBar = findViewById(R.id.cpb_test_progress);
//        mCircularProgressBar.setTipsMessage("下载");
//        mCircularProgressBar.setDefColor(getResources().getColor(R.color.x_red));
//        mCircularProgressBar.setFontColor(getResources().getColor(R.color.x_yellow));
//        mCircularProgressBar.setTabColor(getResources().getColor(R.color.x_blue));
//        mCircularProgressBar.setProgressColor(getResources().getColor(R.color.colorPrimaryDark));
//        mCircularProgressBar.setProgressWidth(4);
//        mCircularProgressBar.setRectWidth(1);
//        mCircularProgressBar.setTipsFinish("傻逼");
        mCircularProgressBar.setOnClickListener(v -> {
            if (disposable != null) {
                disposable.dispose();
            }
            if (mCircularProgressBar.isInProgress()) {
                mCircularProgressBar.doPauseProgress();
            } else {
                doStartDownLoad();
            }
        });
    }

    private io.reactivex.disposables.Disposable disposable;

    private void doStartDownLoad() {
        mCircularProgressBar.doStartProgress();
        disposable = io.reactivex.Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (aLong >= 9) {
                        mCircularProgressBar.doFinishProgress();
                    } else {
                        mCircularProgressBar.setProgress((int) ((aLong + 1) * 10), 100);
                    }
                });
        mCircularProgressBar.setOnProgressListener(disposable::dispose);

    }
}
