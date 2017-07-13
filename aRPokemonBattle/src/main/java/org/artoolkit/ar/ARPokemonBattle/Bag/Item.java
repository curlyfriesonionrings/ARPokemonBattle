package org.artoolkit.ar.ARPokemonBattle.Bag;

import android.os.Parcel;
import android.os.Parcelable;

import org.artoolkit.ar.ARPokemonBattle.Pokemon.Pokemon;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;
import org.artoolkit.ar.ARPokemonBattle.Util.ItemList;

public abstract class Item implements Parcelable {
    // To double an item as a view model for Recycler view
    protected boolean mSelected;
    protected String mName;
    protected int mQty, mID;
    protected String mDescription;
    protected String mEffectText;

    protected Item(int qty) {
        mSelected = false;
        mQty = qty;
        mID = -1;
    }

    protected Item(Parcel in) {
        mID = in.readInt();
        mSelected = in.readByte() != 0;
        mName = in.readString(); mQty = in.readInt();
        mDescription = in.readString();
        mEffectText = in.readString();
    }

    protected Item() {
        this(0);
    }

    public void reduceQuantity(int qty) {
        mQty -= qty;
        if (mQty < 0) {
            mQty = 0;
        }
    }
    public void addQuantity(int qty) {
        mQty += qty;
    }

    public void useItem(Pokemon tgt) {
        if (--mQty < 0) { mQty = 0; }
        applyEffect(tgt);
    }

    public abstract void applyEffect(Pokemon target);

    public String getName() { return mName; }
    public String getDescription() { return mDescription; }
    public String getEffectText() { return mEffectText; }
    public int getQuantity() { return mQty; }
    public int getItemID() { return mID; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Item other = (Item) obj;
        return mName.equals(other.getName());
    }

    public void setSelected(boolean v) { mSelected = v; }
    public boolean isSelected() { return mSelected; }

    /**************************/
    /***** PARCEL METHODS *****/
    /**************************/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mID);
        out.writeByte((byte) (mSelected ? 1 : 0));

        out.writeString(mName);
        out.writeInt(mQty);
        out.writeString(mDescription);
        out.writeString(mEffectText);
    }

    public static final Parcelable.Creator<Item> CREATOR
            = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            int id = in.readInt();
            switch (id) {
                case GameDefinitions.ITEM_ID_POKEBALL:
                    return new ItemList.Pokeball(in);
                case GameDefinitions.ITEM_ID_POTION:
                    return new ItemList.Potion(in);
                case GameDefinitions.ITEM_ID_WHITEHERB:
                    return new ItemList.WhiteHerb(in);
                default:
                    return null;
            }
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}
