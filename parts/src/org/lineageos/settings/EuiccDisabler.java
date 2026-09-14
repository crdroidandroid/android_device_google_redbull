/*
 * Copyright (C) 2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

class EuiccDisabler {
    private static final String TAG = "GoogleParts";
    private static final String[] EUICC_DEPENDENCIES = new String[]{
        "com.google.android.gms",
        "com.google.android.gsf"
    };
    // EuiccSupportPixel: eUICC applet/firmware OTA. OtaApplication reads Gservices
    // when the process starts and dies without it, so it is unusable without GMS.
    private static final String EUICC_SUPPORT_PACKAGE = "com.google.euiccpixel";

    // EuiccGoogle: hosts the EuiccService the LPA UI binds to, and reaches the
    // eUICC over RIL rather than OMAPI, so it needs no GMS to work. It does
    // register SetupWizard hooks though (SETUP_BTS, PARTNER_CUSTOMIZATION,
    // SuwFlowControllerActivity), and SUW binds those and waits on them, which
    // wedges setup when the Google backend behind them is absent or not yet
    // provisioned. Keep it off until setup is finished.
    private static final String EUICC_LPA_PACKAGE = "com.google.android.euicc";

    private static boolean isInstalledAndEnabled(PackageManager pm, String pkgName) {
        try {
            PackageInfo info = pm.getPackageInfo(pkgName, 0);
            Log.d(TAG, "package " + pkgName + " installed, " +
                       "enabled = " + info.applicationInfo.enabled);
            return info.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "package " + pkgName + " is not installed");
            return false;
        }
    }

    private static boolean shouldDisable(PackageManager pm) {
        for (String dep : EUICC_DEPENDENCIES) {
            if (!isInstalledAndEnabled(pm, dep)) {
                // Disable if any of the dependencies are disabled
                return true;
            }
        }
        return false;
    }

    private static boolean isSetupComplete(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0) == 1;
    }

    private static void setEnabled(PackageManager pm, String pkg, boolean enabled) {
        int flag = enabled
            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        try {
            pm.setApplicationEnabledSetting(pkg, flag, 0);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "package " + pkg + " is not present");
        }
    }

    public static void enableOrDisableEuicc(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean haveGms = !shouldDisable(pm);
        boolean setupComplete = isSetupComplete(context);
        Log.d(TAG, "euicc: haveGms = " + haveGms + ", setupComplete = " + setupComplete);

        setEnabled(pm, EUICC_SUPPORT_PACKAGE, haveGms);

        // This only runs on BOOT_COMPLETED, which on a first boot precedes setup,
        // so a GMS-less device picks the LPA up on the next boot after setup.
        setEnabled(pm, EUICC_LPA_PACKAGE, haveGms || setupComplete);
    }
}
