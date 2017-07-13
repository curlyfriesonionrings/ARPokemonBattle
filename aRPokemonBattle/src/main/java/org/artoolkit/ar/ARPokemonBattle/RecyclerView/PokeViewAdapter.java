package org.artoolkit.ar.ARPokemonBattle.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.artoolkit.ar.ARPokemonBattle.R;

import java.util.ArrayList;

public class PokeViewAdapter extends RecyclerView.Adapter<PokeViewHolder> {

    private ArrayList<PokeViewModel> mDataset;

    public PokeViewAdapter(ArrayList<PokeViewModel> list) {
        mDataset = list;
    }

    @Override
    public PokeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_pokemon_select, viewGroup, false);
        return new PokeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PokeViewHolder itemViewHolder, int i) {
        PokeViewModel pkmn = mDataset.get(i);

        itemViewHolder.mIcon.setImageResource(pkmn.getIconId());
        if (!pkmn.isSelected()) {
            itemViewHolder.mPokeball.setVisibility(View.GONE);
            itemViewHolder.mMoveList.setSelected(false);
        }
        else {
            itemViewHolder.mPokeball.setVisibility(View.VISIBLE);
            itemViewHolder.mMoveList.setSelected(true);
        }
        itemViewHolder.mSpecies.setText(pkmn.getSpecies());
        itemViewHolder.mLevel.setText(pkmn.getLevel());
        itemViewHolder.mMoveList.setText(pkmn.getMoveList());
    }

    @Override
    public int getItemCount() { return mDataset.size(); }

    public PokeViewModel get(int position) {
        return mDataset.get(position);
    }

    public ArrayList<PokeViewModel> getDataset() { return mDataset; }
}