package com.github.adee42.keyboardvisibility;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;


public class KeyboardVisibilityPlugin implements FlutterPlugin, ActivityAware, StreamHandler, Application.ActivityLifecycleCallbacks, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String STREAM_CHANNEL_NAME = "github.com/adee42/flutter_keyboard_visibility";
    View mainView = null;
    EventSink eventsSink;
    boolean isVisible;
    Activity activity;


    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();

        if (mainView != null) {
			mainView.getWindowVisibleDisplayFrame(r);

			// check if the visible part of the screen is less than 85%
			// if it is then the keyboard is showing
			boolean newState = ((double)r.height() / (double)mainView.getRootView().getHeight()) < 0.85;

			if (newState != isVisible) {
				isVisible = newState;
				if (eventsSink != null) {
					eventsSink.success(isVisible ? 1 : 0);
				}
			}
		}
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        try {
            mainView = ((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
            mainView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
        catch (Exception e) {
            // do nothing
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        unregisterListener();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

        unregisterListener();
    }

    private void unregisterListener() {
        if (mainView != null) {
            mainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            mainView = null;
        }
    }

    @Override
    public void onListen(Object arguments, final EventSink eventsSink) {
        // register listener
        this.eventsSink = eventsSink;

        // is keyboard is visible at startup, let our subscriber know
        if (isVisible) {
            eventsSink.success(1);
        }
    }

    @Override
    public void onCancel(Object arguments) {
        eventsSink = null;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        final EventChannel eventChannel = new EventChannel(binding.getBinaryMessenger(), STREAM_CHANNEL_NAME);
        eventChannel.setStreamHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
      eventsSink = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
        binding.getActivity().getApplication().registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        if(activity != null) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
       activity = binding.getActivity();
       binding.getActivity().getApplication().registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onDetachedFromActivity() {
        if(activity != null) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
        activity = null;
    }
}
