package com.itant.music.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.miekir.common.log.L;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class NotificationUtils {

    /**
     * 判断是否有通知权限，没有则申请
     * @param activity 上下文
     */
    public static Dialog judgeNotificationPermission(Activity activity) {
        if (!isNotificationEnabled(activity)) {
            return requestNotificationPermission(activity);
        }
        return null;
    }

    /**
     * @param activity 上下文
     * @return 判断是否有通知权限
     */
    public static boolean isNotificationEnabled(Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return NotificationManagerCompat.from(activity).areNotificationsEnabled();
        }

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = activity.getApplicationInfo();
        String pkg = activity.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 弹出对话框申请通知权限
     * @param activity 上下文
     * @return 对话框
     */
    public static Dialog requestNotificationPermission(Activity activity) {
        AlertDialog notificationDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle("温馨提示").setMessage("要想在通知栏控制音乐播放，请先开启本应用的通知权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //跳转设置界面
                        jumpSettingForPermission(activity);
                        //activity.moveTaskToBack(true);
                        dialog.cancel();
                        //AppUtils.exitApp();
                    }
                }).setCancelable(false).create();
        notificationDialog.setCanceledOnTouchOutside(false);
        notificationDialog.show();
        return notificationDialog;
    }

    /**
     * 权限被多次拒绝后跳转设置界面
     * @param activity 上下文
     */
    public static void jumpSettingForPermission(Activity activity) {
        Intent localIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));

        try {
            activity.startActivity(localIntent);
        } catch (Exception e) {
            ToastUtils.showShort("请到应用设置界面授予权限");
            L.e(e.getMessage());
        }
    }
}
