package com.example.converter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private TextView txt_result;
    TextView txt_desc_base;
    TextView txt_desc_target;

    String currency_base;
    String currency_target;

    RadioGroup radio_group_base;
    RadioGroup radio_group_target;

    int checked_btn_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txt_result = findViewById(R.id.txt_result);

        txt_desc_base = findViewById(R.id.txt_desc_base);
        txt_desc_target = findViewById(R.id.txt_desc_convert);

        radio_group_base = findViewById(R.id.radio_group_base);
        radio_group_target = findViewById(R.id.radio_group_target);

        //Set initial values
        changeDesc(radio_group_base, "base", txt_desc_base);
        changeDesc(radio_group_target, "target", txt_desc_target);

        Button buttonConverter = findViewById(R.id.btn_converter);

        //Listener do Radio Group
        radio_group_base.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                changeDesc(radio_group_base, "base", txt_desc_base);
            }
        });
        //Listener do Radio Group
        radio_group_target.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                changeDesc(radio_group_target, "target", txt_desc_target);
            }
        });
        //Listener do Button
        buttonConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                converter();
            }
        });
    }
        private void converter() {

            EditText editField = findViewById(R.id.edit_field);
            String value = editField.getText().toString();

            if(value.isEmpty()) return;

            Runnable r = () -> {
                //Logica do que acontece em paralelo
                //Parametro na instanciação
                HttpsURLConnection conn = null;
                URL url;
                try {
                    final String API_KEY = "fca_live_hI70iLBzosDalZrsSBad2zwVIYavjnKAlRf4xqYQ&currencies";
                    url = new URL("https://api.freecurrencyapi.com/v1/latest?apikey="+ API_KEY +"=" + currency_target + "&base_currency="+currency_base);
                    conn = (HttpsURLConnection) url.openConnection();

                    //Convertendo os bits de dados(que veio da resposta da url da internet) e convertendo para String por meio do bufferedReader e StringBuilder
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String buffer;
                    while ((buffer = bufferedReader.readLine()) != null) {
                        stringBuilder.append(buffer);
                    }

                    // String data =  "{"data":{"EUR":0.161162014}}"
                    String data = stringBuilder.toString();
                    JSONObject obj = new JSONObject(data);
                    Double res = (Double) obj.getJSONObject("data").getDouble(currency_target);
                    Double resConverted = res * Double.parseDouble(value);
                    //Join da Thread da Interface(Renderização da tela) com a Thread da execução
                    runOnUiThread(()-> {

                        txt_result.setText(String.format(Locale.getDefault(), " %.2f", resConverted ));
                        txt_result.setVisibility(View.VISIBLE);
                    });

                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (conn != null) conn.disconnect();
                }

            };
            Thread thread = new Thread(r);
            thread.start();
        }

            private void changeDesc(RadioGroup radio_group, String currency, TextView txt_desc ){
            checked_btn_id = radio_group.getCheckedRadioButtonId();
            RadioButton radio_btn = (RadioButton) findViewById(checked_btn_id);
            String localCurrency;
            if(currency.equals("base")) {
                currency_base = radio_btn.getText().toString();;
                txt_desc.setText(currency_base);
                txt_desc.setVisibility(View.VISIBLE);
            }else{
                currency_target = radio_btn.getText().toString();
                txt_desc.setText(currency_target);
                txt_desc.setVisibility(View.VISIBLE);
            }
        }
}
