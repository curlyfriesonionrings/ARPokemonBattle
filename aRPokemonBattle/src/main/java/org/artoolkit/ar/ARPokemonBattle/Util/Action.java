package org.artoolkit.ar.ARPokemonBattle.Util;

import org.artoolkit.ar.ARPokemonBattle.Bag.Item;

public class Action {
    private int mPlayer;
    private int mActionCode;

    // If mActionCode is ACTION_MOVE, mExtraData is move index for player
    private int mMoveIndex;

    // If mActionCode is ACTION_ITEM, mExtraData is item ID used by player
    private Item mItem;

    public Action(int player, int code, int data) {
        mPlayer = player; mActionCode = code;
        mMoveIndex = data;
    }

    public Action(int player, int code, Item item) {
        mPlayer = player; mActionCode = code;
        mItem = item;
    }

    public int getPlayer() { return mPlayer; }
    public int getActionCode() { return mActionCode; }
    public int getMoveIndex() { return mMoveIndex; }
    public Item getItem() { return mItem; }
}
