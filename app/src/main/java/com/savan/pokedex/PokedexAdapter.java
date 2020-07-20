package com.savan.pokedex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {

    public static class PokedexViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout containerView;
        private TextView textView;
        PokedexViewHolder(View view){
            super(view);
            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon pokemon = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", pokemon.getUrl());
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void setText(String text){
            textView.setText(text);
        }
    }

    private List<Pokemon> filterPokemon;
    private List<Pokemon> filtered;
    private class PokemonFilter extends Filter {

        PokemonFilter(){
            filterPokemon = new ArrayList<>();
            filtered = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            int totalPokemon = pokemon.size();
            for(int i=0; i<totalPokemon; i++){
                Pokemon curr = pokemon.get(i);
                if(curr.getName().contains(constraint)){
                    filterPokemon.add(curr);
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filterPokemon;
            filterResults.count = filterPokemon.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (List<Pokemon>)results.values;
            notifyDataSetChanged();
        }
    }
    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    private List<Pokemon> pokemon = new ArrayList<>();
    private RequestQueue requestQueue;

    PokedexAdapter(Context context){
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon();
    }

    public void loadPokemon(){
        String url = "https://pokeapi.co/api/v2/pokemon?limit=800";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray results = response.getJSONArray("results");
                    for(int i=0; i<results.length(); i++){
                        JSONObject pokemonJSONData = results.getJSONObject(i);
                        Pokemon pokemonObject = new Pokemon(pokemonJSONData.getString("name"), pokemonJSONData.getString("url"));
                        pokemon.add(pokemonObject);
                    }

                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("Pokemon App", "Json Error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Pokemon App", "Pokemon List Error");
            }
        });

        requestQueue.add(request);
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false);
        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon curr = null;
        if(filtered != null){
            curr = filtered.get(position);
        }
        else{
            curr = pokemon.get(position);
        }
        if(curr != null){
            holder.setText(curr.getName());
            holder.containerView.setTag(curr);
        }
    }

    @Override
    public int getItemCount() {
        if(filtered != null){
            return filtered.size();
        }
        else{
            return pokemon.size();
        }
    }
}
