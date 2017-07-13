package org.artoolkit.ar.ARPokemonBattle.Util;

import android.os.Parcel;
import android.os.Parcelable;

import org.artoolkit.ar.ARPokemonBattle.Pokemon.Move;

/**
 * Statically defined moves for usable Pokemon
 */
public final class MoveList {

    /** Pikachu **/
    public static class TailWhip extends Move {
        public TailWhip() {
            mName = "Tail Whip";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 100.0;
            mModifier = true;
            mModifierAccuracy = 100.0;
            mModifierType = Constants.MOVE_MODIFIER_DEF_DOWN_OPP;
            mModifierValue = -1;
        }
        
        public TailWhip(Parcel in) {
            super(in);
        }
        
        public static final Parcelable.Creator<TailWhip> CREATOR
                = new Parcelable.Creator<TailWhip>() {
            public TailWhip createFromParcel(Parcel in) {
                return new TailWhip(in);
            }

            public TailWhip[] newArray(int size) {
                return new TailWhip[size];
            }
        };
    }

    public static class ThunderShock extends Move {
        public ThunderShock() {
            mName = "Thunder Shock";
            mType = Constants.TYPE_ELECTRIC;
            mCategory = Constants.MOVE_CATEGORY_SPECIAL;
            mPP = mMaxPP = 10;
            mBasePower = 40.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public ThunderShock(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<ThunderShock> CREATOR
                = new Parcelable.Creator<ThunderShock>() {
            public ThunderShock createFromParcel(Parcel in) {
                return new ThunderShock(in);
            }

            public ThunderShock[] newArray(int size) {
                return new ThunderShock[size];
            }
        };
    }

    /**
     * Does not add priority modifier
     */
    public static class QuickAttack extends Move {
        public QuickAttack() {
            mName = "Quick Attack";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 10;
            mBasePower = 40.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public QuickAttack(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<QuickAttack> CREATOR
                = new Parcelable.Creator<QuickAttack>() {
            public QuickAttack createFromParcel(Parcel in) {
                return new QuickAttack(in);
            }

            public QuickAttack[] newArray(int size) {
                return new QuickAttack[size];
            }
        };
    }

    public static class Slam extends Move {
        public Slam() {
            mName = "Slam";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 5;
            mBasePower = 80.0;
            mAccuracy = 75.0;
            mModifier = false;
        }
        
        public Slam(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Slam> CREATOR
                = new Parcelable.Creator<Slam>() {
            public Slam createFromParcel(Parcel in) {
                return new Slam(in);
            }

            public Slam[] newArray(int size) {
                return new Slam[size];
            }
        };
    }

    /** Bulbasaur **/
    public static class Tackle extends Move {
        public Tackle() {
            mName = "Tackle";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 10;
            mBasePower = 50.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public Tackle(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Tackle> CREATOR
                = new Parcelable.Creator<Tackle>() {
            public Tackle createFromParcel(Parcel in) {
                return new Tackle(in);
            }

            public Tackle[] newArray(int size) {
                return new Tackle[size];
            }
        };
    }

    public static class Growl extends Move {
        public Growl() {
            mName = "Growl";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 100.0;
            mModifier = true;
            mModifierAccuracy = 100.0;
            mModifierType = Constants.MOVE_MODIFIER_ATK_DOWN_OPP;
            mModifierValue = -1;
        }

        public Growl(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Growl> CREATOR
                = new Parcelable.Creator<Growl>() {
            public Growl createFromParcel(Parcel in) {
                return new Growl(in);
            }

            public Growl[] newArray(int size) {
                return new Growl[size];
            }
        };
    }

    public static class VineWhip extends Move {
        public VineWhip() {
            mName = "Vine Whip";
            mType = Constants.TYPE_GRASS;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 10;
            mBasePower = 45.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public VineWhip(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<VineWhip> CREATOR
                = new Parcelable.Creator<VineWhip>() {
            public VineWhip createFromParcel(Parcel in) {
                return new VineWhip(in);
            }

            public VineWhip[] newArray(int size) {
                return new VineWhip[size];
            }
        };
    }

    public static class RazorLeaf extends Move {
        public RazorLeaf() {
            mName = "Razor Leaf";
            mType = Constants.TYPE_GRASS;
            mCategory = Constants.MOVE_CATEGORY_SPECIAL;
            mPP = mMaxPP = 5;
            mBasePower = 55.0;
            mAccuracy = 95.0;
            mModifier = false;
        }

        public RazorLeaf(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<RazorLeaf> CREATOR
                = new Parcelable.Creator<RazorLeaf>() {
            public RazorLeaf createFromParcel(Parcel in) {
                return new RazorLeaf(in);
            }

            public RazorLeaf[] newArray(int size) {
                return new RazorLeaf[size];
            }
        };
    }

    /** Squirtle **/
    // Tackle
    // Tail Whip
    public static class WaterGun extends Move {
        public WaterGun() {
            mName = "Water Gun";
            mType = Constants.TYPE_WATER;
            mCategory = Constants.MOVE_CATEGORY_SPECIAL;
            mPP = mMaxPP = 5;
            mBasePower = 40.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public WaterGun(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<WaterGun> CREATOR
                = new Parcelable.Creator<WaterGun>() {
            public WaterGun createFromParcel(Parcel in) {
                return new WaterGun(in);
            }

            public WaterGun[] newArray(int size) {
                return new WaterGun[size];
            }
        };
    }

    public static class Withdraw extends Move {
        public Withdraw() {
            mName = "Withdraw";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 100.0;
            mModifier = true;
            mModifierAccuracy = 100.0;
            mModifierType = Constants.MOVE_MODIFIER_DEF_UP_SELF;
            mModifierValue = 1;
        }

        public Withdraw(Parcel in) { super(in); }

        public static final Parcelable.Creator<Withdraw> CREATOR
                = new Parcelable.Creator<Withdraw>() {
            public Withdraw createFromParcel(Parcel in) { return new Withdraw(in); }

            public Withdraw[] newArray(int size) { return new Withdraw[size]; }
        };
    }

    /** Charmander **/
    // Growl
    public static class Cut extends Move {
        public Cut() {
            mName = "Cut";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 10;
            mBasePower = 50.0;
            mAccuracy = 95.0;
            mModifier = false;
        }

        public Cut(Parcel in) { super(in); }

        public static final Parcelable.Creator<Cut> CREATOR
                = new Parcelable.Creator<Cut>() {
            public Cut createFromParcel(Parcel in) { return new Cut(in); }

            public Cut[] newArray(int size) { return new Cut[size]; }
        };
    }

    public static class Ember extends Move {
        public Ember() {
            mName = "Ember";
            mType = Constants.TYPE_FIRE;
            mCategory = Constants.MOVE_CATEGORY_SPECIAL;
            mPP = mMaxPP = 5;
            mBasePower = 40.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public Ember(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Ember> CREATOR
                = new Parcelable.Creator<Ember>() {
            public Ember createFromParcel(Parcel in) {
                return new Ember(in);
            }

            public Ember[] newArray(int size) {
                return new Ember[size];
            }
        };
    }

    public static class Smokescreen extends Move {
        public Smokescreen() {
            mName = "Smokescreen";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 100.0;
            mModifier = true;
            mModifierAccuracy = 100.0;
            mModifierType = Constants.MOVE_MODIFIER_ACC_DOWN_OPP;
            mModifierValue = -1;
        }

        public Smokescreen(Parcel in) { super(in); }

        public static final Parcelable.Creator<Smokescreen> CREATOR
                = new Parcelable.Creator<Smokescreen>() {
            public Smokescreen createFromParcel(Parcel in) { return new Smokescreen(in); }

            public Smokescreen[] newArray(int size) { return new Smokescreen[size]; }
        };
    }

    /** Eevee **/
    // Tackle
    public static class SandAttack extends Move {
        public SandAttack() {
            mName = "Sand Attack";
            mType = Constants.TYPE_GROUND;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 100.0;
            mModifier = true;
            mModifierAccuracy = 100.0;
            mModifierType = Constants.MOVE_MODIFIER_ACC_DOWN_OPP;
            mModifierValue = -1;
        }

        public SandAttack(Parcel in) { super(in); }

        public static final Parcelable.Creator<SandAttack> CREATOR
                = new Parcelable.Creator<SandAttack>() {
            public SandAttack createFromParcel(Parcel in) { return new SandAttack(in); }

            public SandAttack[] newArray(int size) { return new SandAttack[size]; }
        };
    }

    public static class Swift extends Move {
        public Swift() {
            mName = "Swift";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_SPECIAL;
            mPP = mMaxPP = 2;
            mBasePower = 60.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public Swift(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Swift> CREATOR
                = new Parcelable.Creator<Swift>() {
            public Swift createFromParcel(Parcel in) {
                return new Swift(in);
            }

            public Swift[] newArray(int size) {
                return new Swift[size];
            }
        };
    }

    // Quick Attack

    /** Meowth **/
    // Growl
    public static class Scratch extends Move {
        public Scratch() {
            mName = "Scratch";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 10;
            mBasePower = 40.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public Scratch(Parcel in) { super(in); }

        public static final Parcelable.Creator<Scratch> CREATOR
                = new Parcelable.Creator<Scratch>() {
            public Scratch createFromParcel(Parcel in) { return new Scratch(in); }

            public Scratch[] newArray(int size) { return new Scratch[size]; }
        };
    }
    
    public static class Bite extends Move {
        public Bite() {
            mName = "Bite";
            mType = Constants.TYPE_DARK;
            mCategory = Constants.MOVE_CATEGORY_PHYSICAL;
            mPP = mMaxPP = 5;
            mBasePower = 60.0;
            mAccuracy = 100.0;
            mModifier = false;
        }

        public Bite(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Bite> CREATOR
                = new Parcelable.Creator<Bite>() {
            public Bite createFromParcel(Parcel in) {
                return new Bite(in);
            }

            public Bite[] newArray(int size) {
                return new Bite[size];
            }
        };
    }

    public static class Screech extends Move {
        public Screech() {
            mName = "Screech";
            mType = Constants.TYPE_NORMAL;
            mCategory = Constants.MOVE_CATEGORY_STATUS;
            mPP = mMaxPP = 5;
            mBasePower = 0;
            mAccuracy = 85.0;
            mModifier = true;
            mModifierAccuracy = 85.0;
            mModifierType = Constants.MOVE_MODIFIER_DEF_DOWN_OPP;
            mModifierValue = -2;
        }

        public Screech(Parcel in) {
            super(in);
        }

        public static final Parcelable.Creator<Screech> CREATOR
                = new Parcelable.Creator<Screech>() {
            public Screech createFromParcel(Parcel in) {
                return new Screech(in);
            }

            public Screech[] newArray(int size) {
                return new Screech[size];
            }
        };
    }
}
