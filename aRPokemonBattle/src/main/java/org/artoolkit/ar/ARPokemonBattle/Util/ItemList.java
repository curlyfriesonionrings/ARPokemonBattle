package org.artoolkit.ar.ARPokemonBattle.Util;

import android.os.Parcel;
import android.os.Parcelable;

import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Pokemon;

public final class ItemList {
    public static final class Pokeball extends Item {
        public Pokeball(int qty) {
            super(qty);
            mName = "Poké Ball";
            mDescription = GameDefinitions.ITEM_DESC_POKEBALL;
            mID = GameDefinitions.ITEM_ID_POKEBALL;
        }

        public Pokeball(Parcel in) {
            super(in);
        }

        public void applyEffect(Pokemon target) {
            // Does nothing right now
        }

        public static final Parcelable.Creator<Pokeball> CREATOR
                = new Parcelable.Creator<Pokeball>() {
            public Pokeball createFromParcel(Parcel in) {
                return new Pokeball(in);
            }

            public Pokeball[] newArray(int size) {
                return new Pokeball[size];
            }
        };
    }

    public static final class Potion extends Item {
        public Potion(int qty) {
            super(qty);
            mName = "Potion";
            mDescription = GameDefinitions.ITEM_DESC_POTION;
            mID = GameDefinitions.ITEM_ID_POTION;
            mEffectText = GameDefinitions.ITEM_EFFECT_POTION;
        }

        public Potion(Parcel in) {
            super(in);
        }

        public void applyEffect(Pokemon target) {
            // Use negative damage to heal
            target.takeDamage(-GameDefinitions.POTION_HEAL_AMOUNT);
        }

        public static final Parcelable.Creator<Potion> CREATOR
                = new Parcelable.Creator<Potion>() {
            public Potion createFromParcel(Parcel in) {
                return new Potion(in);
            }

            public Potion[] newArray(int size) {
                return new Potion[size];
            }
        };
    }

    public static final class WhiteHerb extends Item {
        public WhiteHerb(int qty) {
            super(qty);
            mName = "White Herb";
            mDescription = GameDefinitions.ITEM_DESC_WHITEHERB;
            mID = GameDefinitions.ITEM_ID_WHITEHERB;
            mEffectText = GameDefinitions.ITEM_EFFECT_WHITEHERB;
        }

        public WhiteHerb(Parcel in) {
            super(in);
        }

        public void applyEffect(Pokemon target) {
            target.removeNegativeStatModifiers();
        }

        public static final Parcelable.Creator<WhiteHerb> CREATOR
                = new Parcelable.Creator<WhiteHerb>() {
            public WhiteHerb createFromParcel(Parcel in) {
                return new WhiteHerb(in);
            }

            public WhiteHerb[] newArray(int size) {
                return new WhiteHerb[size];
            }
        };
    }
}
