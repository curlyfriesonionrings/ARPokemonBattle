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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.ARPokemonBattle.Bag.Bag;
import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.Bluetooth.BluetoothService;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Move;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Pokemon;
import org.artoolkit.ar.ARPokemonBattle.Util.Action;
import org.artoolkit.ar.ARPokemonBattle.Util.BluetoothPacketHelper;
import org.artoolkit.ar.ARPokemonBattle.Util.Constants;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;
import org.artoolkit.ar.ARPokemonBattle.Util.ItemList;
import org.artoolkit.ar.ARPokemonBattle.Util.ParcelableUtil;
import org.artoolkit.ar.ARPokemonBattle.Util.PokemonList;
import org.artoolkit.ar.ARPokemonBattle.Window.BagWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MainWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MessageWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MoveListWindowFragment;
import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Game state transition:
 *
 * Interacting with message dialog window transitions game states
 *
 * Sends data to client for display in the following order:
 *   Move list index 0 -> (on ack) Move list index 1 ... Move list index 3 -> client bad item 1 ->
 *   client bag item 2 ... host pokemon name -> client pokemon name -> client pokemon current hp ->
 *   client pokemon max hp -> host pokemon level -> client pokemon level
 *
 * The next set of information is only sent on receiving the ack from the client
 */
public class ARPokemonBattleActivityHost extends ARActivity implements
    MessageWindowFragment.OnFragmentInteractionListener,
    MainWindowFragment.OnFragmentInteractionListener,
    MoveListWindowFragment.OnFragmentInteractionListener,
    BagWindowFragment.OnFragmentInteractionListener {

    private static final String TAG = ARPokemonBattleActivityHost.class.getSimpleName();

    /** Index value **/
    public static final String ARG_PLAYER_POKEMON = "player_pokemon";
    public static final String ARG_OPPONENT_POKEMON = "opponent_pokemon";

    private SimpleNativeRenderer simpleNativeRenderer;

    private int mGameState;
    private ArrayList<String> mMessageQueue;

    /** Battle turn list **/
    private ArrayList<Action> mTurnQueue;

    /** Simulated battle message display queue **/
    private ArrayList<byte[]> mBattleSimulationQueueHost;
    private ArrayList<byte[]> mBattleSimulationQueueClient;

    private Pokemon mAttacker, mDefender;

    private Pokemon mPlayerPokemon, mOpponentPokemon;

    private Bag mPlayerBag, mOpponentBag;

    private TextView mPlayerHPTextView;
    private View mPlayerHPBarView, mOppHPBarView;

    private BluetoothService mBluetoothService;
    private boolean mIsBound;

    /**
     * Used to keep track of what needs to be sent to client for display. Subvalue acts as
     * the parameter for the main op code and alternates between 1 and 2 (for host/client or
     * current/max HP). Main op_code increments by one each time the subvalue reverts to 1 from a
     * 2 value
     */
    private int mSendDiplayParam = 0x1;
    private int mSendDisplayOpCode = 0x5;

    private boolean mQueuedAction = false;
    // If client has submitted a move yet
    private boolean mWaitingForRemote = true;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBluetoothService = ((BluetoothService.ServiceBinder)service).getService();
            mBluetoothService.setHandler(mHandler);
            Log.d(TAG, "BT Service connected, state: " + mBluetoothService.getState());
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBluetoothService = null;
            Log.e(TAG, "BT Service disconnected");
        }
    };

    private void doBindService() {
        bindService(new Intent(ARPokemonBattleActivityHost.this,
                BluetoothService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mGameState = GameDefinitions.GAME_STATE_ENCOUNTER;
        mMessageQueue = new ArrayList<>();

        mTurnQueue = new ArrayList<>();

        mBattleSimulationQueueHost = new ArrayList<>();
        mBattleSimulationQueueClient = new ArrayList<>();

        mPlayerBag = new Bag();
        mPlayerBag.addToBag(new ItemList.Potion(1));
        mPlayerBag.addToBag(new ItemList.WhiteHerb(1));
        mPlayerBag.addToBag(new ItemList.Pokeball(0));

        // Opponent bag is populated on client side after move list
        // Items are added to this bag so we can pop them off as they are sent to client
        mOpponentBag = new Bag();
        mOpponentBag.addToBag(new ItemList.Potion(1));
        mOpponentBag.addToBag(new ItemList.WhiteHerb(1));
        mOpponentBag.addToBag(new ItemList.Pokeball(0));

        // Service should have been started by PokemonSelectActivity
        doBindService();

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
        ft.replace(R.id.windowFragment, frag, tag).commit();
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
     * 1) Check if move will connect
     * 2) Calculate damage
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
    private void simulateBattle() {
        Log.d(TAG, "Entering battle simulation");

        determineTurnOrder();

        while (!mTurnQueue.isEmpty()) {

            Action turnAction = mTurnQueue.get(0);

            // Handle items
            if (turnAction.getActionCode() == GameDefinitions.ACTION_ITEM) {
                handleUseItem(turnAction);
                mTurnQueue.remove(0);
            }
            else {
                Move move;
                if (turnAction.getPlayer() == GameDefinitions.TAG_OWNER_PLAYER_2) {
                    move = mOpponentPokemon.getMoveNumber(turnAction.getMoveIndex());
                    mAttacker = mOpponentPokemon;
                    mDefender = mPlayerPokemon;
                } else {
                    move = mPlayerPokemon.getMoveNumber(turnAction.getMoveIndex());
                    mAttacker = mPlayerPokemon;
                    mDefender = mOpponentPokemon;
                }

                /**
                 * The opposing X used Y!
                 * X used Y!
                 */
                String msgHost, msgClient;
                if (mAttacker.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
                    msgHost = "The opposing " + mAttacker.getName() + " used " + move.getName() + "!";
                    msgClient = mAttacker.getName() + " used " + move.getName() + "!";
                } else {
                    msgHost = mAttacker.getName() + " used " + move.getName() + "!";
                    msgClient = "The opposing " + mAttacker.getName() + " used " + move.getName() + "!";
                }

                addBattleSimulationMessage(msgHost, BluetoothPacketHelper.TARGET_HOST);
                addBattleSimulationMessage(msgClient, BluetoothPacketHelper.TARGET_CLIENT);

                // Accuracy check
                double acc = move.getAccuracy() * (mAttacker.getAccuracy() / mDefender.getEvasion()) / 100;
                Random rand = new Random();
                double roll = rand.nextDouble();

                if (roll <= acc) {
                    // If move is a status modifier, apply it now
                    if (move.getCategory() == Constants.MOVE_CATEGORY_STATUS) {
                        handleStatusMove(move);
                    } else {
                        int damage = calculateDamage(move);
                        Log.d(TAG, "Damage: " + damage);

                        // Inflict damage and update view
                        float currentPercent = (float) (mDefender.getHP() / mDefender.getMaxHP());
                        mDefender.takeDamage(damage);
                        float newPercent = (float) (mDefender.getHP() / mDefender.getMaxHP());
                        if (mDefender.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_1) {
                            addBattleSimulationHealthChange(BluetoothPacketHelper.TARGET_HOST, currentPercent,
                                    newPercent, 0);
                        } else {
                            addBattleSimulationHealthChange(BluetoothPacketHelper.TARGET_CLIENT, currentPercent,
                                    newPercent, damage);
                        }

                        // Check if Pokemon KOed
                        if (mDefender.getHP() == 0) {
                            if (mDefender.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
                                addBattleSimulationConclusion(BluetoothPacketHelper.TARGET_CLIENT);
                            } else {
                                addBattleSimulationConclusion(BluetoothPacketHelper.TARGET_HOST);
                            }
                        }
                    }
                } else {
                    // Move failed
                    addBattleSimulationMessage(getString(R.string.message_miss), BluetoothPacketHelper.TARGET_BOTH);

                    Log.d(TAG, "Accuracy: " + acc);
                    Log.d(TAG, "Rolled: " + roll);
                }

                // If Pokemon was KOed, we terminate the battle
                if (mDefender.getHP() == 0) {
                    mTurnQueue.clear();
                } else {
                    mTurnQueue.remove(0);
                }

                // If either Pokemon has exhausted, we terminate the battle
                if (checkExhaustion()) {
                    mTurnQueue.clear();
                }
            }
        }

        Log.d(TAG, "Simulation completed, turn reset and start playback");

        // Reset for next turn
        mWaitingForRemote = true;
        mQueuedAction = false;

        // Battle simulation has been complete, so advance the game state
        playBattleSimulationStep();
        sendBattleSimulationStep(0);
    }

    /**
     * Player using item always goes first. If both player use item, turn order does not matter.
     *
     * If neither player uses item, Pokemon with the highest speed goes first
     */
    private void determineTurnOrder() {
        if (mTurnQueue.size() > 2) {
            Log.e(TAG, "Turn queue has more than 2 actions!");
            return;
        }

        Action one = mTurnQueue.get(0);
        Action two = mTurnQueue.get(1);

        // If action one in queue is using item, we can leave as is
        if (one.getActionCode() == GameDefinitions.ACTION_ITEM) {
            Log.d(TAG, "First action is item use");
            return;
        }

        // If action two is use item and action one is not, we have to reverse
        if (two.getActionCode() == GameDefinitions.ACTION_ITEM) {
            Log.d(TAG, "Second action was item use, flipping turn order");
            mTurnQueue.clear();
            mTurnQueue.add(two);
            mTurnQueue.add(one);
            return;
        }

        // Neither action is an item action, so we use Pokemon speed
        Action host, client;
        if (one.getPlayer() == GameDefinitions.TAG_OWNER_PLAYER_1) {
            host = one; client = two;
        }
        else {
            host = two; client = one;
        }

        mTurnQueue.clear();
        if (mPlayerPokemon.getSpeed() > mOpponentPokemon.getSpeed()) {
            Log.d(TAG, "Host Pokemon speed > client Pokemon speed");

            mTurnQueue.add(host);
            mTurnQueue.add(client);
        }
        else {
            Log.d(TAG, "Client Pokemon speed > host Pokemon speed");

            mTurnQueue.add(client);
            mTurnQueue.add(host);
        }
    }

    private void handleUseItem(Action action) {
        Pokemon targetPokemon;
        Item item = action.getItem();
        String actionTextHost1, actionTextHost2;
        String actionTextClient1, actionTextClient2;
        if (action.getPlayer() == GameDefinitions.TAG_OWNER_PLAYER_1) {
            targetPokemon = mPlayerPokemon;

            actionTextHost1 = "You used a " + item.getName() + "!";
            actionTextHost2 = mPlayerPokemon.getName() + item.getEffectText();
            actionTextClient1 = "The opposing " + mPlayerPokemon.getName() + "'s trainer used a " +
                item.getName() + "!";
            actionTextClient2 = "The opposing " + mPlayerPokemon.getName() + item.getEffectText();
        }
        else {
            targetPokemon = mOpponentPokemon;

            actionTextHost1 = "The opposing " + mOpponentPokemon.getName() + "'s trainer used a " +
                    item.getName() + "!";
            actionTextHost2 = "The opposing " + mOpponentPokemon.getName() + item.getEffectText();
            actionTextClient1 = "You used a " + item.getName() + "!";
            actionTextClient2 = mOpponentPokemon.getName() + item.getEffectText();

        }

        // Add the "trainer used X" text first
        addBattleSimulationMessage(actionTextHost1, BluetoothPacketHelper.TARGET_HOST);
        addBattleSimulationMessage(actionTextClient1, BluetoothPacketHelper.TARGET_CLIENT);

        // For potion
        float currentPercent = (float) (targetPokemon.getHP() / targetPokemon.getMaxHP());
        item.applyEffect(targetPokemon);

        // Set up displays
        if (item.getItemID() == GameDefinitions.ITEM_ID_POTION) {
            float newPercent = (float) (targetPokemon.getHP() / targetPokemon.getMaxHP());
            if (targetPokemon.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_1) {
                addBattleSimulationHealthChange(BluetoothPacketHelper.TARGET_HOST, currentPercent,
                        newPercent, 0);
            } else {
                // Negative potion damage to heal client
                addBattleSimulationHealthChange(BluetoothPacketHelper.TARGET_CLIENT, currentPercent,
                        newPercent, -GameDefinitions.POTION_HEAL_AMOUNT);
            }
        }

        addBattleSimulationMessage(actionTextHost2, BluetoothPacketHelper.TARGET_HOST);
        addBattleSimulationMessage(actionTextClient2, BluetoothPacketHelper.TARGET_CLIENT);
    }

    private void handleStatusMove(Move move) {
        Log.d(TAG, "Handling status move");
        // Constants have been set up so that EVEN values target the self and ODD values
        // target the opponent
        String targetHost, targetClient;
        Pokemon targetPokemon;
        if (move.getModifierType() % 2 == 0) {
            targetPokemon = mAttacker;
        }
        else {
            targetPokemon = mDefender;
        }
        if (targetPokemon.getOwner() == GameDefinitions.TAG_OWNER_PLAYER_2) {
            targetHost = "The opposing " + targetPokemon.getName() + "'s ";
            targetClient = targetPokemon.getName() + "'s ";
        }
        else {
            targetHost = targetPokemon.getName() + "'s ";
            targetClient = "The opposing " + targetPokemon.getName() + "'s ";
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
                dirMessageIndex = move.getModifierValue() + GameDefinitions.MESSAGE_STAT_DOWN_INDEX_OFFSET;

                targetPokemon.addStatModifier(val, move.getModifierValue());
            }
        }
        else {
            if (currentModifierVal == 6) {
                dirMessageIndex = GameDefinitions.MESSAGE_STAT_DOWN_INDEX_OFFSET +
                        GameDefinitions.MESSAGE_STAT_UP_INDEX_OFFSET;
            }
            else {
                dirMessageIndex = move.getModifierValue() + GameDefinitions.MESSAGE_STAT_UP_INDEX_OFFSET;

                targetPokemon.addStatModifier(val, move.getModifierValue());
            }
        }

        // Set up display message
        String statMsgHost = targetHost + stat + GameDefinitions.MESSAGE_STAT_DIRECTION_TEXT[dirMessageIndex];
        String statMsgClient = targetClient + stat + GameDefinitions.MESSAGE_STAT_DIRECTION_TEXT[dirMessageIndex];
        addBattleSimulationMessage(statMsgHost, BluetoothPacketHelper.TARGET_HOST);
        addBattleSimulationMessage(statMsgClient, BluetoothPacketHelper.TARGET_CLIENT);
    }

    private int calculateDamage(Move move) {
        // Calculate modifier
        double stab;
        if (move.getType() == mAttacker.getType()) { stab = 1.5; }
        else { stab = 1.0; }

        double effectiveness = Constants.TYPE_INTERACTION_TABLE[move.getType()][mDefender.getType()];

        if (effectiveness > 1) {
            addBattleSimulationMessage(getString(R.string.message_super_effective),
                    BluetoothPacketHelper.TARGET_BOTH);
        }
        else if (effectiveness < 1) {
            addBattleSimulationMessage(getString(R.string.message_not_effective),
                    BluetoothPacketHelper.TARGET_BOTH);
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
            Log.e(TAG, "Unrecognized move category in calculateDamage!");
            attack = defense = 0;
        }

        double a = 2 * (double) mAttacker.getLevel() + 10;
        return (int) Math.floor(Math.floor(a / 250.0 * attack / defense * move.getBasePower() + 2) * modifier);
    }

    private boolean checkExhaustion() {
        boolean exhaustedHost = true;
        boolean exhaustedClient = true;

        // Only 4 moves in the move list
        for (int i = 0; i < 4; i++) {
            if (mPlayerPokemon.getMoveNumber(i).getPP() != 0) {
                exhaustedHost = false;
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (mOpponentPokemon.getMoveNumber(i).getPP() != 0) {
                exhaustedClient = false;
                break;
            }
        }

        if (exhaustedHost && exhaustedClient) {
            Log.d(TAG, "Both players have no PP left!");
            addBattleSimulationExhausted(BluetoothPacketHelper.TARGET_BOTH);
        }
        else if (exhaustedHost) {
            Log.d(TAG, "Host has no moves left!");
            addBattleSimulationExhausted(BluetoothPacketHelper.TARGET_HOST);
        }
        else if (exhaustedClient) {
            Log.d(TAG, "Client has no moves left!");
            addBattleSimulationExhausted(BluetoothPacketHelper.TARGET_CLIENT);
        }

        return (exhaustedHost || exhaustedClient);
    }

    private void addBattleSimulationMessage(String msg, int target) {
        byte[] payload = BluetoothPacketHelper.appendByteArrayToInt(msg.length(), msg.getBytes());
        byte[] data = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_BATTLE_MESSAGE,
                payload);

        switch (target) {
            case BluetoothPacketHelper.TARGET_HOST:
                Log.d(TAG, "Add to host: " + msg);
                mBattleSimulationQueueHost.add(data);
                break;
            case BluetoothPacketHelper.TARGET_CLIENT:
                Log.d(TAG, "Add to client: " + msg);
                mBattleSimulationQueueClient.add(data);
                break;
            default:
                // target = 0 means add to both
                Log.d(TAG, "Add to both: " + msg);
                mBattleSimulationQueueHost.add(data);
                mBattleSimulationQueueClient.add(data);
                break;
        }
    }

    private void addBattleSimulationHealthChange(int target, float cPercent, float fPercent, int damage) {
        byte[] curPercentByte = BluetoothPacketHelper.convertToByteArray(cPercent);
        byte[] finPercentByte = BluetoothPacketHelper.convertToByteArray(fPercent);
        byte[] temp = BluetoothPacketHelper.appendByteArrayToInt(target,
                BluetoothPacketHelper.appendByteArrays(curPercentByte, finPercentByte));

        byte[] payload = new byte[temp.length + 1];

        System.arraycopy(temp, 0, payload, 0, temp.length);
        payload[payload.length - 1] = Integer.valueOf(damage).byteValue();

        byte[] data = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_ANIMATE_HEALTH,
                payload);

        // This battle message is target agnostic
        Log.d(TAG, "Add health change to both");
        mBattleSimulationQueueHost.add(data);
        mBattleSimulationQueueClient.add(data);
    }

    private void addBattleSimulationConclusion(int target) {
        byte[] data = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_POKEMON_FAINTED,
                target);

        // This battle message is target agnostic
        Log.d(TAG, "Add conclusion to both");
        mBattleSimulationQueueHost.add(data);
        mBattleSimulationQueueClient.add(data);
    }

    private void addBattleSimulationExhausted(int target) {
        byte[] data = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_POKEMON_EXHAUSTED,
                target);

        // This battle message is target agnostic
        Log.d(TAG, "Add exhaustion to both");
        mBattleSimulationQueueHost.add(data);
        mBattleSimulationQueueClient.add(data);
    }

    /**
     * Battle simulation queue is filled after the battle simulation. Battle consists of playing
     * out one "phase" composing of player and opponent Pokemon moves. Interaction simply shows
     * user what happened during the simulation.
     *
     * A single "step" plays up until the next display message. This is because things like health
     * animation and determining the victor need to be handled while a message is being displayed
     */
    private void playBattleSimulationStep() {
        if (!mBattleSimulationQueueHost.isEmpty()) {
            // Parse simulation message. We can always assume that if there is an item in the
            // queue, the first item will always be a display message item, since we always
            // leave it in that state when we finish with this method
            byte[] data = mBattleSimulationQueueHost.get(0);

            /**
             * Battle message:
             *   Payload:   1 byte, length of String payload
             *              String, display message
             */
            // Cut out op_code and param
            int start = 2;
            int msgLen = data[1];
            byte[] msgBytes =  Arrays.copyOfRange(data, start, start + msgLen);
            String msg = new String(msgBytes);
            Log.d(TAG, "Battle message playback: " + msg);

            // If the message window is not already displayed, show it
            MessageWindowFragment frag = MessageWindowFragment.newInstance(msg);
            updateFragment(frag, MessageWindowFragment.MESSAGE_WINDOW_TAG);

            mBattleSimulationQueueHost.remove(0);
            Log.d(TAG, "Popped item from queue, new size: " + mBattleSimulationQueueHost.size());

            // Peek at next item and handle if necessary
            while (!nextSimulationIsMessage()) {
                byte[] nextData = mBattleSimulationQueueHost.get(0);
                int opcode = nextData[0];
                switch (opcode) {
                    case BluetoothPacketHelper.OP_CODE_BATTLE_MESSAGE:
                        break;

                    case BluetoothPacketHelper.OP_CODE_ANIMATE_HEALTH:
                        animateHealth(nextData);
                        mBattleSimulationQueueHost.remove(0);
                        Log.d(TAG, "Popped item from queue, new size: " +
                                mBattleSimulationQueueHost.size());
                        break;

                    case BluetoothPacketHelper.OP_CODE_POKEMON_FAINTED:
                        pokemonFainted(nextData);
                        mBattleSimulationQueueHost.remove(0);
                        Log.d(TAG, "Popped item from queue, new size: " +
                                mBattleSimulationQueueHost.size());
                        break;

                    case BluetoothPacketHelper.OP_CODE_POKEMON_EXHAUSTED:
                        pokemonExhausted(nextData);
                        mBattleSimulationQueueHost.remove(0);
                        Log.d(TAG, "Popped item from queue, new size: " +
                                mBattleSimulationQueueHost.size());
                        break;

                    default:
                        Log.e(TAG, "Unrecognized op code " + opcode);
                        break;
                }
            }
        }
        else {
            Log.e(TAG, "Battle simulation queue is empty!");
        }
    }

    private boolean nextSimulationIsMessage() {
        if (mBattleSimulationQueueHost.isEmpty()) {
            return true;
        }
        int op = mBattleSimulationQueueHost.get(0)[0];
        return (op == BluetoothPacketHelper.OP_CODE_BATTLE_MESSAGE);
    }

    private void animateHealth(byte[] in) {
        /**
         * Animate health:
         *   Payload:   1 byte, 0x1 for host HP or 0x2 for client HP
         *              4 bytes, long value for current percentage
         *              4 bytes, long value for final percentage
         *              1 byte, damage (IFF ANIMATE_CLIENT_HEALTH)
         */
        int target = in[1];
        // Offset to current percentage, will be used to read buffer
        int offset = 2;
        // 4 bytes for a long value
        byte[] cP = Arrays.copyOfRange(in, offset, offset + 4);
        offset += 4;
        float currentPercent = BluetoothPacketHelper.convertToFloat(cP);
        byte[] fP = Arrays.copyOfRange(in, offset, offset + 4);
        float newPercent = BluetoothPacketHelper.convertToFloat(fP);
        offset += 4;

        switch (target) {
            case BluetoothPacketHelper.TARGET_HOST: {
                Log.d(TAG, "Animate host health");
                updatePlayerHPText();

                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mPlayerHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
                break;
            }
            case BluetoothPacketHelper.TARGET_CLIENT: {
                Log.d(TAG, "Animate client health");
                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mOppHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
                break;
            }
        }
    }

    private void pokemonFainted(byte[] in) {
        /**
         * Fainted:
         *   Payload:   1 byte, 0x1 for host fainted of 0x2 for client fainted
         */
        int target = in[1];

        mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

        String faint, conclusion;
        if (target == BluetoothPacketHelper.TARGET_CLIENT) {
            Log.d(TAG, "Client Pokemon fainted");
            faint = "The opposing " + mOpponentPokemon.getName();
            conclusion = getString(R.string.message_win);
        }
        else {
            Log.d(TAG, "Host Pokemon fainted");
            faint = mPlayerPokemon.getName();
            conclusion = getString(R.string.message_lose);
        }
        faint += " fainted!";

        mMessageQueue.add(faint);
        mMessageQueue.add(conclusion);
    }

    private void pokemonExhausted(byte[] in) {
        /**
         * Exhausted:
         *   Payload:   1 byte, 0x0 for both, 0x1 for host, 0x2 for client
         */
        int target = in[1];

        mGameState = GameDefinitions.GAME_STATE_CONCLUSION;

        switch (target) {
            case BluetoothPacketHelper.TARGET_BOTH: {
                mMessageQueue.add(getString(R.string.message_draw1));
                mMessageQueue.add(getString(R.string.message_draw2));
                break;
            }

            case BluetoothPacketHelper.TARGET_HOST: {
                String msg = mPlayerPokemon.getName() + " " + getString(R.string.message_exhaust);
                mMessageQueue.add(msg);
                mMessageQueue.add(getString(R.string.message_lose));
                break;
            }

            case BluetoothPacketHelper.TARGET_CLIENT: {
                String msg = "The opposing " + mOpponentPokemon.getName() + " " +
                        getString(R.string.message_exhaust);
                mMessageQueue.add(msg);
                mMessageQueue.add(getString(R.string.message_win));
                break;
            }
        }
    }

    /************************/
    /** BT SERVICE HANDLER **/
    /************************/
    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GameDefinitions.MESSAGE_STATE_CHANGE:
                    finish();
                    break;
                case GameDefinitions.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
                    // don't need to do anything on write?
                    break;
                case GameDefinitions.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    readBluetoothMessage(readBuf);
                    break;

                case GameDefinitions.MESSAGE_TOAST:
                    Toast.makeText(ARPokemonBattleActivityHost.this,
                            msg.getData().getString(GameDefinitions.TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    Log.d(TAG, "Unhandled message value " + msg.what);
                    break;
            }
        }
    };

    private void readBluetoothMessage(byte[] in) {
        int opcode = in[0];
        Log.d(TAG, "Read message op code " + opcode);

        switch (opcode) {
            /** Payload: Don't care **/
            case BluetoothPacketHelper.OP_CODE_HANDSHAKE_RQ:
                Log.d(TAG, "Handshake requested received");
                // Send ack. Payload is don't care, so just send a null
                byte[] payload = { '\0' };
                byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_HANDSHAKE_ACK,
                        payload);

                Log.d(TAG, "Sending handshake ACK");
                mBluetoothService.write(send);
                break;
            /** Payload: Don't care **/
            case BluetoothPacketHelper.OP_CODE_READY_FOR_DATA:
                // Send move list first
                sendMoveListToClient(0);
                break;
            /** Payload: 1 byte, move index **/
            case BluetoothPacketHelper.OP_CODE_MOVE_LIST_ACK:
                int moveNum = in[1];
                Log.d(TAG, "Move list ack received, move " + moveNum);
                // 4 moves to send for move list
                if (moveNum < 3) {
                    sendMoveListToClient(moveNum + 1);
                }
                else {
                    // Client has received all the moves, so start sending bag inventory
                    sendBagItemToClient();
                }
                break;
            case BluetoothPacketHelper.OP_CODE_BAG_ITEM_ACK:
                if (!sendBagItemToClient()) {
                    // Client has received all bag items, so start sending display information
                    sendClientDisplayData();
                }
                break;
            /** Payload: 1 byte, move index **/
            case BluetoothPacketHelper.OP_CODE_MOVE_SELECT:
                int oppMoveIndex = in[1];
                Log.d(TAG, "Opponent move selected, index " + oppMoveIndex);
                mWaitingForRemote = false;

                // Reduce PP for opponent's move
                mOpponentPokemon.getMoveNumber(oppMoveIndex).reducePP();

                /**
                 * New action:
                 *  Player: 2 (client)
                 *  Action: Move selection
                 *  Extra: Move index
                 */
                mTurnQueue.add(new Action(GameDefinitions.TAG_OWNER_PLAYER_2, GameDefinitions.ACTION_MOVE,
                        oppMoveIndex));

                // If the host action has already been selected, start the battle simulation
                if (mQueuedAction) {
                    simulateBattle();
                }
                break;
            /** Payload: 1 byte, item ID **/
            case BluetoothPacketHelper.OP_CODE_ITEM_SELECT:
                int itemId = in[1];
                Item item;
                Log.d(TAG, "Opponent item selected, item ID " + itemId);
                mWaitingForRemote = false;

                // To prevent having to send Parceled item, just create an instance of the item ID
                // Client manages inventory queue anyway

                // Create a new item instance from the ID
                switch (itemId) {
                    case GameDefinitions.ITEM_ID_POTION:
                        item = new ItemList.Potion(1);
                        break;
                    case GameDefinitions.ITEM_ID_WHITEHERB:
                        item = new ItemList.WhiteHerb(1);
                        break;
                    default:
                        Log.e(TAG, "Unhandled item ID received! Item set to potion!");
                        item = new ItemList.Potion(1);
                        break;
                }

                /**
                 * New action:
                 *  Player: 2 (client)
                 *  Action: Item selection
                 *  Extra: Item
                 */
                mTurnQueue.add(new Action(GameDefinitions.TAG_OWNER_PLAYER_2, GameDefinitions.ACTION_ITEM,
                        item));

                // If the host action has already been selected, start the battle simulation
                if (mQueuedAction) {
                    simulateBattle();
                }
                break;

            /** Payload: Don't care **/
            case BluetoothPacketHelper.OP_CODE_DISPLAY_DATA_ACK:
                // Update mSendDisplayParam first, or roll it over
                mSendDiplayParam += 0x1;
                if (mSendDiplayParam > 0x2) {
                    mSendDiplayParam = 0x1;
                    mSendDisplayOpCode += 0x1;
                }
                sendClientDisplayData();
                break;
            /** Payload:   1 byte, index of battle sim step received **/
            case BluetoothPacketHelper.OP_CODE_BATTLE_SIM_STEP_ACK:
                int index = in[1];

                // Send next step
                if (index != mBattleSimulationQueueClient.size() - 1) {
                    sendBattleSimulationStep(index + 1);
                }
                else {
                    Log.d(TAG, "Clearing client simulation queue");
                    // Client received all of the simulation, so clean it for the next turn
                    mBattleSimulationQueueClient.clear();
                }
                break;
            default:
                Log.d(TAG, "Unhandled message op code " + opcode);
                break;
        }
    }


    private void sendMoveListToClient(int moveIndex) {
        /**
         * Move list:
         *   Op code: 0x2
         *   Payload:   1 byte, move index
         *              Parceled Move
         */
        Log.d(TAG, "Sending move number " + moveIndex);
        byte[] move = ParcelableUtil.marshall(mOpponentPokemon.getMoveNumber(moveIndex));
        byte[] payload = BluetoothPacketHelper.appendByteArrayToInt(moveIndex, move);

        byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_MOVE_LIST,
                payload);

        mBluetoothService.write(send);
    }

    /**
     * Send bag item to client.
     * @return True if item was sent, false if bag is empty
     */
    private boolean sendBagItemToClient() {
        /**
         * Bag item:
         *   Payload:   1 byte, item ID (defined in GameDefinitions)
         *              1 byte, item quantity to add
         */
        Log.d(TAG, "Sending next bag item");
        if (mOpponentBag.getBag().isEmpty()) {
            Log.d(TAG, "Bag is empty");
            return false;
        }

        int[] payload = { mOpponentBag.getItemAtIndex(0).getItemID(),
                mOpponentBag.getItemAtIndex(0).getQuantity() };

        byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_BAG_ITEM,
                payload);

        mBluetoothService.write(send);

        // Pop item
        mOpponentBag.getBag().remove(0);
        return true;
    }

    /**
     * Send:    Pokemon names of host (mPlayerPokemon) and client (mOpponentPokemon)
     *          Pokemon levels of host and client
     *          Pokemon current HP and max HP (mOpponentPokemon)
     */
    private void sendClientDisplayData() {
        switch (mSendDisplayOpCode) {
            case BluetoothPacketHelper.OP_CODE_POKEMON_NAME: {
                byte[] name;

                if (mSendDiplayParam == BluetoothPacketHelper.TARGET_HOST) {
                    name = mPlayerPokemon.getName().getBytes();
                }
                else if (mSendDiplayParam == BluetoothPacketHelper.TARGET_CLIENT) {
                    name = mOpponentPokemon.getName().getBytes();
                }
                else {
                    name = "Error".getBytes();
                    Log.e(TAG, "Invalid mSendDisplayParam " + mSendDiplayParam + " for op code " +
                        mSendDisplayOpCode);
                }
                byte[] param = { Integer.valueOf(mSendDiplayParam).byteValue(),
                        Integer.valueOf(name.length).byteValue() };
                byte[] payload = BluetoothPacketHelper.appendByteArrays(param, name);
                byte[] send = BluetoothPacketHelper.createPacket(mSendDisplayOpCode, payload);
                mBluetoothService.write(send);
                Log.d(TAG, "Sending display data op code " + mSendDisplayOpCode +
                        " param " + mSendDiplayParam);
                break;
            }

            case BluetoothPacketHelper.OP_CODE_POKEMON_LEVEL: {
                int[] payload = new int[2];
                payload[0] = mSendDiplayParam;
                if (mSendDiplayParam == BluetoothPacketHelper.TARGET_HOST) {
                    payload[1] = mPlayerPokemon.getLevel();
                }
                else if (mSendDiplayParam == BluetoothPacketHelper.TARGET_CLIENT) {
                    payload[1] = mOpponentPokemon.getLevel();
                }
                else {
                    Log.e(TAG, "Invalid mSendDisplayParam " + mSendDiplayParam + " for op code " +
                            mSendDisplayOpCode);
                }
                byte[] send = BluetoothPacketHelper.createPacket(mSendDisplayOpCode, payload);
                mBluetoothService.write(send);
                Log.d(TAG, "Sending display data op code " + mSendDisplayOpCode +
                        " param " + mSendDiplayParam);
                break;
            }

            case BluetoothPacketHelper.OP_CODE_POKEMON_HP: {
                int[] payload = new int[2];
                payload[0] = mSendDiplayParam;
                if (mSendDiplayParam == BluetoothPacketHelper.POKEMON_HP_CURRENT) {
                    payload[1] = (int)mOpponentPokemon.getHP();
                }
                else if (mSendDiplayParam == BluetoothPacketHelper.POKEMON_HP_MAX) {
                    payload[1] = (int)mOpponentPokemon.getMaxHP();
                }
                else {
                    Log.e(TAG, "Invalid mSendDisplayParam " + mSendDiplayParam + " for op code " +
                            mSendDisplayOpCode);
                }
                byte[] send = BluetoothPacketHelper.createPacket(mSendDisplayOpCode, payload);
                mBluetoothService.write(send);
                Log.d(TAG, "Sending display data op code " + mSendDisplayOpCode +
                        " param " + mSendDiplayParam);
                break;
            }

            default:
                Log.d(TAG, "Reached invalid send op code " + mSendDisplayOpCode);
                break;
        }
    }

    private void sendBattleSimulationStep(int index) {
        if (index > mBattleSimulationQueueClient.size() - 1) {
            Log.e(TAG, "Client queue last index: " + String.valueOf(mBattleSimulationQueueClient.size() - 1));
            Log.e(TAG, "Index attempt to send: " + index);
            return;
        }
        if (!mBattleSimulationQueueClient.isEmpty()) {
            Log.d(TAG, "Sending battle simulation step index " + index + " of " +
                String.valueOf(mBattleSimulationQueueClient.size() - 1));
            byte[] data = mBattleSimulationQueueClient.get(index);

            byte[] param = { Integer.valueOf(index).byteValue(),
                    Integer.valueOf(mBattleSimulationQueueClient.size()).byteValue(),
                    Integer.valueOf(data.length).byteValue() };
            byte[] payload = BluetoothPacketHelper.appendByteArrays(param, data);
            byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_BATTLE_SIM_STEP,
                    payload);
            mBluetoothService.write(send);
        }
    }

    /************************/
    /** Fragment Listeners **/
    /************************/
    @Override
    public void onMessageWindowFragmentInteraction() {

        if (!mMessageQueue.isEmpty()) {
            // Queued messages for really only encounter part
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
                    if (!mBattleSimulationQueueHost.isEmpty()) {
                        Log.d(TAG, "Playing next battle message");
                        playBattleSimulationStep();
                    }
                    else if (mWaitingForRemote && mQueuedAction) {
                        Log.d(TAG, "Waiting for remote");
                        return;
                    }
                    else {
                        MainWindowFragment bat = MainWindowFragment.newInstance(mPlayerPokemon.getName());
                        updateFragment(bat, MainWindowFragment.MAIN_WINDOW_TAG);
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

        if (move.getPP() == 0) {
            Toast.makeText(this, "Not enough PP!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Reduce move's PP
            move.reducePP();

            mQueuedAction = true;

            /**
             * New action:
             *  Player: 1 (host)
             *  Action: Move selection
             *  Extra: Move index
             */
            mTurnQueue.add(new Action(GameDefinitions.TAG_OWNER_PLAYER_1, GameDefinitions.ACTION_MOVE,
                    moveIndex));

            if (!mWaitingForRemote) {
                Log.d(TAG, "Client action already received in onMoveSelected, proceeding with sim");
                simulateBattle();
            }
            else {
                Log.d(TAG, "Have not received action from client yet...");
                MessageWindowFragment frag = MessageWindowFragment.newInstance(
                        getString(R.string.message_waiting));
                updateFragment(frag, MessageWindowFragment.MESSAGE_WINDOW_TAG);
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
            mQueuedAction = true;

            // As opposed to local battle, item qty needs to be reduced here, not in simulation
            item.reduceQuantity(1);

            /**
             * New action:
             *  Player: 1 (host)
             *  Action: Item selection
             *  Extra: Item
             */
            mTurnQueue.add(new Action(GameDefinitions.TAG_OWNER_PLAYER_1, GameDefinitions.ACTION_ITEM,
                    item));

            if (!mWaitingForRemote) {
                Log.d(TAG, "Client action already received in onBagItemSelected, proceeding with sim");
                simulateBattle();
            }
            else {
                Log.d(TAG, "Have not received action from client yet...");
                MessageWindowFragment frag = MessageWindowFragment.newInstance(
                        getString(R.string.message_waiting));
                updateFragment(frag, MessageWindowFragment.MESSAGE_WINDOW_TAG);
            }

        }
    }
}