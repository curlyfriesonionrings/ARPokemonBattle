package org.artoolkit.ar.ARPokemonBattle.Pokemon;

import android.util.Log;

import org.artoolkit.ar.ARPokemonBattle.Util.Constants;

import java.util.ArrayList;
import java.util.Random;

/**
 * For all stats EXCEPT HP:
 *      Stat = floor(floor((2 * B + I + E) * L / 100 + 5) * N)
 * For HP:
 *      Stat = floor((2 * B + I + E) * L / 100 + L + 10)
 *
 * B = Base, I = IV, E = EV, L = Level, N = Nature
 *
 * IV = rand(0, 31)
 * EV = floor(rand(0, 252) / 4)
 * N = 1.0 for sake of simplicity
 */
public class Pokemon {
    protected String mSpecies;
    protected int mType;
    protected int mLevel;

    /** Current values **/
    protected double mHP, mMaxHP;
    protected double mAttack, mDefense, mSpecialAttack, mSpecialDefense, mSpeed;
    /** Base stat values **/
    protected int mBaseHP, mBaseAttack, mBaseDefense, mBaseSpecialAttack, mBaseSpecialDefense, mBaseSpeed;
    /** Stat modifiers, in terms of +/- levels **/
    /** Attack, Defense, SAttack, SDefense, Speed, Acc, Eva **/
    protected int mStatModifiers[];

    protected ArrayList<Move> mMoveList;

    /** "Tag" **/
    protected int mOwner;

    protected Pokemon() {
        mStatModifiers = new int[Constants.STAT_MODIFIER_TABLE_EVASION_INDEX + 1];

        for (int i = 0; i < Constants.STAT_MODIFIER_TABLE_EVASION_INDEX + 1; i++) {
            mStatModifiers[i] = 0;
        }
    }

    protected void updateStatValues() {
        /** Base values must be set by inheriting class **/
        Random rand = new Random();
        /** Generate stats **/
        int IVs[] = {0, 0, 0, 0, 0, 0};
        int EVs[] = {0, 0, 0, 0, 0, 0};

        for (int i = 0; i < 6; ++i) {
            IVs[i] = rand.nextInt(32);
            EVs[i] = (int)Math.floor(rand.nextInt(253) / 4.d);
        }

        mMaxHP = Math.floor((2.d * mBaseHP + IVs[0] + EVs[0]) * mLevel / 100 + mLevel + 10);
        mHP = mMaxHP;
        mAttack = Math.floor((2.d * mBaseAttack + IVs[1] + EVs[1]) * mLevel / 100 + 5);
        mDefense =  Math.floor((2.d * mBaseDefense + IVs[2] + EVs[2]) * mLevel / 100 + 5);
        mSpecialAttack = Math.floor((2.d * mBaseSpecialAttack + IVs[3] + EVs[3]) * mLevel / 100 + 5);
        mSpecialDefense = Math.floor((2.d * mBaseSpecialDefense + IVs[4] + EVs[4]) * mLevel / 100 + 5);
        mSpeed = Math.floor((2.d * mBaseSpeed + IVs[5] + EVs[5]) * mLevel / 100 + 5);
    }

    public void takeDamage(int damage) {
        mHP -= damage;
        if (mHP < 0) { mHP = 0; }
    }

    public void setOwner(int tag) {
        mOwner = tag;
    }
    public void addStatModifier(int stat, int value) {
        mStatModifiers[stat] += value;
        if (mStatModifiers[stat] < -6) {
            mStatModifiers[stat] = -6;
        }
        if (mStatModifiers[stat] > 6) {
            mStatModifiers[stat] = 6;
        }
    }
    public void clearStatModifiers() {
        for (int i = 0; i < Constants.STAT_MODIFIER_TABLE_EVASION_INDEX + 1; i++) {
            mStatModifiers[i] = 0;
        }
    }
    public void removeNegativeStatModifiers() {
        for (int i = 0; i < Constants.STAT_MODIFIER_TABLE_EVASION_INDEX + 1; i++) {
            if (mStatModifiers[i] < 0) {
                mStatModifiers[i] = 0;
            }
        }
    }

    // FIXME: Use species for now
    public String getName() { return getSpecies(); }
    public String getSpecies() { return mSpecies; }
    public int getType() { return mType; }
    public String getTypeString() { return Constants.TYPE_STRING_TABLE[mType]; }

    public double getHP() { return mHP; }
    public double getMaxHP() { return mMaxHP; }
    public int getLevel() { return mLevel; }
    public double getAttack() {
        int modTableIndex = mStatModifiers[Constants.STAT_MODIFIER_TABLE_ATTACK_INDEX] +
                Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET;
        return mAttack * Constants.STAT_MODIFIER_TABLE[modTableIndex];
    }
    public double getDefense() {
        int modTableIndex = mStatModifiers[Constants.STAT_MODIFIER_TABLE_DEFENSE_INDEX] +
                Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET;
        return mDefense * Constants.STAT_MODIFIER_TABLE[modTableIndex];
    }
    public double getSpecialAttack() {
        int modTableIndex = mStatModifiers[Constants.STAT_MODIFIER_TABLE_SPECIAL_ATTACK_INDEX] +
                Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET;
        return mSpecialAttack * Constants.STAT_MODIFIER_TABLE[modTableIndex];
    }
    public double getSpecialDefense() {
        int modTableIndex = mStatModifiers[Constants.STAT_MODIFIER_TABLE_SPECIAL_DEFENSE_INDEX] +
                Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET;
        return mSpecialDefense * Constants.STAT_MODIFIER_TABLE[modTableIndex];
    }
    public double getSpeed() {
        int modTableIndex = mStatModifiers[Constants.STAT_MODIFIER_TABLE_SPEED_INDEX] +
                Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET;
        return mSpeed * Constants.STAT_MODIFIER_TABLE[modTableIndex];
    }
    public int getStatModifierValue(int stat) {
        return mStatModifiers[stat];
    }


    public int getBaseHP() { return mBaseHP; }
    public int getBaseAttack() { return mBaseAttack; }
    public int getBaseDefense() { return mBaseDefense; }
    public int getBaseSpecialAttack() { return mBaseSpecialAttack; }
    public int getBaseSpecialDefense() { return mBaseSpecialDefense; }
    public int getBaseSpeed() { return mBaseSpeed; }
    public Move getMoveNumber(int i) throws IndexOutOfBoundsException {
        try {
            return mMoveList.get(i);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e("Pokemon", "Move index out of bounds!", e);
            return null;
        }
    }

    public int getOwner() { return mOwner; }

    /** Return straight from the stat value table **/
    public double getAccuracy() {
        return Constants.BATTLE_STAT_MODIFIER_TABLE[
                    mStatModifiers[Constants.STAT_MODIFIER_TABLE_ACCURACY_INDEX] +
                                    Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET];
    }
    public double getEvasion() {
        return Constants.BATTLE_STAT_MODIFIER_TABLE[
                mStatModifiers[Constants.STAT_MODIFIER_TABLE_EVASION_INDEX] +
                        Constants.STAT_MODIFIER_TABLE_INDEX_OFFSET];
    }
}
