package com.savan.pokedex;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;

public class DownloadPokemonImage extends AsyncTask<String, Void, Bitmap> {
    private ImageView pokemonImage;

    public DownloadPokemonImage(ImageView pokemonImage) {
        this.pokemonImage = pokemonImage;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {

        try {
            URL url = new URL(strings[0]);
            System.out.println(url);
            return BitmapFactory.decodeStream(url.openStream());
        } catch (IOException e) {
            Log.e("Image Error", "Error occured while downloding pokemon image...");
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        pokemonImage.setImageBitmap(bitmap);
    }
}
