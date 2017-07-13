package org.artoolkit.ar.ARPokemonBattle.RecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.R;

public class BagViewHolder extends RecyclerView.ViewHolder {

    protected ImageView mSelectCursor;
    protected TextView mName, mQty;

    public BagViewHolder(View itemView) {
        super(itemView);

        mSelectCursor = (ImageView) itemView.findViewById(R.id.itemSelectedCursor);
        mName = (TextView) itemView.findViewById(R.id.itemName);
        mQty = (TextView) itemView.findViewById(R.id.itemQty);
    }
}
