package com.iusmaharjan.simplecosuapp;

import android.app.Application;

import timber.log.Timber;

/**
 * The main application class
 */
public class SimpleCUSOApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Plant new debug tree for timber
        Timber.plant(new Timber.DebugTree());

        // Testing Timber
        Timber.d("Application created.");
    }
}
