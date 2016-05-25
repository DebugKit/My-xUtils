package com.slht.webservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;

@ContentView(value = R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewInject(value = R.id.editText_phone)
    private EditText editText_phone;
    @ViewInject(value = R.id.textView_address)
    private TextView textView_address;
    //命名空间
    private String nameSpace = "http://WebXml.com.cn/";
    //调用的方法名
    private String methodName = "getMobileCodeInfo";
    //EndPoint
    private String endPoint = "http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?wsdl";
    //SOAP Action
    private String soapAction = "http://WebXml.com.cn/getMobileCodeInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//        SoapObject rpc = new SoapObject();
        x.view().inject(this);
    }

    @Event(value = R.id.button_query, type = View.OnClickListener.class)
    private void onClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.指定WebService的命名空间和调用的方法名
                SoapObject rpc = new SoapObject(nameSpace, methodName);
                String mobile = editText_phone.getText().toString();
                //`2.设置调用WebService接口需要传入的两个参数
                rpc.addProperty("mobileCode", mobile);
                rpc.addProperty("userID", "");

                //3.生成调用WebService方法的SOAP请求信息，并指定SOAP版本
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
                envelope.bodyOut = rpc;
                envelope.dotNet = true;//是指是否调用dotNet开发的WebService
                HttpTransportSE transportSE = new HttpTransportSE(endPoint);
                try {
                    transportSE.call(soapAction, envelope);//4.调用webService
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

                //获取返回的数据
                SoapObject object = (SoapObject) envelope.bodyIn;
                //获取返回的结果
                final String result = object.getProperty(0).toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView_address.setText(result);
                    }
                });
            }
        }).start();


        SoapObject rpc = new SoapObject(nameSpace,methodName);
        rpc.addProperty("mobileCode","");
        rpc.addProperty("userId","");
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.bodyOut = rpc;

        HttpTransportSE transportSE = new HttpTransportSE(endPoint);
        try {
            transportSE.call(soapAction,envelope);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        SoapObject object = (SoapObject) envelope.bodyIn;
        object.getProperty(0);
    }
}
