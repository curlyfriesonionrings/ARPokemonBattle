package org.artoolkit.ar.ARPokemonBattle.Pokemon;

import android.os.Parcel;
import android.os.Parcelable;

import org.artoolkit.ar.ARPokemonBattle.Util.Constants;

public class Move implements Parcelable {
    protected String mName;
    protected int mType;
    protected int mPP, mMaxPP;
    protected double mBasePower, mAccuracy;
    protected int mCategory;

    // For net code
    protected int mMoveIndex;

    public Move() {

    }

    public Move(Parcel in) {
        mName = in.readString();
        mType = in.readInt();
        mPP = in.readInt(); mMaxPP = in.readInt(); mBasePower = in.readDouble();
        mAccuracy = in.readDouble();

        mCategory = in.readInt();
        mMoveIndex = in.readInt();
    }

    /** Status modifiers **/
    protected boolean mModifier;
    protected int mModifierType, mModifierValue;

    /** Used for attack that have secondary effects **/
    protected double mModifierAccuracy;
    
    public void reducePP() {
        if (--mPP < 0) { mPP = 0; }
    }

    public String getName() { return mName; }
    public int getType() { return mType; }
    public String getTypeString() { return Constants.TYPE_STRING_TABLE[mType]; }
    public int getCategory() { return mCategory; }
    public int getPP() { return mPP; }
    public int getMaxPP() { return mMaxPP; }
    public double getBasePower() { return mBasePower; }
    public double getAccuracy() { return mAccuracy; }
    public boolean isModifier() { return mModifier; }
    public double getModifierAccuracy() { return mModifierAccuracy; }
    public int getModifierType() { return mModifierType; }
    public int getModifierValue() { return mModifierValue; }
    public int getMoveIndex() { return mMoveIndex; }

    /**************************/
    /***** PARCEL METHODS *****/
    /**************************/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeInt(mType);
        out.writeInt(mPP); out.writeInt(mMaxPP); out.writeDouble(mBasePower);

        out.writeDouble(mAccuracy);
        out.writeInt(mCategory);
        out.writeInt(mMoveIndex);
    }

    public static final Parcelable.Creator<Move> CREATOR
            = new Parcelable.Creator<Move>() {
        public Move createFromParcel(Parcel in) {
            return new Move(in);
        }

        public Move[] newArray(int size) {
            return new Move[size];
        }
    };
}
