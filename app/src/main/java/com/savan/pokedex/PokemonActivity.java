package com.savan.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class PokemonActivity extends AppCompatActivity {

    private ImageView pokemonImage;
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView pokemonType1;
    private TextView pokemonType2;
    private Button catchButton;

    private ProgressDialog mProgressDialog;
    private String url;

    private int pokemonId;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        url             = getIntent().getStringExtra("url");
        pokemonImage    = findViewById(R.id.pokemon_image);
        nameTextView    = findViewById(R.id.pokemon_name);
        numberTextView  = findViewById(R.id.pokemon_number);
        pokemonType1    = findViewById(R.id.pokemon_type1);
        pokemonType2    = findViewById(R.id.pokemon_type2);
        catchButton     = findViewById(R.id.catch_button);

        requestQueue    = Volley.newRequestQueue(getApplicationContext());

        load();
    }

    public String LoadButtonState(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("Pokedex", MODE_PRIVATE);
        String buttonState = sharedPreferences.getString(key, "CATCH");
        return buttonState;
    }

    public void load(){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    pokemonId = response.getInt("id");
                    numberTextView.setText(String.format("%03d", response.getInt("id")));

                    JSONArray types = response.getJSONArray("types");
                    for(int i=0; i<types.length(); i++){
                        JSONObject typeEntry = types.getJSONObject(i);
                        JSONObject type = typeEntry.getJSONObject("type");
                        int slot = typeEntry.getInt("slot");
                        String typeName = type.getString("name");
                        if(slot == 1){
                            pokemonType1.setText(typeName);
                        }

                        if(slot == 2){
                            pokemonType2.setText(typeName);
                        }
                    }

                    // Setting the button state...
                    String buttonText = LoadButtonState((String)numberTextView.getText());
                    catchButton.setText(buttonText);

                    // Downloading the image from sprites...
                    JSONObject spriteObject = response.getJSONObject("sprites");
                    String url = (String) spriteObject.getString("front_default");
                    int SDK_INT = android.os.Build.VERSION.SDK_INT;
                    if (SDK_INT > 8)
                    {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                .permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                    }
                    new DownloadImage().execute(new String[]{url});

                } catch (JSONException e) {
                    Log.e("Pokemon App", "Pokemon Json Error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Pokemon App", "Pokemon Data Error");
            }
        });

        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        String buttonText = catchButton.getText().toString();
        if(buttonText.equals("CATCH")){
            buttonText = "RELEASE";
            catchButton.setText(buttonText);
        }
        else{
            buttonText = "CATCH";
            catchButton.setText(buttonText);
        }
        SaveButtonState((String)numberTextView.getText(), buttonText);
    }

    public void SaveButtonState(String key, String bState){
        SharedPreferences sharedPreferences = getSharedPreferences("Pokedex", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(key, bState);
        edit.commit();
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... URL) {
            String imageURL = URL[0];
            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap result) {
            if(result != null){
                int width = result.getWidth();
                int height = result.getHeight();

                int imageViewWidth = pokemonImage.getWidth();
                int imageViewHeight = pokemonImage.getHeight();

                int newWidth = imageViewWidth;
                int newHeight = (int) Math.floor((double) height *((double) newWidth / (double) height));

                Bitmap resized = Bitmap.createScaledBitmap (result, newWidth, newHeight, false) ;

                // Set the bitmap into ImageView
                pokemonImage.setImageBitmap(resized);
            }
        }
    }
}
