package com.qlc.qlocation

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import org.greenrobot.eventbus.EventBus

/**
 * Created by zl on 2019/2/27
 */
class BackGroundService : Service() {
    var notification: NotificationCompat.Builder? = null
    private var mContext: Context? = null
    private val bgmediaPlayer: MediaPlayer? = null
    private var isrun = true
    private var locationManager: LocationManager? = null
    private var pm: PowerManager? = null
    private var wakeLock: WakeLock? = null
    override fun onCreate() {
        super.onCreate()
        //创建LocationManger对象(LocationMangager，位置管理器。要想操作定位相关设备，必须先定义个LocationManager)
        locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //利用Criteria选择最优的位置服务
        val criteria = Criteria()
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.accuracy = Criteria.ACCURACY_FINE
        //设置是否需要海拔信息
        criteria.isAltitudeRequired = true
        //设置是否需要方位信息
        criteria.isBearingRequired = false
        // 设置是否允许运营商收费
        criteria.isCostAllowed = true
        // 设置对电源的需求
        criteria.powerRequirement = Criteria.POWER_LOW
        //获取最符合要求的provider
        val provider = locationManager!!.getBestProvider(criteria, true)
        //绑定监听，有4个参数
//参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
//参数2，位置信息更新周期，单位毫秒
//参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
//参数4，监听
//备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
        if (ActivityCompat.checkSelfPermission(
                AppConfig.instance,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager!!.requestLocationUpdates(provider, 5000, 0f, locationListener) // 2000,10
    }

    /**
     * 实现一个位置变化的监听器
     */
    private val locationListener: LocationListener? = object : LocationListener {
        override fun onLocationChanged(location: Location) { // TODO Auto-generated method stub
            /**
             * 此处实现定位上传功能
             */
            KLog.i("定位方式：" + location!!.getProvider())
            KLog.i("纬度：" + location.getLatitude())
            KLog.i("经度：" + location.getLongitude())
            KLog.i("海拔：" + location.getAltitude())
            KLog.i("时间：" + location.getTime())
            //toast("海拔：" + location.getAltitude())
            var sb = StringBuilder()
            sb.append("定位方式：").append(location!!.getProvider()).append("\n")
                .append("纬度：").append(location!!.getLatitude()).append("\n")
                .append("经度：").append(location!!.getLongitude()).append("\n")
                .append("海拔：").append(location!!.getAltitude()).append("\n")
                .append("时间：").append(location!!.getTime()).append("\n\n")
            EventBus.getDefault().post(LocationUpDate(sb.toString()))
        }

        // 当位置信息不可获取时
        override fun onProviderDisabled(provider: String) { // TODO Auto-generated method stub
            /**
             *
             */
        }

        override fun onProviderEnabled(provider: String) { // TODO Auto-generated method stub
        }

        override fun onStatusChanged(
            provider: String,
            status: Int,
            extras: Bundle
        ) { // TODO Auto-generated method stub
        }
    }

    override fun onStart(
        intent: Intent,
        startId: Int
    ) { // TODO Auto-generated method stub
        super.onStart(intent, startId)
        //创建PowerManager对象
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //保持cpu一直运行，不管屏幕是否黑屏
        wakeLock = pm!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning")
        wakeLock!!.acquire()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mContext = this
        //新增---------------------------------------------
        val CHANNEL_ONE_ID = "com.primedu.cn"
        val CHANNEL_ONE_NAME = "Channel One"
        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(false)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(false)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            mContext, 0,
            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        //1.通知栏占用，不清楚的看官网或者音乐类APP的效果
        notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(mContext)
                .setChannelId(CHANNEL_ONE_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        Resources.getSystem(),
                        R.mipmap.ic_launcher
                    )
                )
                .setWhen(System.currentTimeMillis())
                .setTicker(AppConfig.instance.getString(R.string.app_name))
                .setContentTitle(AppConfig.instance.getString(R.string.app_name))
                .setContentText("后台启动")
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setWhen(0)
                .setSound(null)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
        } else {
            NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        Resources.getSystem(),
                        R.mipmap.ic_launcher
                    )
                )
                .setTicker(AppConfig.instance.getString(R.string.app_name))
                .setContentTitle(AppConfig.instance.getString(R.string.app_name))
                .setContentText("后台启动")
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(0)
                .setAutoCancel(false)
                .setSound(null)
        }
        /*使用startForeground,如果id为0，那么notification将不会显示*/startForeground(
            313399,
            notification!!.build()
        )
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        isrun = false
        stopForeground(true)
        bgmediaPlayer?.release()
        stopSelf()
        // toggleGPS(false);
        if (locationListener != null) {
            locationManager!!.removeUpdates(locationListener)
        }
        wakeLock!!.release()
        super.onDestroy()
    }
}