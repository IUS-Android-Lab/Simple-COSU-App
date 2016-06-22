package com.iusmaharjan.simplecosuapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    /**
     * Pins the app when the lock button is pressed
     */
    @OnClick(R.id.lock_button)
    public void lock() {
        Timber.d("Pinning the app");
        this.startLockTask();
    }

    /**
     * Unpins the app when the lock button is pressed
     */
    @OnClick(R.id.unlock_button)
    public void unlock() {
        Timber.d("Unpinning the app");
        this.stopLockTask();
    }
}
