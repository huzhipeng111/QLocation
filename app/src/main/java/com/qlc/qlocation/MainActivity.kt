package com.qlc.qlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.*
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pawegio.kandroid.toast
import com.socks.library.KLog
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermission()
        EventBus.getDefault().register(this)
    }

    var locationManager : LocationManager? = null

    val permission = object : PermissionListener {
        override fun onSucceed(requestCode: Int, grantPermissions: MutableList<String>) {
            KLog.i("权限通过")
//            initData()
            var locationService = Intent(this@MainActivity, BackGroundService::class.java)
            startForegroundService(locationService)
        }

        override fun onFailed(requestCode: Int, deniedPermissions: MutableList<String>) {
            KLog.i("权限错误")

        }

    }

    val locationListener: LocationListener = object : LocationListener {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        override fun onLocationChanged(location: Location?) {
            KLog.i("定位方式：" + location!!.getProvider())
            KLog.i("纬度：" + location.getLatitude())
            KLog.i("经度：" + location.getLongitude())
            KLog.i("海拔：" + location.getAltitude())
            KLog.i("时间：" + location.getTime())
            toast("海拔：" + location.getAltitude())
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            when (status) {
                LocationProvider.AVAILABLE -> {
                    KLog.i("AVAILABLE")
                }
                LocationProvider.OUT_OF_SERVICE -> {
                    KLog.i("OUT_OF_SERVICE")
                }
                LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                    KLog.i("TEMPORARILY_UNAVAILABLE")
                }
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        override fun onProviderEnabled(provider: String) {
            KLog.i("GPS开启时触发")
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        override fun onProviderDisabled(provider: String) {
            KLog.i("GPS禁用时触发")
        }
    }


    @SuppressLint("MissingPermission")
    fun initData() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager!!.isProviderEnabled(GPS_PROVIDER)) { // 转到手机设置界面，用户设置GPS
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }
//        // 设置选择条件
//        val criteria = Criteria()
//        criteria.accuracy = Criteria.ACCURACY_COARSE //低精度，如果设置为高精度，依然获取不了location。
//
//        criteria.isAltitudeRequired = false //不要求海拔
//
//        criteria.isBearingRequired = false //不要求方位
//
//        criteria.isCostAllowed = true //允许有花费
//
//        criteria.powerRequirement = Criteria.POWER_LOW //低功耗
//        // 下面两种方式任选一种
//        // 获取可用的位置提供器
//        val providerList = locationManager!!.getProviders(true)
//        // 获取最好的定位方式
//        val provider = locationManager!!.getBestProvider(criteria, true)
//
//        // 获取位置信息
//        val location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // 监听位置信息变化
        KLog.i("开始定位")
        locationManager!!.requestLocationUpdates(GPS_PROVIDER, 0L, 0F, locationListener)
    }

    override fun onDestroy() {
        // 移除监听器
        locationManager!!.removeUpdates(locationListener)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateLocation(locationUpDate: LocationUpDate) {
        tvContent.text = tvContent.text.toString() + locationUpDate.content
    }

    fun getPermission() {
        AndPermission.with(this)
            .requestCode(101)
            .permission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .rationale { requestCode, rationale ->

            }
            .callback(permission)
            .start()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

}
