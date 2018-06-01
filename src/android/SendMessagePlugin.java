package com.qiyue.jia.sendmessage;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jia on 2018/5/31.
 */

public class SendMessagePlugin extends CordovaPlugin {

  //发送号码
  private  String phoneNumber;
  //发送内容
  private String message;
  //插件结果回调
  private CallbackContext callbackContext;
  //自定义BroadcastReceiver
  private SendMessagePlugin.myReceiver myReceiver;
  //自定义ACTION常量,作为广播的Intent Filter识别
  private String SENT_SMS_ACTION = "SENT_SMS_ACTION";


  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.callbackContext = callbackContext;
    if (action.equals("sendMessage")) {
      phoneNumber = args.getString(0);
      message = args.getString(1);
      initData(phoneNumber, message);
      return true;
    }
    return false;
  }

  //点击发送,获取权限
  private void initData(final String phoneNumber, final String message) {

    //获取发送短信的权限
    Acp.getInstance(cordova.getActivity()).request(new AcpOptions.Builder().setPermissions(
      Manifest.permission.SEND_SMS).build(),
      new AcpListener() {
        @Override
        public void onGranted() {
          if (phoneNumber.length() > 0) {
            //发送短信
            sendMessage(phoneNumber, message);
          }

        }

        @Override
        public void onDenied(List<String> permissions) {
          Toast.makeText(cordova.getActivity(), "权限拒绝", Toast.LENGTH_SHORT).show();
        }
      });

  }

  //静默发送短信,并监听结果
  private void sendMessage(String phoneNumber, String message) {

    Intent sent = new Intent(SENT_SMS_ACTION);
    PendingIntent sendIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, sent, 0);
    myReceiver = new myReceiver();
    cordova.getActivity().registerReceiver(myReceiver, new IntentFilter(SENT_SMS_ACTION));

    //获取短信管理器
    SmsManager smsManager = SmsManager.getDefault();
    if (message.length() > 70) {
      // 拆分短信内容（手机短信长度限制）,使用sendMultipartTextMessage用户只会接收一条
      ArrayList<String> msgs = smsManager.divideMessage(message);
      ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
      for (int i = 0; i < msgs.size(); i++) {
        sentIntents.add(sendIntent);
      }
      smsManager.sendMultipartTextMessage(phoneNumber, null, msgs, sentIntents, null);
    } else {
      smsManager.sendTextMessage(phoneNumber, null, message, sendIntent, null);
    }

  }

  //监听广播,获取发送结果
  protected class myReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context _context, Intent _intent) {

      if (getResultCode() == Activity.RESULT_OK) {

        callbackContext.success("短信发送成功");
      } else {

        callbackContext.error("短信发送失败");
      }

    }

  }

}
