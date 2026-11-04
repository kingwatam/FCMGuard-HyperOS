package com.reed.fcmguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!SettingsGuard.isProtectionEnabled(context)) return;

        String action = intent.getAction();
        if (action == null ||
                (!action.equals(Intent.ACTION_BOOT_COMPLETED) &&
                        !action.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) &&
                        !action.equals(Intent.ACTION_MY_PACKAGE_REPLACED))) {
            return;
        }

        Intent service = new Intent(context, GuardService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    SettingsGuard.usePersistentNotification(context)) {
                context.startForegroundService(service);
            } else {
                context.startService(service);
            }
        } catch (Throwable ignored) {}
    }
}
