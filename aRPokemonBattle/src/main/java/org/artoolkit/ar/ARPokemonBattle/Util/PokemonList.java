package org.artoolkit.ar.ARPokemonBattle.Util;

import android.util.Log;

import org.artoolkit.ar.ARPokemonBattle.Pokemon.Pokemon;
import org.artoolkit.ar.ARPokemonBattle.PokemonSelectActivity;

import java.util.ArrayList;

public final class PokemonList {

    public static final class Pikachu extends Pokemon {
        public Pikachu() {
            mSpecies = "Pikachu";
            mType = Constants.TYPE_ELECTRIC;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 35;
            mBaseAttack = 55;
            mBaseDefense = 40;
            mBaseSpecialAttack = 50;
            mBaseSpecialDefense = 50;
            mBaseSpeed = 90;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.TailWhip());
            mMoveList.add(new MoveList.ThunderShock());
            mMoveList.add(new MoveList.QuickAttack());
            mMoveList.add(new MoveList.Slam());

            updateStatValues();
        }
    }

    public static final class Bulbasaur extends Pokemon {
        public Bulbasaur() {
            mSpecies = "Bulbasaur";
            mType = Constants.TYPE_GRASS;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 45;
            mBaseAttack = 49;
            mBaseDefense = 49;
            mBaseSpecialAttack = 65;
            mBaseSpecialDefense = 65;
            mBaseSpeed = 45;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.Growl());
            mMoveList.add(new MoveList.Tackle());
            mMoveList.add(new MoveList.VineWhip());
            mMoveList.add(new MoveList.RazorLeaf());

            updateStatValues();
        }
    }

    public static final class Squirtle extends Pokemon {
        public Squirtle() {
            mSpecies = "Squirtle";
            mType = Constants.TYPE_WATER;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 44;
            mBaseAttack = 48;
            mBaseDefense = 65;
            mBaseSpecialAttack = 50;
            mBaseSpecialDefense = 64;
            mBaseSpeed = 43;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.Tackle());
            mMoveList.add(new MoveList.TailWhip());
            mMoveList.add(new MoveList.WaterGun());
            mMoveList.add(new MoveList.Withdraw());

            updateStatValues();
        }
    }

    public static final class Charmander extends Pokemon {
        public Charmander() {
            mSpecies = "Charmander";
            mType = Constants.TYPE_FIRE;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 39;
            mBaseAttack = 52;
            mBaseDefense = 43;
            mBaseSpecialAttack = 60;
            mBaseSpecialDefense = 50;
            mBaseSpeed = 65;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.Growl());
            mMoveList.add(new MoveList.Cut());
            mMoveList.add(new MoveList.Ember());
            mMoveList.add(new MoveList.Smokescreen());

            updateStatValues();
        }
    }

    public static final class Eevee extends Pokemon {
        public Eevee() {
            mSpecies = "Eevee";
            mType = Constants.TYPE_NORMAL;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 55;
            mBaseAttack = 55;
            mBaseDefense = 50;
            mBaseSpecialAttack = 45;
            mBaseSpecialDefense = 65;
            mBaseSpeed = 55;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.Tackle());
            mMoveList.add(new MoveList.SandAttack());
            mMoveList.add(new MoveList.Swift());
            mMoveList.add(new MoveList.QuickAttack());

            updateStatValues();
        }
    }

    public static final class Meowth extends Pokemon {
        public Meowth() {
            mSpecies = "Meowth";
            mType = Constants.TYPE_NORMAL;
            mLevel = GameDefinitions.POKEMON_DEFAULT_LEVEL;

            mBaseHP = 40;
            mBaseAttack = 45;
            mBaseDefense = 35;
            mBaseSpecialAttack = 40;
            mBaseSpecialDefense = 40;
            mBaseSpeed = 90;

            mMoveList = new ArrayList<>();
            mMoveList.add(new MoveList.Growl());
            mMoveList.add(new MoveList.Scratch());
            mMoveList.add(new MoveList.Bite());
            mMoveList.add(new MoveList.Screech());

            updateStatValues();
        }
    }
}