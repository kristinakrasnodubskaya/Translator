package com.example.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    final String API_KEY = "trnsl.1.1.20200215T040833Z.57629730de061aa6.b3d3952ac16507446e5aad10d5a7300df9f9a55f";
    final String API_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    final String API_URL2 = "https://translate.yandex.net/api/v1.5/tr.json/getLangs";

    EditText input, output;
    Spinner lang_from, lang_to;

    Map<String, String> choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = findViewById(R.id.output);
        input = findViewById(R.id.input);

        lang_from = findViewById(R.id.lang_from);
        lang_to = findViewById(R.id.lang_to);

        //предварительный запрос для определения языков
        String data = "ui=" + "ru" + "&key=" + API_KEY;
        ReqSuppLang req0 = new ReqSuppLang();
        req0.execute(data);


        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    try {
                        String text = input.getText().toString();
                        String txt = URLEncoder.encode(text, "UTF-8");
                        if (!txt.isEmpty()) {
                            String from = "", to = "";
                            for (Map.Entry<String, String> entry : choice.entrySet()) {
                                if (entry.getValue().equals(lang_from.getSelectedItem())) {
                                    from = entry.getKey();
                                } else if (entry.getValue().equals(lang_to.getSelectedItem())) {
                                    to = entry.getKey();
                                }
                            }
                            String direction = from + "-" + to;

                            //String data = "?text="+ txt + "&format=plain&lang=en-ru&key=" + API_KEY;
                            String data = "text=" + txt + "&format=plain&lang=" + direction + "&key=" + API_KEY;
                            ReqTranslate req = new ReqTranslate();
                            req.execute(data);
                        } else
                            Toast.makeText(getBaseContext(), "Пустое поле ввода", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                    }
                    return true;
                }
                return false;
            }
        });
    }


    class ReqTranslate extends AsyncTask<String, Void, AnswerServTransl> {
        @Override
        protected AnswerServTransl doInBackground(String... data) {
            Gson gson = new Gson();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                Log.d("data", data[0]);
                // use GET запрос в заголовке
                //url = new URL(API_URL+data[0]);
                url = new URL(API_URL);
                Log.d("url", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                // setting POST method
                urlConnection.setDoOutput(true);
                OutputStream out = urlConnection.getOutputStream();
                out.write(data[0].getBytes());

                InputStream stream = urlConnection.getInputStream();
                AnswerServTransl translation = gson.fromJson(new InputStreamReader(stream), AnswerServTransl.class);
                return translation;
            } catch (IOException e) {
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(AnswerServTransl ans) {
            super.onPostExecute(ans);
            String txt = ans.text.get(0);
            output.setText(txt);
        }
    }

    class ReqSuppLang extends AsyncTask<String, Void, AnswerServLangs> {
        @Override
        protected AnswerServLangs doInBackground(String... data) {
            Gson gson = new Gson();
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(API_URL2);
                Log.d("url", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();

                // setting POST method
                urlConnection.setDoOutput(true);
                OutputStream out = urlConnection.getOutputStream();
                out.write(data[0].getBytes());

                InputStream stream = urlConnection.getInputStream();
                AnswerServLangs supp_langs = gson.fromJson(new InputStreamReader(stream), AnswerServLangs.class);
                return supp_langs;
            } catch (IOException e) {
                return null;
            } finally {
                urlConnection.disconnect();
            }
        }

        @Override
        protected void onPostExecute(AnswerServLangs ans) {
            super.onPostExecute(ans);
            if (choice == null) {
                choice = ans.langs;
            }
            Log.d("lang", ans.langs.values().toString());
            ArrayAdapter<?> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, ans.langs.values().toArray());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            lang_from.setAdapter(adapter);
            lang_from.setSelection(70);
            lang_to.setAdapter(adapter);
            lang_to.setSelection(17);
        }
    }

}
