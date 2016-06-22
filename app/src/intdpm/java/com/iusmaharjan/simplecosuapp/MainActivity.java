package com.iusmaharjan.simplecosuapp;

import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private DevicePolicyManager devicePolicyManager;

    private ComponentName mAdminComponentName;

    private static final String LOCK_KEY = "locked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get Device Policy Service
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        ButterKnife.bind(this);

        mAdminComponentName = DeviceAdminReceiver.getComponentName(this);

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(LOCK_KEY, false)) {
            lock();
        }

    }

    /**
     * Pins the app when the lock button is pressed
     */
    @OnClick(R.id.lock_button)
    public void lock() {

        if(devicePolicyManager.isDeviceOwnerApp(getPackageName())){
            // Set default policies
            Timber.d("Setting default COSU policies");
            setDefaultCosuPolicies(true);
        } else {
            // Do nothing
            Timber.d("This app is not the device owner");
            Toast.makeText(getApplicationContext(),
                    "This app is not the device owner",Toast.LENGTH_SHORT)
                    .show();
        }

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
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(LOCK_KEY, true).apply();
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
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(LOCK_KEY, true).apply();
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

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(LOCK_KEY, false).apply();

        Timber.d("resetting default COSU policies");
        setDefaultCosuPolicies(false);

    }

    private void setDefaultCosuPolicies(boolean active){

        // Set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // Disable keyguard and status bar
        devicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        devicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // Enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // Set system update policy
        if (active){
            devicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            devicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    null);
        }

        // set this Activity as a lock task package
        devicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            devicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(
                            getPackageName(), MainActivity.class.getName()));
        } else {
            devicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
        }
    }

    private void setUserRestriction(String restriction, boolean disallow){
        if (disallow) {
            devicePolicyManager.addUserRestriction(mAdminComponentName,
                    restriction);
        } else {
            devicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    private void enableStayOnWhilePluggedIn(boolean enabled){
        if (enabled) {
            devicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            devicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );
        }
    }
}
