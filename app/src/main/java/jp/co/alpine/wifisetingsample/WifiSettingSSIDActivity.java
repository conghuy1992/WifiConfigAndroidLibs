package jp.co.alpine.wifisetingsample;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import jp.co.alpine.wificonfiglibs.MyWifiManager;

public class WifiSettingSSIDActivity extends WifiSettingBase {
	private EditText mEditTextWifiPassword;

	@Override
	protected void setupView() {
		setContentView(R.layout.activity_wifi_setting_ssid);

		TextView ssidName = (TextView) findViewById(R.id.tv_SSID_name);
		mEditTextWifiPassword = (EditText) findViewById(R.id.edit_text_wifi_passowrd);

		final MyWifiManager.WifiData wifiInfo = getIntent().getParcelableExtra("WIFI_INFO");
		ssidName.setText(wifiInfo.ssid);

		mBtnOK = (Button) findViewById(R.id.btn_OK);
		mBtnOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wifiInfo.password = mEditTextWifiPassword.getText().toString();
				wifiInfo.security = mWifiManager.scanSsidSecurity(wifiInfo.ssid);

				connectWiFi(wifiInfo);
			}
		});
	}
}
