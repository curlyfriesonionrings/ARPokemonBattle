package org.artoolkit.ar.ARPokemonBattle.Util;

public interface Constants {
    /** Move modifier enums (Growl, Tail Whip, Swords Dance, etc) **/
    int MOVE_MODIFIER_ATK_UP_SELF = 0;
    int MOVE_MODIFIER_ATK_UP_OPP = 1;
    int MOVE_MODIFIER_ATK_DOWN_SELF = 2;
    int MOVE_MODIFIER_ATK_DOWN_OPP = 3;
    int MOVE_MODIFIER_DEF_UP_SELF = 4;
    int MOVE_MODIFIER_DEF_UP_OPP = 5;
    int MOVE_MODIFIER_DEF_DOWN_SELF = 6;
    int MOVE_MODIFIER_DEF_DOWN_OPP = 7;
    int MOVE_MODIFIER_SP_ATK_UP_SELF = 8;
    int MOVE_MODIFIER_SP_ATK_UP_OPP = 9;
    int MOVE_MODIFIER_SP_ATK_DOWN_SELF = 10;
    int MOVE_MODIFIER_SP_ATK_DOWN_OPP = 11;
    int MOVE_MODIFIER_SP_DEF_UP_SELF = 12;
    int MOVE_MODIFIER_SP_DEF_UP_OPP = 13;
    int MOVE_MODIFIER_SP_DEF_DOWN_SELF = 14;
    int MOVE_MODIFIER_SP_DEF_DOWN_OPP = 15;
    int MOVE_MODIFIER_SPD_UP_SELF = 16;
    int MOVE_MODIFIER_SPD_UP_OPP = 17;
    int MOVE_MODIFIER_SPD_DOWN_SELF = 18;
    int MOVE_MODIFIER_SPD_DOWN_OPP = 19;
    int MOVE_MODIFIER_ACC_UP_SELF = 20;
    int MOVE_MODIFIER_ACC_UP_OPP = 21;
    int MOVE_MODIFIER_ACC_DOWN_SELF = 22;
    int MOVE_MODIFIER_ACC_DOWN_OPP = 23;
    int MOVE_MODIFIER_EVA_UP_SELF = 24;
    int MOVE_MODIFIER_EVA_UP_OPP = 25;
    int MOVE_MODIFIER_EVA_DOWN_SELF = 26;
    int MOVE_MODIFIER_EVA_DOWN_OPP = 27;

    /** Stat modifier indexes for Pokemon **/
    int STAT_MODIFIER_TABLE_ATTACK_INDEX = 0;
    int STAT_MODIFIER_TABLE_DEFENSE_INDEX = 1;
    int STAT_MODIFIER_TABLE_SPECIAL_ATTACK_INDEX = 2;
    int STAT_MODIFIER_TABLE_SPECIAL_DEFENSE_INDEX = 3;
    int STAT_MODIFIER_TABLE_SPEED_INDEX = 4;
    int STAT_MODIFIER_TABLE_ACCURACY_INDEX = 5;
    int STAT_MODIFIER_TABLE_EVASION_INDEX = 6;

    /** Stat modifier table, 0 index matches -6 **/
    int STAT_MODIFIER_TABLE_INDEX_OFFSET = 6;
    double STAT_MODIFIER_TABLE[] = {
            0.25, 0.28, 0.33, 0.40, 0.50, 0.66,
            1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0
    };

    /** Battle stat modifier table (accuracy and evasion), 0 index matches -6 **/
    double BATTLE_STAT_MODIFIER_TABLE[] = {
            0.33, 0.36, 0.43, 0.5, 0.6, 0.75,
            1.0, 1.33, 1.66, 2.0, 2.33, 2.66, 3.0
    };

    /** Move categories (stat dependency) **/
    int MOVE_CATEGORY_PHYSICAL = 0;
    int MOVE_CATEGORY_SPECIAL = 1;
    int MOVE_CATEGORY_STATUS = 2;

    /********************/
    /** TYPE CONSTANTS **/
    /********************/

    /** Pokemon and move type enums **/
    int TYPE_NORMAL = 0;
    int TYPE_FIRE = 1;
    int TYPE_WATER = 2;
    int TYPE_ELECTRIC = 3;
    int TYPE_GRASS = 4;
    int TYPE_ICE = 5;
    int TYPE_FIGHTING = 6;
    int TYPE_POISON = 7;
    int TYPE_GROUND = 8;
    int TYPE_FLYING = 9;
    int TYPE_PSYCHIC = 10;
    int TYPE_BUG = 11;
    int TYPE_ROCK = 12;
    int TYPE_GHOST = 13;
    int TYPE_DRAGON = 14;
    int TYPE_DARK = 15;
    int TYPE_STEEL = 16;
    int TYPE_FAIRY = 17;

    /** Enum to String table **/
    String TYPE_STRING_TABLE[] = {
        "Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison", "Ground",
        "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy"
    };

    /** Type strength and weakness table **/
    double TYPE_INTERACTION_TABLE[][] = {
/** ATK v DEF >  Nor  Fir  Wat  Ele  Gra  Ice  Poi  Fig  Gro  Fly  Psy  Bug  Roc  Gho  Dra  Dar  Ste  Fai
/** Normal **/  {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 0.0, 1.0, 1.0, 0.5, 1.0},
/** Fire **/    {1.0, 0.5, 0.5, 1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 0.5, 1.0, 2.0, 1.0},
/** Water **/   {1.0, 2.0, 0.5, 1.0, 0.5, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 1.0, 1.0},
/** Elec **/    {1.0, 1.0, 2.0, 0.5, 0.5, 1.0, 1.0, 1.0, 0.0, 2.0, 1.0, 1.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0},
/** Grass **/   {1.0, 0.5, 2.0, 1.0, 0.5, 1.0, 1.0, 0.5, 2.0, 0.5, 1.0, 0.5, 2.0, 1.0, 0.5, 1.0, 0.5, 1.0},
/** Ice **/     {1.0, 0.5, 0.5, 1.0, 2.0, 0.5, 1.0, 1.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0},
/** Fight **/   {2.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 0.5, 0.5, 0.5, 2.0, 0.0, 1.0, 2.0, 2.0, 0.5},
/** Poison **/  {1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 0.5, 0.5, 1.0, 1.0, 1.0, 0.5, 0.5, 1.0, 1.0, 0.0, 2.0},
/** Ground **/  {1.0, 2.0, 1.0, 2.0, 0.5, 1.0, 1.0, 2.0, 1.0, 0.0, 1.0, 0.5, 2.0, 1.0, 1.0, 1.0, 2.0, 1.0},
/** Flying **/  {1.0, 1.0, 1.0, 0.5, 2.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 0.5, 1.0},
/** Psychic **/ {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 0.0, 0.5, 1.0},
/** Bug **/     {1.0, 0.5, 1.0, 1.0, 2.0, 1.0, 0.5, 0.5, 1.0, 0.5, 2.0, 1.0, 1.0, 0.5, 1.0, 2.0, 0.5, 0.5},
/** Rock **/    {1.0, 2.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 0.5, 2.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 0.5, 1.0},
/** Ghost **/   {0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 1.0},
/** Dragon **/  {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 0.5, 0.0},
/** Dark **/    {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.5, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 2.0, 1.0, 0.5, 1.0, 0.5},
/** Steel **/   {1.0, 0.5, 0.5, 0.5, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 0.5, 2.0},
/** Fairy **/   {1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 2.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 0.5, 1.0}
    };
}