package com.iusmaharjan.simplecosuapp;

import android.app.admin.DevicePolicyManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager devicePolicyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get Device Policy Service
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        ButterKnife.bind(this);
    }

    /**
     * Pins the app when the lock button is pressed
     */
    @OnClick(R.id.lock_button)
    public void lock() {
        if(devicePolicyManager.isLockTaskPermitted(getPackageName())) {
            /**
             * If lock task is permitted, we can lock the task. We can use an external DPM like
             * TestDPC provided by Google to manage lock task list.
             *
             * If the lock is obtained using TestDPC, features like status bar, home button, recent
             * apps, etc is disabled.
             *
             * To unlock we can programatically call stopLockTask() when users taps a button. But
             * in practice this should be done using a separate admin console or Confirm Credential.
             *
             * For API 23+ you can check if the lock is active by checking if
             * activityManager.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE
             */
            Timber.d("pinning and locking the app");
            this.startLockTask();
        } else {
            /**
             * The device is not whitelisted.
             */
            Toast.makeText(this, "The app is not whitelisted for lock", Toast.LENGTH_SHORT).show();
            Timber.d("The app is not whitelisted for lock task");


            /**
             * We can still pin the app but it will not be locked.
             *
             * We can simply unlock by pressing recent and back button together.
             *
             * Unlocking by calling stopLockTask() on button click can be achieved as well.
             */
            Timber.d("just pinning the app");
            this.startLockTask();
        }

    }

    /**
     * Unpins the app when the lock button is pressed
     *
     * For API 23+ you can check if the lock is active by checking if
     * activityManager.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE
     */
    @OnClick(R.id.unlock_button)
    public void unlock() {
        Timber.d("unlock/unpin the app");
        this.stopLockTask();
    }
}
