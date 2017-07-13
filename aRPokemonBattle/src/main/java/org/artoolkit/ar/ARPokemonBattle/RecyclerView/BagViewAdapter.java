package org.artoolkit.ar.ARPokemonBattle.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.R;

import java.util.ArrayList;

public class BagViewAdapter extends RecyclerView.Adapter<BagViewHolder> {

    private ArrayList<Item> mDataset;

    public BagViewAdapter(ArrayList<Item> list) {
        mDataset = list;
    }

    @Override
    public BagViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_bag_item, viewGroup, false);
        return new BagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BagViewHolder itemViewHolder, int i) {
        Item item = mDataset.get(i);

        if (!item.isSelected()) {
            itemViewHolder.mSelectCursor.setVisibility(View.INVISIBLE);
        }
        else {
            itemViewHolder.mSelectCursor.setVisibility(View.VISIBLE);
        }
        itemViewHolder.mName.setText(item.getName());
        String q = "x " + item.getQuantity();
        itemViewHolder.mQty.setText(q);
    }

    @Override
    public int getItemCount() { return mDataset.size(); }

    public Item get(int position) {
        return mDataset.get(position);
    }

    public ArrayList<Item> getDataset() { return mDataset; }
}