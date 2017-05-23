/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2014 Zhenghong Wang
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

package zh.wang.android.yweathergetter4a_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import zh.wang.android.yweathergetter4a.WeatherInfo;
import zh.wang.android.yweathergetter4a.YahooWeather;
import zh.wang.android.yweathergetter4a.YahooWeatherInfoListener;

public class MainActivity extends Activity implements YahooWeatherInfoListener {

    private ImageView mIvWeather0;
    private TextView mTvWeather0;
    private TextView mTvErrorMessage;
    private TextView mTvTitle;
    private EditText mEtAreaOfCity;
    private Button mBtSearch;
    private Button mBtGPS;
    private LinearLayout mCurrentWeatherInfoLayout;
    private LinearLayout mWeatherInfosLayout;

    private YahooWeather mYahooWeather = YahooWeather.getInstance(5000, true);

    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mTvTitle = (TextView) findViewById(R.id.textview_title);
        mTvWeather0 = (TextView) findViewById(R.id.textview_weather_info_0);
        mTvErrorMessage = (TextView) findViewById(R.id.textview_error_message);
        mIvWeather0 = (ImageView) findViewById(R.id.imageview_weather_info_0);
        mCurrentWeatherInfoLayout = (LinearLayout) findViewById(R.id.current_weather_info);

        mEtAreaOfCity = (EditText) findViewById(R.id.edittext_area);

        mBtSearch = (Button) findViewById(R.id.search_button);
        mBtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeatherInfosLayout.removeAllViews();
                String _location = mEtAreaOfCity.getText().toString();
                if (!TextUtils.isEmpty(_location)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtAreaOfCity.getWindowToken(), 0);
                    searchByPlaceName(_location);
                    showProgressDialog();
                } else {
                    Toast.makeText(getApplicationContext(), "location is not inputted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtGPS = (Button) findViewById(R.id.gps_button);
        mBtGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                mWeatherInfosLayout.removeAllViews();
                searchByGPS();
            }
        });

        mWeatherInfosLayout = (LinearLayout) findViewById(R.id.weather_infos);

        this.searchByGPS();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        hideProgressDialog();
        mProgressDialog = null;
        super.onDestroy();
    }

    @Override
    public void gotWeatherInfo(final WeatherInfo weatherInfo, YahooWeather.ErrorType errorType) {
        // TODO Auto-generated method stub
        hideProgressDialog();
        if (weatherInfo != null) {
            setNormalLayout();
            if (mYahooWeather.getSearchMode() == YahooWeather.SEARCH_MODE.GPS) {
                if (weatherInfo.getAddress() != null) {
                    mEtAreaOfCity.setText(YahooWeather.addressToPlaceName(weatherInfo.getAddress()));
                }
            }
            mWeatherInfosLayout.removeAllViews();
            mTvTitle.setText(weatherInfo.getTitle());
            mTvWeather0.setText("====== 當前 ======" + "\n" +
                    "日期: " + weatherInfo.getCurrentConditionDate() + "\n" +
                    "天氣: " + weatherInfo.getCurrentText() + "\n" +
                    "溫度ºC: " + weatherInfo.getCurrentTemp() + "\n" +
                    "冷風指數: " + weatherInfo.getWindChill() + "\n" +
                    "風向: " + weatherInfo.getWindDirection() + "\n" +
                    "風速: " + weatherInfo.getWindSpeed() + "\n" +
                    "濕度: " + weatherInfo.getAtmosphereHumidity() + "\n" +
                    "壓力: " + weatherInfo.getAtmospherePressure() + "\n" +
                    "能見度: " + weatherInfo.getAtmosphereVisibility()
            );
            if (weatherInfo.getCurrentConditionIcon() != null) {
                mIvWeather0.setImageBitmap(weatherInfo.getCurrentConditionIcon());
            }
            for (int i = 0; i < YahooWeather.FORECAST_INFO_MAX_SIZE; i++) {
                final LinearLayout forecastInfoLayout = (LinearLayout)
                        getLayoutInflater().inflate(R.layout.forecastinfo, null);
                final TextView tvWeather = (TextView) forecastInfoLayout.findViewById(R.id.textview_forecast_info);
                final WeatherInfo.ForecastInfo forecastInfo = weatherInfo.getForecastInfoList().get(i);
                tvWeather.setText("====== 預測 " + (i + 1) + " ======" + "\n" +
                        "日期: " + forecastInfo.getForecastDate() + "\n" +
                        "天氣: " + forecastInfo.getForecastText() + "\n" +
                        "低溫: " + forecastInfo.getForecastTempLow() + "\n" +
                        "高溫: " + forecastInfo.getForecastTempHigh() + "\n"
                );
                final ImageView ivForecast = (ImageView) forecastInfoLayout.findViewById(R.id.imageview_forecast_info);
                if (forecastInfo.getForecastConditionIcon() != null) {
                    ivForecast.setImageBitmap(forecastInfo.getForecastConditionIcon());
                }
                mWeatherInfosLayout.addView(forecastInfoLayout);
            }
        } else {
            setNoResultLayout(errorType.name());
        }
    }

    private void setNormalLayout() {
        mTvTitle.setVisibility(View.VISIBLE);
        mCurrentWeatherInfoLayout.setVisibility(View.VISIBLE);
        mWeatherInfosLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void setNoResultLayout(String errorMsg) {
        mTvTitle.setVisibility(View.INVISIBLE);
        mWeatherInfosLayout.removeAllViews();
        mWeatherInfosLayout.setVisibility(View.INVISIBLE);
        mCurrentWeatherInfoLayout.setVisibility(View.INVISIBLE);
        mTvErrorMessage.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText("Sorry, no result returned\n" + errorMsg);
        mProgressDialog.cancel();
    }

    private void searchByGPS() {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(getApplicationContext(), this);
    }

    private void searchByPlaceName(String location) {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(getApplicationContext(), location, MainActivity.this);
    }

    private void showProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    private void getCode(int code) {
        String name = "";
        switch (code) {
            case 0:
                name = "龍捲風";
                break;

            case 1:
                name = "熱帶風暴";
                break;
            case 2:
                name = "颶風";
                break;
            case 3:
                name = "強雷陣雨";
                break;
            case 4:
                name = "雷陣雨";
                break;
            case 5:
                name = "混合雨雪";
                break;
            case 6:
                name = "混合雨雪";
                break;
            case 7:
                name = " 混合雨雪 ";
                break;
            case 8:
                name = " 冰凍小雨 ";
                break;
            case 9:
                name = " 細雨 ";
                break;
            case 10:
                name = " 凍雨 ";
                break;
            case 11:
                name = " 陣雨 ";
                break;
            case 12:
                name = " 陣雨 ";
                break;
            case 13:
                name = " 飄雪 ";
                break;
            case 14:
                name = " 陣雪 ";
                break;
            case 15:
                name = " 吹雪 ";
                break;
            case 16:
                name = " 下雪 ";
                break;
            case 17:
                name = " 冰雹 ";
                break;
            case 18:
                name = " 雨雪 ";
                break;
            case 19:
                name = " 多塵 ";
                break;
            case 20:
                name = " 多霧 ";
                break;
            case 21:
                name = "陰霾";
                break;
            case 22:
                name = "多煙";
                break;
            case 23:
                name = "狂風大作";
                break;
            case 24:
                name = "有風";
                break;
            case 25:
                name = "冷";
                break;
            case 26:
                name = " 多雲 ";
                break;
            case 27:
                name = "晴間多雲（夜）";
                break;
            case 28:
                name = "晴間多雲（日）";
                break;
            case 29:
                name = "晴間多雲（夜）";
                break;
            case 30:
                name = "晴間多雲（日）";
                break;
            case 31:
                name = "清晰的（夜）";
                break;
            case 32:
                name = "晴朗";
                break;
            case 33:
                name = "晴朗（夜）";
                break;
            case 34:
                name = "晴朗（日）";
                break;
            case 35:
                name = "雨和冰雹";
                break;
            case 36:
                name = "炎熱";
                break;
            case 37:
                name = "雷陣雨";
                break;
            case 38:
                name = "零星雷陣雨";
                break;
            case 39:
                name = "零星雷陣雨";
                break;
            case 40:
                name = " 零星雷陣雨 ";
                break;
            case 41:
                name = " 大雪 ";
                break;
            case 42:
                name = " 零星陣雪 ";
                break;
            case 43:
                name = " 大雪 ";
                break;
            case 44:
                name = " 多雲 ";
                break;
            case 45:
                name = " 雷陣雨 ";
                break;
            case 46:
                name = " 陣雪 ";
                break;
            case 47:
                name = " 雷陣雨 ";
                break;
            case 3200:
                name = "資料錯誤 ";
                break;

        }

    }
}
