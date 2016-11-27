package com.ghelas.moldcellsmssender;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    final static String OP = "";
    final static String FORM_ID = "websms_main_form";
    String URL_ACTION = "http://www.moldcell.md/sendsms";


    String formBuildIdStr;
    String formIdStr;
    String captchaSidStr;
    String captchaTokenStr;
    String phoneNumberStr;
    String senderStr;
    String messageStr;
    String captchaStr;
    Map<String, String> myData = new HashMap<String, String>();
    ImageView captchaImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText phoneNr = (EditText) findViewById(R.id.telephoneNr);
        final EditText sender = (EditText) findViewById(R.id.sender);
        final EditText message = (EditText) findViewById(R.id.message);
        final EditText captcha = (EditText) findViewById(R.id.captchaEnter);
        final Button button = (Button) findViewById(R.id.button);
        captchaImg = (ImageView) findViewById(R.id.captcha);


//        ParsePageVolley();
        new MoldcellParserCaptcha().execute();


        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                phoneNumberStr = phoneNr.getText().toString();
                senderStr = sender.getText().toString();
                messageStr = message.getText().toString();
                captchaStr = captcha.getText().toString();

                myData.put("phone", phoneNumberStr);
                myData.put("name", senderStr);
                myData.put("message", messageStr);
                myData.put("captcha_response", captchaStr);

                new sendRequestAsync().execute(URL_ACTION);
                phoneNr.setText("");
                sender.setText("");
                message.setText("");
                captcha.setText("");
            }
        });
    }

    private class sendRequestAsync extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... params) {
            URL url = null;
            try {
                url = new URL(URL_ACTION);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                CookieManager cookieManager = new CookieManager();
                CookieHandler.setDefault(cookieManager);
                conn.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(setPostData());
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String setPostData() {
            String data = "";
            try {
                data = URLEncoder.encode("phone", "UTF-8")
                        + "=" + URLEncoder.encode(myData.get("phone"), "UTF-8");

            data += "&" + URLEncoder.encode("name", "UTF-8") + "="
                    + URLEncoder.encode(myData.get("name"), "UTF-8");

            data += "&" + URLEncoder.encode("message", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("message"), "UTF-8");

            data += "&" + URLEncoder.encode("captcha_sid", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("captcha_sid"), "UTF-8");

            data += "&" + URLEncoder.encode("captcha_token", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("captcha_token"), "UTF-8");

            data += "&" + URLEncoder.encode("captcha_response", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("captcha_response"), "UTF-8");

            data += "&" + URLEncoder.encode("op", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("op"), "UTF-8");

            data += "&" + URLEncoder.encode("form_build_id", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("form_build_id"), "UTF-8");

            data += "&" + URLEncoder.encode("form_id", "UTF-8")
                    + "=" + URLEncoder.encode(myData.get("form_id"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(data + "post data");
            return data;
        }
        @Override
        protected void onPostExecute(Void result) {

            new MoldcellParserCaptcha().execute();

        }
    }


    private class MoldcellParserCaptcha extends AsyncTask<Void, Void, Void> {


        Bitmap bitmap;

        @Override
        protected Void doInBackground(Void... parameters) {
            try {

                // Connect to the web site
                Connection conn = Jsoup.connect(URL_ACTION);
                Document document = conn.get();
                // Using Elements to get the Meta data
                document.setBaseUri("http://www.moldcell.md");

                Elements captcha = document
                        .select("div[class=captcha] img[src]");
                // Locate the content attribute

                String imgSrc = captcha.attr("abs:src");
                URL newurl = new URL(imgSrc);
                bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());

                Elements formBuildId = document.select("form[id=websms-main-form] input[name=form_build_id]");
                formBuildIdStr = formBuildId.attr("value");
                System.out.println(formBuildIdStr + " formBuildid");

                Elements formId = document.select("form[id=websms-main-form] input[name=form_id]");
                formIdStr = formId.attr("value");
                System.out.println(formIdStr + " Formid");

                Elements captchaSid = document.select("div[class=captcha] input[name=captcha_sid]");
                captchaSidStr = captchaSid.attr("value");
                System.out.println(captchaSidStr + "captcha sid");

                Elements captchaToken = document.select("div[class=captcha] input[name=captcha_token]");
                captchaTokenStr = captchaToken.attr("value");
                System.out.println(captchaTokenStr + "captcha token");

                myData.put("captcha_sid", captchaSidStr);
                myData.put("captcha_token", captchaTokenStr);
                myData.put("form_build_id", formBuildIdStr);
                myData.put("form_id", FORM_ID);
                myData.put("op", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // Set downloaded image into ImageView
            captchaImg.setImageBitmap(bitmap);



        }
    }



}
