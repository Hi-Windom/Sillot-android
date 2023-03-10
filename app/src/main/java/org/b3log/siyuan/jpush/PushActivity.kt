package org.b3log.siyuan.jpush

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import cn.jpush.android.api.JPushInterface
import com.tencent.mmkv.MMKV

//import cn.jiguang.demo.R;
//import cn.jiguang.demo.baselibrary.ClipUtils;
//import cn.jiguang.demo.baselibrary.ScreenUtils;
//import cn.jiguang.demo.baselibrary.ToastHelper;
/**
 * Copyright(c) 2020 极光
 * Description
 */
class PushActivity : AppCompatActivity(), View.OnClickListener {
    private val toggle: ToggleButton? = null
    private val tvOnline: TextView? = null
    private val tvAppKey: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootDir = MMKV.initialize(this)
        println("mmkv root: $rootDir")
        //        setContentView(R.layout.jpush_demo_activity_push);
//        ScreenUtils.setStatusBarTransparent(getWindow());
//        initView();
        registerReceiver(reciver, IntentFilter("com.jiguang.demo.message"))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(reciver)
    }

    //    private void initView() {
    //        findViewById(R.id.tv_notify).setOnClickListener(this);
    //        findViewById(R.id.tv_adv).setOnClickListener(this);
    //        findViewById(R.id.iv_info).setOnClickListener(this);
    //        findViewById(R.id.iv_back).setOnClickListener(this);
    //        toggle = findViewById(R.id.toggle);
    //        toggle.setOnClickListener(this);
    //        boolean checked = MMKV.defaultMMKV().getBoolean("PushOnline", true);
    //        toggle.setChecked(checked);
    //
    //        tvOnline = findViewById(R.id.tv_online);
    //        updateStatu();
    //        tvAppKey = findViewById(R.id.tv_appKey_desc);
    //
    //        String registrationID = JPushInterface.getRegistrationID(getApplicationContext());
    //        tvAppKey.setText(registrationID);
    //    }
    override fun onClick(v: View) {
//        int id = v.getId();
//        if (id == R.id.iv_back) {
//            onBackPressed();
//        } else if (id == R.id.toggle) {
//            updateStatu();
//            ToastHelper.showOk(getApplicationContext(), getString(R.string.toast_modify_ok));
//        } else if (id == R.id.iv_info) {
//            ClipUtils.copyText(v.getContext(), tvAppKey.getText().toString());
//        } else if (id == R.id.tv_notify) {
//
//            if (toggle.isChecked()) {
//                if (JPushInterface.isNotificationEnabled(this)==1) {
//                    ExampleUtil.buildLocalNotification(this.getApplicationContext(), getString(R.string.app_name), "这是一条测试消息。");
//                } else {
//                    pushPermissionDialog();
//                }
//            } else {
//                ToastHelper.showOther(getApplicationContext(), "不接收通知，不发送本地通知请求");
//            }
//        } else if (id == R.id.tv_adv) {
//            startActivity(new Intent(this, cn.jiguang.demo.jpush.AdvActivity.class));
//        }
    }

    private fun pushPermissionDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dv = View.inflate(this, R.layout.d_dialog_two_button, null);
//        TextView tvOk = (TextView) dv.findViewById(R.id.btn_ok);
//        TextView tvCancel = (TextView) dv.findViewById(R.id.btn_cancel);
//        final Dialog dialog = builder.create();
//        dialog.show();
//        dialog.getWindow().setContentView(dv);
//        tvCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        tvOk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                JPushInterface.goToAppNotificationSettings(PushActivity.this);
//                dialog.dismiss();
//            }
//        });
    }

    private val reciver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            View dv = View.inflate(context, R.layout.d_dialog_msg, null);
//            TextView tvOk = (TextView) dv.findViewById(R.id.btn_ok);
//            TextView msg = (TextView) dv.findViewById(R.id.msg);
//            msg.setText(intent.getStringExtra("msg"));
//            final Dialog dialog = builder.create();
//            dialog.show();
//            dialog.getWindow().setContentView(dv);
//            tvOk.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
        }
    }

    private fun updateStatu() {
        val checked = toggle!!.isChecked
        tvOnline!!.text = if (checked) "接收" else "不接收"
        if (checked) {
            JPushInterface.resumePush(this)
        } else {
            JPushInterface.stopPush(this)
        }
        MMKV.defaultMMKV().putBoolean("PushOnline", checked)
    }
}