package org.artoolkit.ar.ARPokemonBattle.Util;

public interface GameDefinitions {
    int MODE_HOST = 0;
    int MODE_CLIENT = 1;
    int MODE_LOCAL = 2;

    int GAME_STATE_ENCOUNTER = 0;
    int GAME_STATE_BATTLE = 1;
    int GAME_STATE_CONCLUSION = 2;

    int TAG_OWNER_PLAYER_1 = 0;
    int TAG_OWNER_PLAYER_2 = 1;

    int ACTION_MOVE = 0;
    int ACTION_ITEM = 1;

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // In ms
    int HP_ANIM_DUR = 1000;

    /**
     * Offsets needed to get to the proper string from the modifier value (ie -1, +2, etc)
     */
    int MESSAGE_STAT_DOWN_INDEX_OFFSET = 4;
    int MESSAGE_STAT_UP_INDEX_OFFSET = 3;
    String MESSAGE_STAT_DIRECTION_TEXT[] = {
        "won't go any lower!",
        "severely fell!",
        "harshly fell!",
        "fell!",
        "rose!",
        "rose sharply!",
        "rose drastically!",
        "won't go any higher!"
    };

    int POKEMON_DEFAULT_LEVEL = 15;

    String MODEL_PATH_BULBASAUR = "Data/models/BR_Bulbasaur.obj";
    String MODEL_PATH_SQUIRTLE = "Data/models/BR_Squirtle.obj";
    String MODEL_PATH_CHARMANDER = "Data/models/Charmander.obj";
    String MODEL_PATH_PIKACHU = "Data/models/XY_PikachuM.obj";
    String MODEL_PATH_MEOWTH = "Data/models/Meowth.obj";
    String MODEL_PATH_EEVEE = "Data/models/XY_Eevee.obj";

    // Items
    int POTION_HEAL_AMOUNT = 20;
    int ITEM_ID_POKEBALL = 0;
    int ITEM_ID_POTION = 1;
    int ITEM_ID_WHITEHERB = 2;

    String ITEM_DESC_POKEBALL = "A device for catching wild Pokémon.";
    String ITEM_DESC_POTION = "Restores the HP of one Pokémon by 20 points.";
    String ITEM_DESC_WHITEHERB = "Restores a Pokémon's lowered stats in battle.";

    String ITEM_EFFECT_POTION = " feels a bit better!";
    String ITEM_EFFECT_WHITEHERB = " feels battle-ready again!";

    // AI decisions
    // % HP before AI attempts to use potion
    double AI_POTION_HP_THRESHOLD = 0.45;
    // Change that AI uses potion goes by formula:
    //    y = 0.2775x-0.349
    // Which was obtained applying a Power trendline on the extremes [0.45, 0.3], [0.25, 0.8]
    // and [x, y] correlates to [%HP, %UseChance]
    double AI_POTION_USE_CHANCE_COEFF = 0.2775;
    double AI_POTION_USE_CHANCE_EXP = -0.349;
    // Net sum of stat modifiers before AI tries to use White Herb
    int AI_WHITE_HERB_THRESHOLD = -2;
    // The chance AI will use White Herb increases with negative stats
    double AI_WHITE_HERB_UNIT_CHANCE = 0.07;
}
