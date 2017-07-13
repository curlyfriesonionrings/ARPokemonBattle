package org.artoolkit.ar.ARPokemonBattle.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.R;

public class PokeViewHolder extends RecyclerView.ViewHolder {

    protected ImageView mIcon;
    protected ImageView mPokeball;
    protected TextView mSpecies, mLevel;
    protected TextView mMoveList;

    public PokeViewHolder(View itemView) {
        super(itemView);

        mIcon = (ImageView) itemView.findViewById(R.id.pokemonSelectIcon);
        mPokeball = (ImageView) itemView.findViewById(R.id.pokemonSelectPokeball);
        mSpecies = (TextView) itemView.findViewById(R.id.pokemonSelectSpecies);
        mLevel = (TextView) itemView.findViewById(R.id.pokemonSelectLevel);
        mMoveList = (TextView) itemView.findViewById(R.id.pokemonSelectMoveList);
    }
}
