/*
 *  ARSimpleNativeCarsActivity.java
 *  ARToolKit5
 *
 *  Disclaimer: IMPORTANT:  This Daqri software is supplied to you by Daqri
 *  LLC ("Daqri") in consideration of your agreement to the following
 *  terms, and your use, installation, modification or redistribution of
 *  this Daqri software constitutes acceptance of these terms.  If you do
 *  not agree with these terms, please do not use, install, modify or
 *  redistribute this Daqri software.
 *
 *  In consideration of your agreement to abide by the following terms, and
 *  subject to these terms, Daqri grants you a personal, non-exclusive
 *  license, under Daqri's copyrights in this original Daqri software (the
 *  "Daqri Software"), to use, reproduce, modify and redistribute the Daqri
 *  Software, with or without modifications, in source and/or binary forms;
 *  provided that if you redistribute the Daqri Software in its entirety and
 *  without modifications, you must retain this notice and the following
 *  text and disclaimers in all such redistributions of the Daqri Software.
 *  Neither the name, trademarks, service marks or logos of Daqri LLC may
 *  be used to endorse or promote products derived from the Daqri Software
 *  without specific prior written permission from Daqri.  Except as
 *  expressly stated in this notice, no other rights or licenses, express or
 *  implied, are granted by Daqri herein, including but not limited to any
 *  patent rights that may be infringed by your derivative works or by other
 *  works in which the Daqri Software may be incorporated.
 *
 *  The Daqri Software is provided by Daqri on an "AS IS" basis.  DAQRI
 *  MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 *  THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE, REGARDING THE DAQRI SOFTWARE OR ITS USE AND
 *  OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 *  IN NO EVENT SHALL DAQRI BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 *  MODIFICATION AND/OR DISTRIBUTION OF THE DAQRI SOFTWARE, HOWEVER CAUSED
 *  AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 *  STRICT LIABILITY OR OTHERWISE, EVEN IF DAQRI HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 */

package org.artoolkit.ar.ARPokemonBattle;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.ARPokemonBattle.Bag.Bag;
import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Move;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Pokemon;
import org.artoolkit.ar.ARPokemonBattle.Util.Constants;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;
import org.artoolkit.ar.ARPokemonBattle.Util.ItemList;
import org.artoolkit.ar.ARPokemonBattle.Util.PokemonList;
import org.artoolkit.ar.ARPokemonBattle.Window.BagWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MainWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MessageWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MoveListWindowFragment;
import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.Random;

/**
 * For local battle only, each "turn" is played out as it comes, unlike the networked model. One
 * "turn" consists of a single action by one side (player or opponent).
 *
 * Turn order is determined once player selects action. Whoever uses an item always goes first. If
 * both sides use an item, player goes first.
 *
 * AI's decision to use an item happens when the player selects an action. Move choice happens during
 * the simulation step, if it's the opponent's turn.
 *
 * Interacting with message dialog window transitions game states
 */
public class ARPokemonBattleActivity extends ARActivity implements
    MessageWindowFragment.OnFragmentInteractionListener,
    MainWindowFragment.OnFragmentInteractionListener,
    MoveListWindowFragment.OnFragmentInteractionListener,
    BagWindowFragment.OnFragmentInteractionListener {

    private static final String TAG = ARPokemonBattleActivity.class.getSimpleName();

    public static final String ARG_PLAYER_POKEMON = "player_pokemon";
    public static final String ARG_OPPONENT_POKEMON = "opponent_pokemon";

    private SimpleNativeRenderer simpleNativeRenderer;

    private int mGameState;
    private ArrayList<String> mMessageQueue;

    /** Battle turn list **/
    private ArrayList<Pokemon> mTurnQueue;
    private Pokemon mAttacker, mDefender;

    private int mQueuedPlayerMoveIndex;
    private Item mQueuedPlayerItem;
    private Item mAIQueuedItem;

    private Pokemon mPlayerPokemon;
    private Pokemon mOpponentPokemon;

    private TextView mPlayerHPTextView;
    private View mPlayerHPBarView, mOppHPBarView;

    /** Player bags **/
    private Bag mPlayerBag;
    private Bag mOpponentBag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mGameState = GameDefinitions.GAME_STATE_ENCOUNTER;
        mMessageQueue = new ArrayList<>();

        mTurnQueue = new ArrayList<>();

        // Set up Pokemon
        int pokeIndex = getIntent().getIntExtra(ARG_PLAYER_POKEMON, -1);
        switch (pokeIndex) {
            case PokemonSelectActivity.POKEMON_SELECT_BULBASAUR_INDEX:
                mPlayerPokemon = new PokemonList.Bulbasaur();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_CHARMANDER_INDEX:
                mPlayerPokemon = new PokemonList.Charmander();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_SQUIRTLE_INDEX:
                mPlayerPokemon = new PokemonList.Squirtle();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_PIKACHU_INDEX:
                mPlayerPokemon = new PokemonList.Pikachu();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_MEOWTH_INDEX:
                mPlayerPokemon = new PokemonList.Meowth();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_EEVEE_INDEX:
                mPlayerPokemon = new PokemonList.Eevee();
                break;
            default:
                Log.e(TAG, "Unknown Pokemon selected! Default to 0");
                mPlayerPokemon = new PokemonList.Bulbasaur();
                pokeIndex = 0;
                break;
        }
        mPlayerPokemon.setOwner(GameDefinitions.TAG_OWNER_PLAYER_1);

        int oppPokeIndex = getIntent().getIntExtra(ARG_OPPONENT_POKEMON, -1);
        switch (oppPokeIndex) {
            case PokemonSelectActivity.POKEMON_SELECT_BULBASAUR_INDEX:
                mOpponentPokemon = new PokemonList.Bulbasaur();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_CHARMANDER_INDEX:
                mOpponentPokemon = new PokemonList.Charmander();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_SQUIRTLE_INDEX:
                mOpponentPokemon = new PokemonList.Squirtle();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_PIKACHU_INDEX:
                mOpponentPokemon = new PokemonList.Pikachu();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_MEOWTH_INDEX:
                mOpponentPokemon = new PokemonList.Meowth();
                break;
            case PokemonSelectActivity.POKEMON_SELECT_EEVEE_INDEX:
                mOpponentPokemon = new PokemonList.Eevee();
                break;
            default:
                Log.e(TAG, "Unknown opponent Pokemon selected! Default to 0");
                mOpponentPokemon = new PokemonList.Bulbasaur();
                oppPokeIndex = 0;
                break;
        }
        mOpponentPokemon.setOwner(GameDefinitions.TAG_OWNER_PLAYER_2);

        // Renderer: Opponent takes the first pattern, player gets the second pattern
        simpleNativeRenderer = new SimpleNativeRenderer(
                PokemonSelectActivity.POKEMON_SELECT_LIST[oppPokeIndex].getModelPath(),
                PokemonSelectActivity.POKEMON_SELECT_LIST[pokeIndex].getModelPath());

        // Update one-time Pokemon views
        TextView playerPokemon = (TextView) findViewById(R.id.pokemonNameText);
        playerPokemon.setText(mPlayerPokemon.getName());
        TextView playerLvl = (TextView) findViewById(R.id.pokemonLevelText);
        String lv = "Lv " + mPlayerPokemon.getLevel();
        playerLvl.setText(lv);

        TextView oppPokemon = (TextView) findViewById(R.id.oppPokemonNameText);
        oppPokemon.setText(mOpponentPokemon.getName());
        TextView oppLvl = (TextView) findViewById(R.id.oppPokemonLevelText);
        lv = "Lv " + mOpponentPokemon.getLevel();
        oppLvl.setText(lv);

        // Set up views needed for updating
        mPlayerHPTextView = (TextView) findViewById(R.id.hpText);
        mPlayerHPBarView = findViewById(R.id.hpBar);
        mPlayerHPBarView.setPivotX(0f);
        mOppHPBarView = findViewById(R.id.oppHpBar);
        mOppHPBarView.setPivotX(0f);

        updatePlayerHPText();

        // Populate bags
        mPlayerBag = new Bag();
        mPlayerBag.addToBag(new ItemList.Potion(1));
        mPlayerBag.addToBag(new ItemList.WhiteHerb(1));
        mPlayerBag.addToBag(new ItemList.Pokeball(0));
        mOpponentBag = new Bag();
        mOpponentBag.addToBag(new ItemList.Potion(1));
        mOpponentBag.addToBag(new ItemList.WhiteHerb(1));
        
        String encounterMsg = mOpponentPokemon.getName() + " wants to fight!";
        MessageWindowFragment encounter = MessageWindowFragment.newInstance(encounterMsg);
        mMessageQueue.add("Go! " + mPlayerPokemon.getName() + "!");

        updateFragment(encounter, MessageWindowFragment.MESSAGE_WINDOW_TAG);
    }

    public void onStop() {
        SimpleNativeRenderer.demoShutdown();

        super.onStop();
    }

    public void updateFragment(Fragment frag, String tag) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.windowFragment, frag, tag);
        ft.commit();
    }

    public void updatePlayerHPText() {
        String hp = (int)mPlayerPokemon.getHP() + "/" + (int)mPlayerPokemon.getMaxHP();
        mPlayerHPTextView.setText(hp);
    }

    @Override
    protected ARRenderer supplyRenderer() {
        return simpleNativeRenderer;
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.mainLayout);
    }

    @Override
    public void onBackPressed() {
        if (mGameState == GameDefinitions.GAME_STATE_CONCLUSION) {
            finish();
        }
        else {
            super.onBackPressed();
        }
    }

    /*********************/
    /** Battle Step Sim **/
    /*********************/
    /**
     * 1) Activate item, if item is to be used
     * 2) Check if move will connect
     *  2a) Handle status move, if move is a stat modifier
     *  2b) Calculate damage
     * 3) Push messages to play out
     *
     * Accuracy formula:
     *      P = A(base) * A / E
     *
     * P = probability of hit, A(base) = move base accuracy, A = attacker modified accuracy
     * E = defender modified evasion
     *
     * Damage formula:
     *      Damage = ((2 * L  + 10) / 250 * (A / D) * B + 2) * Modifier
     *
     * L = attacker level, A = attacker attack, D = attacker defense, B = move base power
     *
     * Modifier formula:
     *      Modifier = STAB * T * C * O * rand(0.85, 1)
     *
     * STAB = 1.5 if attack type = Poke type, otherwise, 1.0
     * T = type effectiveness
     * C = 2 if critical, 1 otherwise
     * O = Other (items, abilities, etc)
     */
    private void simulateBattleStep(boolean useItem) {
        final String FN_TAG = "simulateBattleStep";

        if (useItem) {
            if (mTurnQueue.get(0).getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
                // AI used item
                handleUseItem(GameDefinitions.TAG_OWNER_PLAYER_2, mAIQueuedItem);
            }
            else {
                handleUseItem(GameDefinitions.TAG_OWNER_PLAYER_1, mQueuedPlayerItem);
            }
            mTurnQueue.remove(0);
            return;
        }

        if (mTurnQueue.isEmpty()) {
            Log.e(TAG, "Turn queue is empty when trying to simulate!");
            return;
        }

        Move move;
        if (mTurnQueue.get(0).getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
            move = simpleAIMove(mTurnQueue.get(0));
            move.reducePP();
            mAttacker = mOpponentPokemon;
            mDefender = mPlayerPokemon;
        }
        else {
            move = mTurnQueue.get(0).getMoveNumber(mQueuedPlayerMoveIndex);
            mAttacker = mPlayerPokemon;
            mDefender = mOpponentPokemon;
        }

        /**
         * The opposing X used Y!
         * X used Y!
         */
        String msg;
        if (mAttacker.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
            msg = "The opposing " + mAttacker.getName() + " used " + move.getName() + "!";
        }
        else {
            msg = mAttacker.getName() + " used " + move.getName() + "!";
        }
        MessageWindowFragment attackMsg = MessageWindowFragment.newInstance(msg);
        updateFragment(attackMsg, MessageWindowFragment.MESSAGE_WINDOW_TAG);

        // Accuracy check
        double acc = move.getAccuracy() * (mAttacker.getAccuracy() / mDefender.getEvasion()) / 100;
        Random rand = new Random();
        double roll = rand.nextDouble();

        if (roll <= acc) {
            // If move is a status modifier, apply it now
            if (move.getCategory() == Constants.MOVE_CATEGORY_STATUS) {
                handleStatusMove(move);
                mTurnQueue.remove(0);
                return;
            }

            int damage = calculateDamage(move);
            Log.d(FN_TAG, "Damage: " + damage);

            // Inflict damage and update view
            float currentPercent = (float) (mDefender.getHP() / mDefender.getMaxHP());
            mDefender.takeDamage(damage);
            float newPercent = (float) (mDefender.getHP() / mDefender.getMaxHP());
            if (mDefender.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_1) {
                updatePlayerHPText();

                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mPlayerHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
            }
            else {
                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mOppHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
            }

            // Check if Pokemon KOed
            if (mDefender.getHP() == 0) {
                mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

                String faint, conclusion;
                if (mDefender.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
                    faint = "The opposing " + mDefender.getName();
                    conclusion = getString(R.string.message_win);
                }
                else {
                    faint = mDefender.getName();
                    conclusion = getString(R.string.message_lose);
                }
                faint += " fainted!";

                mTurnQueue.clear();
                mMessageQueue.add(faint);
                mMessageQueue.add(conclusion);
                return;
            }
        }
        else {
            // Move failed
            mMessageQueue.add(getString(R.string.message_miss));

            Log.d(FN_TAG, "Accuracy: " + acc);
            Log.d(FN_TAG, "Rolled: " + roll);
        }

        mTurnQueue.remove(0);
    }

    private void handleUseItem(int player, Item item) {
        Pokemon targetPokemon;
        String target1;
        String target2;
        if (player == GameDefinitions.TAG_OWNER_PLAYER_1) {
            targetPokemon = mPlayerPokemon;

            target1 = "You ";
            target2 = mPlayerPokemon.getName();
        }
        else {
            targetPokemon = mOpponentPokemon;

            target1 = "The opposing " + mOpponentPokemon.getName() + "'s trainer ";
            target2 = "The opposing " + mOpponentPokemon.getName();

            // Clear opponent queued item
            mAIQueuedItem = null;
        }

        String display1 = target1 + "used a " + item.getName() + "!";

        // For potion
        float currentPercent = (float) (targetPokemon.getHP() / targetPokemon.getMaxHP());
        item.useItem(targetPokemon);

        // Set up displays
        if (item.getItemID() == GameDefinitions.ITEM_ID_POTION) {
            float newPercent = (float) (targetPokemon.getHP() / targetPokemon.getMaxHP());
            if (targetPokemon.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_1) {
                updatePlayerHPText();

                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mPlayerHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
            } else {
                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mOppHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
            }
        }

        String display2 = target2 + item.getEffectText();
        mMessageQueue.add(display2);

        MessageWindowFragment msg = MessageWindowFragment.newInstance(display1);
        updateFragment(msg, MessageWindowFragment.MESSAGE_WINDOW_TAG);
    }

    private void handleStatusMove(Move move) {
        // Constants have been set up so that EVEN values target the self and ODD values
        // target the opponent
        String target;
        Pokemon targetPokemon;
        if (move.getModifierType() % 2 == 0) {
            targetPokemon = mAttacker;
        }
        else {
            targetPokemon = mDefender;
        }
        if (targetPokemon.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
            target = "The opposing " + targetPokemon.getName() + "'s ";
        }
        else {
            target = targetPokemon.getName() + "'s ";
        }

        // Constants have been set up so that DIVIDING the value by 4 will give the stat:
        //  Value / 4 = 0 > Attack              Value / 4 = 1 > Defense
        //  Value / 4 = 2 > Special Attack      Value / 4 = 3 > Special Defense
        //  Value / 4 = 4 > Speed               Value / 4 = 5 > Accuracy
        //  Value / 4 = 6 > Evasion
        // The resultant values also align with the index in the Pokemon's stat modifier table
        String stat;
        int val = move.getModifierType() / 4;
        switch (val) {
            case Constants.STAT_MODIFIER_TABLE_ATTACK_INDEX:
                stat = "Attack ";
                break;
            case Constants.STAT_MODIFIER_TABLE_DEFENSE_INDEX:
                stat = "Defense ";
                break;
            case Constants.STAT_MODIFIER_TABLE_SPECIAL_ATTACK_INDEX:
                stat = "Special Attack ";
                break;
            case Constants.STAT_MODIFIER_TABLE_SPECIAL_DEFENSE_INDEX:
                stat = "Special Defense ";
                break;
            case Constants.STAT_MODIFIER_TABLE_SPEED_INDEX:
                stat = "Speed ";
                break;
            case Constants.STAT_MODIFIER_TABLE_ACCURACY_INDEX:
                stat = "Accuracy ";
                break;
            case Constants.STAT_MODIFIER_TABLE_EVASION_INDEX:
                stat = "Evasion ";
                break;
            default:
                Log.e(TAG, "Encountered bad value in determining status stat!");
                stat = "??? ";
                break;
        }

        // Bound stat modifier values
        int dirMessageIndex;
        int currentModifierVal = targetPokemon.getStatModifierValue(val);

        // Drop stat
        if (move.getModifierValue() < 0) {
            if (currentModifierVal == -6) {
                dirMessageIndex = 0;
            }
            else {
                dirMessageIndex = move.getModifierValue()
                        + GameDefinitions.MESSAGE_STAT_DOWN_INDEX_OFFSET;

                targetPokemon.addStatModifier(val, move.getModifierValue());
            }
        }
        else {
            if (currentModifierVal == 6) {
                dirMessageIndex = GameDefinitions.MESSAGE_STAT_DOWN_INDEX_OFFSET +
                        GameDefinitions.MESSAGE_STAT_UP_INDEX_OFFSET;
            }
            else {
                dirMessageIndex = move.getModifierValue()
                        + GameDefinitions.MESSAGE_STAT_UP_INDEX_OFFSET;

                targetPokemon.addStatModifier(val, move.getModifierValue());
            }
        }

        // Set up display message
        String statMsg = target + stat + GameDefinitions.MESSAGE_STAT_DIRECTION_TEXT[dirMessageIndex];
        mMessageQueue.add(statMsg);
    }

    private int calculateDamage(Move move) {
        // Calculate modifier
        double stab;
        if (move.getType() == mAttacker.getType()) { stab = 1.5; }
        else { stab = 1.0; }

        double effectiveness = Constants.TYPE_INTERACTION_TABLE[move.getType()][mDefender.getType()];

        if (effectiveness > 1) {
            mMessageQueue.add(getString(R.string.message_super_effective));
        }
        else if (effectiveness < 1) {
            mMessageQueue.add(getString(R.string.message_not_effective));
        }

        Random rand = new Random();
        double r = 0.85 + (1.0 - 0.85) * rand.nextDouble();

        // Critical and other are 1.0 for now
        double modifier = stab * effectiveness * r;

        // Calculate damage
        double attack, defense;
        if (move.getCategory() == Constants.MOVE_CATEGORY_PHYSICAL) {
            attack = mAttacker.getAttack();
            defense = mDefender.getDefense();
        }
        else if (move.getCategory() == Constants.MOVE_CATEGORY_SPECIAL) {
            attack = mAttacker.getSpecialAttack();
            defense = mDefender.getSpecialDefense();
        }
        else {
            Log.e(TAG, "Unrecognized move category!");
            attack = defense = 0;
        }

        double a = 2 * (double) mAttacker.getLevel() + 10;
        return (int) Math.floor(Math.floor(a / 250.0 * attack / defense * move.getBasePower() + 2) * modifier);
    }

    private Move simpleAIMove(Pokemon pokemon) {
        Random rand = new Random();
        int moveIndex;
        Move move;

        do {
            // Max moves in the movelist is 4
            moveIndex = rand.nextInt(4);

            move = pokemon.getMoveNumber(moveIndex);
        } while (move.getPP() == 0);

        Log.d("simpleAIMove", "Move index = " + moveIndex);
        Log.d("simpleAIMove", "Move name = " + move.getName());
        Log.d("simpleAIMove", "Move PP = " + move.getPP());

        return move;
    }

    /**
     * Check for potion use first. Constraints are statically defined in GameDefinitions. Next check
     * is for White Herb. Use consider only starts when net negative stat modifiers is -2 or more.
     * Chance for using White Herb depends on the net negative stat modifier value.
     * 
     * @return True if opponent is going to use item
     */
    private boolean checkAIUseItem() {
        // Check if Potion exists
        int potionIndex = mOpponentBag.getIndexOfItem(GameDefinitions.ITEM_ID_POTION);
        if (potionIndex > -1) {
            // Check if potion can be used
            Item opPotion = mOpponentBag.getItemAtIndex(potionIndex);
            if (opPotion.getQuantity() > 0) {
                double relativeHp = mOpponentPokemon.getHP() / mOpponentPokemon.getMaxHP();
                if (relativeHp <= GameDefinitions.AI_POTION_HP_THRESHOLD) {
                    Random rand = new Random();
                    double roll = rand.nextDouble();
                    double chance = GameDefinitions.AI_POTION_USE_CHANCE_COEFF *
                            Math.pow(relativeHp, GameDefinitions.AI_POTION_USE_CHANCE_EXP);
                    Log.d("checkAIUseItem", "Potion roll: " + roll);
                    Log.d("checkAIUseItem", "Use chance: " + chance);
                    // Roll for Potion use
                    if (roll <= chance) {
                        mAIQueuedItem = opPotion;
                        return true;
                    }
                }
            }
        }
        
        // Check for white herb
        int herbIndex = mOpponentBag.getIndexOfItem(GameDefinitions.ITEM_ID_WHITEHERB);
        if (herbIndex > -1) {
            // Check if white herb can be used
            Item opHerb = mOpponentBag.getItemAtIndex(herbIndex);
            if (opHerb.getQuantity() > 0) {
                int netNegStat = 0;
                for (int i = 0; i < Constants.STAT_MODIFIER_TABLE_EVASION_INDEX + 1; i++) {
                    int statMod = mOpponentPokemon.getStatModifierValue(i);
                    if (statMod < 0) {
                        netNegStat += statMod;
                    }
                }
                if (netNegStat <= GameDefinitions.AI_WHITE_HERB_THRESHOLD) {
                    Random rand = new Random();
                    double chance = ((double)-netNegStat) * GameDefinitions.AI_WHITE_HERB_UNIT_CHANCE;
                    double roll = rand.nextDouble();
                    Log.d("checkAIUseItem", "Herb roll: " + roll);
                    Log.d("checkAIUseItem", "Use chance: " + chance);
                    // Roll for White Herb use
                    if (roll <= chance) {
                        mAIQueuedItem = opHerb;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkExhaustion() {
        boolean exhaustedPlayer = true;
        boolean exhaustedOpponent = true;

        // Only 4 moves in the move list
        for (int i = 0; i < 4; i++) {
            if (mPlayerPokemon.getMoveNumber(i).getPP() != 0) {
                exhaustedPlayer = false;
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (mOpponentPokemon.getMoveNumber(i).getPP() != 0) {
                exhaustedOpponent = false;
                break;
            }
        }

        if (exhaustedPlayer && exhaustedOpponent) {
            Log.d(TAG, "Both players have no PP left!");
            mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

            mMessageQueue.add(getString(R.string.message_draw1));
            mMessageQueue.add(getString(R.string.message_draw2));
        }
        else if (exhaustedPlayer) {
            Log.d(TAG, "Player has no moves left!");
            mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

            String msg = mPlayerPokemon.getName() + " " + getString(R.string.message_exhaust);

            mMessageQueue.add(msg);
            mMessageQueue.add(getString(R.string.message_lose));
        }
        else if (exhaustedOpponent) {
            Log.d(TAG, "Opponent has no moves left!");
            mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

            String msg = "The opposing " + mOpponentPokemon.getName() + " " +
                    getString(R.string.message_exhaust);

            mMessageQueue.add(msg);
            mMessageQueue.add(getString(R.string.message_win));
        }

        return (exhaustedPlayer || exhaustedOpponent);
    }

    /************************/
    /** Fragment Listeners **/
    /************************/
    @Override
    public void onMessageWindowFragmentInteraction() {

        if (!mMessageQueue.isEmpty()) {
            MessageWindowFragment next = MessageWindowFragment.newInstance(mMessageQueue.get(0));
            mMessageQueue.remove(0);
            updateFragment(next, MessageWindowFragment.MESSAGE_WINDOW_TAG);
        }
        else {
            // Check game state
            switch (mGameState) {
                case GameDefinitions.GAME_STATE_ENCOUNTER:
                    // Add the main battle fragment
                    mGameState = GameDefinitions.GAME_STATE_BATTLE;

                    MainWindowFragment main = MainWindowFragment.newInstance(mPlayerPokemon.getName());
                    updateFragment(main, MainWindowFragment.MAIN_WINDOW_TAG);
                    break;
                case GameDefinitions.GAME_STATE_BATTLE:
                    if (!mTurnQueue.isEmpty()) {
                        // If the next turn belongs to the opponent and there is a queued item, we know
                        // the opponent will use it
                        if (mTurnQueue.get(0).getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2 &&
                                mAIQueuedItem != null) {
                            simulateBattleStep(true);
                        }
                        else {
                            simulateBattleStep(false);
                        }
                    }
                    else {
                        // Make sure that either Pokemon has moves with PP left
                        if (!checkExhaustion()) {
                            MainWindowFragment bat = MainWindowFragment.newInstance(mPlayerPokemon.getName());

                            updateFragment(bat, MainWindowFragment.MAIN_WINDOW_TAG);
                        }
                    }
                    break;
                case GameDefinitions.GAME_STATE_CONCLUSION:
                    Toast.makeText(this, "Accept your fate!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "Unrecognized game state!");
                    break;
            }
        }
    }

    @Override
    public void onMainButtonInteraction(int btnId) {
        switch (btnId) {
            case R.id.fightButton: {
                // Add move list window with back capability
                MoveListWindowFragment moves = MoveListWindowFragment.newInstance(
                        mPlayerPokemon.getMoveNumber(0), mPlayerPokemon.getMoveNumber(1),
                        mPlayerPokemon.getMoveNumber(2), mPlayerPokemon.getMoveNumber(3)
                );

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.windowFragment, moves, MoveListWindowFragment.MOVE_WINDOW_TAG)
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.bagButton: {
                BagWindowFragment bag = BagWindowFragment.newInstance(mPlayerBag);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.windowFragment, bag, BagWindowFragment.BAG_WINDOW_TAG)
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.pokemonButton: {
                MessageWindowFragment pkmn = MessageWindowFragment
                        .newInstance(getString(R.string.message_pokemon));
                updateFragment(pkmn, MessageWindowFragment.MESSAGE_WINDOW_TAG);
                break;
            }
            case R.id.runButton: {
                MessageWindowFragment run = MessageWindowFragment
                        .newInstance(getString(R.string.message_run));
                updateFragment(run, MessageWindowFragment.MESSAGE_WINDOW_TAG);
                break;
            }
        }
    }

    @Override
    public void onMoveSelected(int moveIndex) {
        // See if move has enough PP first
        Move move = mPlayerPokemon.getMoveNumber(moveIndex);

        mQueuedPlayerMoveIndex = moveIndex;

        if (move.getPP() == 0) {
            Toast.makeText(this, "Not enough PP!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Reduce move's PP
            move.reducePP();

            boolean aiItem = checkAIUseItem();
            if (aiItem) {
                // AI goes first for using item
                mTurnQueue.add(mOpponentPokemon);
                mTurnQueue.add(mPlayerPokemon);
                mAttacker = mPlayerPokemon;
                mDefender = mOpponentPokemon;

                simulateBattleStep(true);
            }
            else {
                // Set up who goes first
                if (mPlayerPokemon.getSpeed() > mOpponentPokemon.getSpeed()) {
                    mTurnQueue.add(mPlayerPokemon);
                    mAttacker = mPlayerPokemon;
                    mTurnQueue.add(mOpponentPokemon);
                    mDefender = mOpponentPokemon;
                } else {
                    mTurnQueue.add(mOpponentPokemon);
                    mAttacker = mOpponentPokemon;
                    mTurnQueue.add(mPlayerPokemon);
                    mDefender = mPlayerPokemon;
                }

                simulateBattleStep(false);
            }

        }
    }

    @Override
    public void onBagItemSelected(int index) {
        Item item = mPlayerBag.getItemAtIndex(index);

        if (item.getQuantity() == 0) {
            Toast.makeText(this, "You have none left!", Toast.LENGTH_SHORT).show();
        }
        else {
            mQueuedPlayerItem = item;

            checkAIUseItem();

            // Player goes first, even if opponent is using item
            mTurnQueue.add(mPlayerPokemon);
            mTurnQueue.add(mOpponentPokemon);
            mAttacker = mOpponentPokemon;
            mDefender = mPlayerPokemon;

            simulateBattleStep(true);
        }
    }
}