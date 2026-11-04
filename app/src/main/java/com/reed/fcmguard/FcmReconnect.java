package com.reed.fcmguard;

import android.content.Context;
import android.content.Intent;

/**
 * Best-effort FCM/MCS reconnect kick without ADB, Shizuku or root.
 * Mirrors the heartbeat intents used by HeartbeatFixerForFCM.
 */
public final class FcmReconnect {
    private static final String ACTION_GTALK_HEARTBEAT =
            "com.google.android.intent.action.GTALK_HEARTBEAT";
    private static final String ACTION_MCS_HEARTBEAT =
            "com.google.android.intent.action.MCS_HEARTBEAT";
    private static final String ACTION_MIPUSH_REFRESH_WHITE_LIST =
            "com.xiaomi.mipush.REFRESH_WHITE_LIST_ACTION";

    private static final String[] TARGET_PACKAGES = {
            "com.google.android.gms",
            "com.google.android.gsf"
    };

    private FcmReconnect() {}

    public static boolean kick(Context context) {
        boolean sent = false;
        // Thaw first: a frozen process cannot hear the reconnect ping, so the
        // MiPush refresh broadcast (which thaws the uid before reading option)
        // must go out before any reconnect request.
        String required = SettingsGuard.getConfiguredRequiredItem(context);
        if (required == null || required.trim().isEmpty()) {
            required = "com.google.android.gms";
        }
        try {
            context.sendBroadcast(new Intent(ACTION_MIPUSH_REFRESH_WHITE_LIST)
                    .setPackage("android")
                    .putExtra("pkgName", required)
                    .putExtra("option", -1));
            sent = true;
        } catch (Throwable ignored) {
        }
        // Then heartbeat / reconnect ping
        for (String target : TARGET_PACKAGES) {
            try {
                context.sendBroadcast(new Intent(ACTION_GTALK_HEARTBEAT).setPackage(target));
                context.sendBroadcast(new Intent(ACTION_MCS_HEARTBEAT).setPackage(target));
                sent = true;
            } catch (Throwable ignored) {
            }
        }
        return sent;
    }
}
