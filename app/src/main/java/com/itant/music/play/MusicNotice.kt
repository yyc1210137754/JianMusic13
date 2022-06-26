package com.itant.music.play

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.itant.music.R
import com.itant.music.main.detail.MusicDetailActivity
import com.itant.music.manager.PlayManager
import com.miekir.common.context.MainContext
import androidx.media.app.NotificationCompat as MediaNotificationCompat


class MusicNotice {
    /**
     * 获取点击通知栏图标后的意图
     */
    private fun getActionIntent(context: Context, controlAction: String): PendingIntent {
        val intent = Intent(context, MusicService::class.java)
        intent.action = controlAction
        return intent.let {
            PendingIntent.getService(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    /**
     * 跳转音乐详情
     */
    private fun getDetailIntent(): PendingIntent {
        return PendingIntent.getActivity(
            MainContext.getContext(),
            0,
            Intent(MainContext.getContext(), MusicDetailActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * 获取弹出的通知
     * @param context 上下文
     * @param mediaSession 使用mediaSession可以在锁屏显示歌曲控制
     */
    fun getMusicNotification(context: Context, mediaSession: MediaSessionCompat): Notification {
        val currentSong = PlayManager.currentSong

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                MusicService.CHANNEL_ID,
                "简杰音乐",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setShowBadge(false)
            channel.lightColor = ContextCompat.getColor(context, R.color.green_ka)
            // 禁用弹出式通知(heads-up)对用户造成干扰
            channel.importance = NotificationManager.IMPORTANCE_LOW
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, MusicService.CHANNEL_ID)
            .setStyle(MediaNotificationCompat.MediaStyle()
                // 设置了MediaSession后，背景颜色不一定生效，背景颜色会跟随大图标的主色来采样
                .setMediaSession(mediaSession.sessionToken)
                // 媒体风格，默认通知栏显示上一曲、播放、下一曲三个图标
                .setShowActionsInCompactView(0, 1, 2)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setAutoCancel(false)
            // 设置颜色也会让通知尽量靠顶
            .setColorized(true)
            .setColor(ContextCompat.getColor(context, R.color.white))
            .setSmallIcon(R.mipmap.ic_small)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_large))
            //.setUsesChronometer()
            //.setOngoing(false)
            //.setContentIntent

        if (currentSong != null) {
            builder.setContentTitle(currentSong.title)
                .setContentText(currentSong.artist).setContentIntent(getDetailIntent())
        } else {
            builder.setContentTitle("没有正在播放的歌曲").setContentText("")
        }
        if (PlayManager.isPlaying) {
            builder
                .addAction(R.drawable.ic_prev, "上一曲", getActionIntent(context, MusicService.KEY_ACTION_PREV))
                .addAction(R.drawable.ic_pause, "暂停", getActionIntent(context, MusicService.KEY_ACTION_PAUSE))
                .addAction(R.drawable.ic_next, "下一曲", getActionIntent(context, MusicService.KEY_ACTION_NEXT))
        } else {
            builder
                .addAction(R.drawable.ic_prev, "上一曲", getActionIntent(context, MusicService.KEY_ACTION_PREV))
                .addAction(R.drawable.ic_play, "播放", getActionIntent(context, MusicService.KEY_ACTION_PLAY_RESUME))
                .addAction(R.drawable.ic_next, "下一曲", getActionIntent(context, MusicService.KEY_ACTION_NEXT))
        }
        builder.addAction(R.drawable.ic_close, "关闭", getActionIntent(context, MusicService.KEY_ACTION_CLOSE))
        return builder.build().apply {
            // 尽量让通知栏置顶
            flags = flags or Notification.FLAG_ONGOING_EVENT
            flags = flags or Notification.FLAG_NO_CLEAR
        }
    }
}

