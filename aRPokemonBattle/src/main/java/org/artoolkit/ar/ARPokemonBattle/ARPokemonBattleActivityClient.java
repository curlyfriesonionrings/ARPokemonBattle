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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.ARPokemonBattle.Bag.Bag;
import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.Bluetooth.BluetoothService;
import org.artoolkit.ar.ARPokemonBattle.Pokemon.Move;
import org.artoolkit.ar.ARPokemonBattle.Util.BluetoothPacketHelper;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;
import org.artoolkit.ar.ARPokemonBattle.Util.ItemList;
import org.artoolkit.ar.ARPokemonBattle.Util.ParcelableUtil;
import org.artoolkit.ar.ARPokemonBattle.Window.BagWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MainWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MessageWindowFragment;
import org.artoolkit.ar.ARPokemonBattle.Window.MoveListWindowFragment;
import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Client. Basic functionality that responds to messages sent by host. Sends some commands to host
 * to progress turns.
 */
public class ARPokemonBattleActivityClient extends ARActivity implements
    MessageWindowFragment.OnFragmentInteractionListener,
    MainWindowFragment.OnFragmentInteractionListener,
    MoveListWindowFragment.OnFragmentInteractionListener,
    BagWindowFragment.OnFragmentInteractionListener {

    private static final String TAG = ARPokemonBattleActivityClient.class.getSimpleName();

    /** Index value **/
    public static final String ARG_PLAYER_POKEMON = "player_pokemon";
    public static final String ARG_OPPONENT_POKEMON = "opponent_pokemon";

    /** Time in ms to wait before retry a display data request if no ACK is received **/
    private static final int HANDSHAKE_RETRY_TIME = 2000;

    private SimpleNativeRenderer simpleNativeRenderer;

    private int mGameState;
    private ArrayList<String> mMessageQueue;

    private TextView mPlayerHPTextView;
    private View mPlayerHPBarView, mOppHPBarView;

    /** Updated via BluetoothService message, used for display **/
    private int mPokemonHP, mPokemonMaxHP;
    private String mPokemonName, mOppPokemonName;

    /** Updated via BluetoothService, needed for reference and self-management **/
    private ArrayList<Move> mMoveList;

    private Bag mPlayerBag;

    private BluetoothService mBluetoothService;
    private boolean mIsBound;

    /** Timer to retry handshake request **/
    private Handler mTimer = new Handler();
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "Handshake retry timer triggered");
            sendReadyForData(BluetoothPacketHelper.OP_CODE_HANDSHAKE_RQ);
        }
    };

    /**
     * When the host simulates the battle, each of the operations gets sent to the
     * client so the client can run through the correct messages at its own pace
     */
    private ArrayList<byte[]> mBattleSimulationQueue;

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
            sendReadyForData(BluetoothPacketHelper.OP_CODE_HANDSHAKE_RQ);
            Log.d(TAG, "Sent handshake request to host");
            // Set up timer to retry if ACK is not received
            mTimer.postDelayed(mTimerRunnable, HANDSHAKE_RETRY_TIME);
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
        bindService(new Intent(ARPokemonBattleActivityClient.this,
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

        // Service should have been started by PokemonSelectActivity
        doBindService();

        // Set up Pokemon models
        int pokeIndex = getIntent().getIntExtra(ARG_PLAYER_POKEMON, -1);
        if (pokeIndex < 0 || pokeIndex > PokemonSelectActivity.POKEMON_SELECT_EEVEE_INDEX) {
                Log.e(TAG, "Unknown Pokemon selected! Default to 0");
                pokeIndex = 0;
        }

        int oppPokeIndex = getIntent().getIntExtra(ARG_OPPONENT_POKEMON, -1);
        if (oppPokeIndex < 0 || oppPokeIndex > PokemonSelectActivity.POKEMON_SELECT_EEVEE_INDEX) {
                Log.e(TAG, "Unknown opponent Pokemon selected! Default to 0");
                oppPokeIndex = 0;
        }

        // Renderer: Opponent takes the first pattern, player gets the second pattern
        simpleNativeRenderer = new SimpleNativeRenderer(
                PokemonSelectActivity.POKEMON_SELECT_LIST[oppPokeIndex].getModelPath(),
                PokemonSelectActivity.POKEMON_SELECT_LIST[pokeIndex].getModelPath());

        mMoveList = new ArrayList<>();

        mBattleSimulationQueue = new ArrayList<>();

        // Bag items received by host after move list
        mPlayerBag = new Bag();

        // Pokemon view windows are updated upon receiving Bluetooth messages
        mPokemonHP = mPokemonMaxHP = -1;

        // Set up views needed for updating
        mPlayerHPTextView = (TextView) findViewById(R.id.hpText);
        mPlayerHPBarView = findViewById(R.id.hpBar);
        mPlayerHPBarView.setPivotX(0f);
        mOppHPBarView = findViewById(R.id.oppHpBar);
        mOppHPBarView.setPivotX(0f);

        // Hide all views until required information is obtained via Bluetooth
        FrameLayout msgWindow = (FrameLayout) findViewById(R.id.windowFragment);
        msgWindow.setVisibility(View.INVISIBLE);
        RelativeLayout pokeWindow = (RelativeLayout) findViewById(R.id.playerPokemonWindow);
        pokeWindow.setVisibility(View.INVISIBLE);
        RelativeLayout opPokeWindow = (RelativeLayout) findViewById(R.id.opponentPokemonWindow);
        opPokeWindow.setVisibility(View.INVISIBLE);

        // Order information is sent by host: names, levels, HP
        // Show all views when HP is received

        /**
         * First encounter messages can only happen when Pokemon names are received
         * (in updatePokemonName())
         */
        mPokemonName = mOppPokemonName = null;
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
        String hp = mPokemonHP + "/" + mPokemonMaxHP;
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
                    // don't need to do anything on write
                    break;
                case GameDefinitions.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    readBluetoothMessage(readBuf);
                    break;

                case GameDefinitions.MESSAGE_TOAST:
                    Toast.makeText(ARPokemonBattleActivityClient.this,
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

        switch (opcode) {
            case BluetoothPacketHelper.OP_CODE_HANDSHAKE_ACK:
                Log.d(TAG, "Handshake ACK received");
                sendReadyForData(BluetoothPacketHelper.OP_CODE_READY_FOR_DATA);
                // Cancel retry timer
                Log.d(TAG, "Stopping retry timer");
                mTimer.removeCallbacks(mTimerRunnable);
                break;
            case BluetoothPacketHelper.OP_CODE_MOVE_LIST:
                addMoveToMoveList(in);
                break;
            case BluetoothPacketHelper.OP_CODE_BAG_ITEM:
                addItemToBag(in);
                break;
            case BluetoothPacketHelper.OP_CODE_POKEMON_NAME:
                updatePokemonName(in);
                sendReadyForData(BluetoothPacketHelper.OP_CODE_DISPLAY_DATA_ACK);
                break;
            case BluetoothPacketHelper.OP_CODE_POKEMON_LEVEL:
                updatePokemonLevel(in);
                sendReadyForData(BluetoothPacketHelper.OP_CODE_DISPLAY_DATA_ACK);
                break;
            case BluetoothPacketHelper.OP_CODE_POKEMON_HP:
                updatePokemonHPValues(in);
                sendReadyForData(BluetoothPacketHelper.OP_CODE_DISPLAY_DATA_ACK);
                break;
            case BluetoothPacketHelper.OP_CODE_BATTLE_SIM_STEP:
                addBattleSimulationStep(in);
                break;
            default:
                Log.d(TAG, "Unhandled message op code " + opcode);
                break;
        }
    }

    private void addMoveToMoveList(byte[] in) {
        /**
         * Move list:
         *   Payload:   1 byte, move index
         *              Parceled Move
         */
        int moveNum = in[1];
        Log.d(TAG, "Move list item received, move index " + moveNum);

        // Cut off the first two bytes
        byte[] moveBytes = Arrays.copyOfRange(in, 2, in.length);
        Move m = ParcelableUtil.unmarshall(moveBytes, Move.CREATOR);
        mMoveList.add(moveNum, m);

        // Send move ack
        byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_MOVE_LIST_ACK,
                moveNum);
        mBluetoothService.write(send);
    }

    private void addItemToBag(byte[] in) {
        /**
         * Bag item:
         *   Payload:   1 byte, item ID (defined in GameDefinitions)
         *              1 byte, item quantity to add
         */
        int id = in[1]; int qty = in[2];
        Log.d(TAG, "Received item ID " + id + " with qty " + qty);

        switch (id) {
            case GameDefinitions.ITEM_ID_POTION:
                mPlayerBag.addToBag(new ItemList.Potion(qty));
                break;
            case GameDefinitions.ITEM_ID_WHITEHERB:
                mPlayerBag.addToBag(new ItemList.WhiteHerb(qty));
                break;
            case GameDefinitions.ITEM_ID_POKEBALL:
                mPlayerBag.addToBag(new ItemList.Pokeball(qty));
                break;
            default:
                Log.e(TAG, "Unrecognized item ID!");
                break;
        }

        // Send ack
        sendReadyForData(BluetoothPacketHelper.OP_CODE_BAG_ITEM_ACK);
    }

    private void updatePokemonName(byte[] in) {
        /**
         * Pokemon name:
         *   Payload:   1 byte, 0x1 for name of host or 0x2 for name of client
         *              1 byte, length of String payload
         *              String, name of Pokemon
         */
        int param = in[1];
        Log.d(TAG, "Name received with param " + param);

        switch (param) {
            case BluetoothPacketHelper.TARGET_HOST: {
                // Cut out op_code and params
                int start = 3;
                int nameLen = in[2];
                byte[] nameBytes =  Arrays.copyOfRange(in, start, start + nameLen);
                mOppPokemonName = new String(nameBytes);
                Log.d(TAG, "Opponent name: " + mOppPokemonName);

                TextView oppPokemon = (TextView) findViewById(R.id.oppPokemonNameText);
                oppPokemon.setText(mOppPokemonName);
                break;
            }

            case BluetoothPacketHelper.TARGET_CLIENT: {
                // Cut out op_code and param
                int start = 3;
                int nameLen = in[2];
                byte[] nameBytes =  Arrays.copyOfRange(in, start, start + nameLen);
                mPokemonName = new String(nameBytes);
                Log.d(TAG, "Pokemon name: " + mPokemonName);

                // Update view
                TextView playerPokemon = (TextView) findViewById(R.id.pokemonNameText);
                playerPokemon.setText(mPokemonName);
                break;
            }

            default:
                Log.e(TAG, "Unrecognized param value " + param);
                break;
        }

        if (mPokemonName != null && mOppPokemonName != null) {
            // At this point, we have enough information to show the encounter messages
            FrameLayout msgWindow = (FrameLayout) findViewById(R.id.windowFragment);
            msgWindow.setVisibility(View.VISIBLE);

            String encounterMsg = mOppPokemonName + " wants to fight!";
            MessageWindowFragment encounter = MessageWindowFragment.newInstance(encounterMsg);
            mMessageQueue.add("Go! " + mPokemonName + "!");

            updateFragment(encounter, MessageWindowFragment.MESSAGE_WINDOW_TAG);
        }
    }

    private void updatePokemonLevel(byte[] in) {
        /**
         * Pokemon level:
         *   Payload:   1 byte, 0x1 for level of host or 0x2 for level of client
         *              1 byte, level of Pokemon
         */
        int param = in[1];
        Log.d(TAG, "Level received with param " + param);

        switch (param) {
            case BluetoothPacketHelper.TARGET_HOST: {
                int oppPokemonLvl = in[2];
                Log.d(TAG, "Opponent lvl: " + oppPokemonLvl);

                TextView oppLvl = (TextView) findViewById(R.id.oppPokemonLevelText);
                String lv = "Lv " + oppPokemonLvl;
                oppLvl.setText(lv);
                break;
            }

            case BluetoothPacketHelper.TARGET_CLIENT: {
                int pokemonLvl = in[2];
                Log.d(TAG, "Pokemon lvl: " + pokemonLvl);

                TextView playerLvl = (TextView) findViewById(R.id.pokemonLevelText);
                String lv = "Lv " + pokemonLvl;
                playerLvl.setText(lv);
                break;
            }

            default:
                Log.e(TAG, "Unrecognized param value " + param);
                break;
        }

        // At this point, we should have the information to display the battle windows
        RelativeLayout pokeWindow = (RelativeLayout) findViewById(R.id.playerPokemonWindow);
        pokeWindow.setVisibility(View.VISIBLE);
        RelativeLayout opPokeWindow = (RelativeLayout) findViewById(R.id.opponentPokemonWindow);
        opPokeWindow.setVisibility(View.VISIBLE);
    }

    private void updatePokemonHPValues(byte[] in) {
        /**
         * Pokemon HP:
         *   Payload:   1 byte, 0x1 for current client HP or 0x2 for client max HP
         *              1 byte, HP value
         */
        int param = in[1];
        Log.d(TAG, "HP received with param " + param);

        switch (param) {
            case BluetoothPacketHelper.POKEMON_HP_CURRENT:
                mPokemonHP = in[2];
                Log.d(TAG, "Current HP: " + mPokemonHP);
                break;

            case BluetoothPacketHelper.POKEMON_HP_MAX:
                mPokemonMaxHP = in[2];
                Log.d(TAG, "Max HP: " + mPokemonMaxHP);
                break;

            default:
                Log.e(TAG, "Unrecognized param value " + param);
                break;
        }

        if (mPokemonHP > -1 && mPokemonMaxHP > -1) {
            updatePlayerHPText();
        }
    }

    private void sendReadyForData(int opcode) {
        /**
         * Ready for data:
         *   Payload:   "Don't care", used to notify host that client is ready for data
         */
        Log.d(TAG, "Ready for data with op code " + opcode);

        // Payload is don't care, so just send a null
        byte[] payload = { '\0' };
        byte[] send = BluetoothPacketHelper.createPacket(opcode, payload);

        mBluetoothService.write(send);
    }

    private void addBattleSimulationStep(byte[] in) {
        /**
         * Battle sim step:
         *   Payload:   1 byte, index of battle sim step in host's queue
         *              1 byte, max index in host's queue
         *              1 byte, length of simulation step packet
         *              Byte[], packet of battle simulation step
         */
        int index = in[1];
        int size = in[2];
        int length = in[3];
        int dataStart = 4;
        byte[] data = Arrays.copyOfRange(in, dataStart, dataStart + length);
        mBattleSimulationQueue.add(data);

        Log.d(TAG, "Battle simulation step " + index + " of " + String.valueOf(size - 1) + " received");

        // Send ACK
        byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_BATTLE_SIM_STEP_ACK,
                index);

        mBluetoothService.write(send);

        // If we receive the whole queue, we can display to the user
        if (index == size - 1) {
            Log.d(TAG, "Received final message, turn reset");
            mWaitingForRemote = false;
            playBattleSimulationStep();
        }
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
        if (!mBattleSimulationQueue.isEmpty()) {
            // Parse simulation message. We can always assume that if there is an item in the
            // queue, the first item will always be a display message item, since we always
            // leave it in that state when we finish with this method
            byte[] data = mBattleSimulationQueue.get(0);

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

            mBattleSimulationQueue.remove(0);
            Log.d(TAG, "Popped item from queue, size now " + mBattleSimulationQueue.size());

            // Peek at next item and handle if necessary
            while (!nextSimulationIsMessage()) {
                byte[] nextData = mBattleSimulationQueue.get(0);
                int opcode = nextData[0];
                switch (opcode) {
                    case BluetoothPacketHelper.OP_CODE_BATTLE_MESSAGE:
                        break;

                    case BluetoothPacketHelper.OP_CODE_ANIMATE_HEALTH:
                        animateHealth(nextData);
                        mBattleSimulationQueue.remove(0);
                        Log.d(TAG, "Popped item from queue, size now " + mBattleSimulationQueue.size());
                        break;

                    case BluetoothPacketHelper.OP_CODE_POKEMON_FAINTED:
                        pokemonFainted(nextData);
                        mBattleSimulationQueue.remove(0);
                        Log.d(TAG, "Popped item from queue, size now " + mBattleSimulationQueue.size());
                        break;

                    case BluetoothPacketHelper.OP_CODE_POKEMON_EXHAUSTED:
                        pokemonExhausted(nextData);
                        mBattleSimulationQueue.remove(0);
                        Log.d(TAG, "Popped item from queue, size now " + mBattleSimulationQueue.size());
                        break;

                    default:
                        Log.e(TAG, "Unrecognized op code " + opcode);
                        break;
                }
            }
        }
        else {
            Log.e(TAG, "Simulation queue is empty!");
        }
    }

    private boolean nextSimulationIsMessage() {
        if (mBattleSimulationQueue.isEmpty()) {
            return true;
        }
        int op = mBattleSimulationQueue.get(0)[0];
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
                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mOppHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();
                break;
            }
            case BluetoothPacketHelper.TARGET_CLIENT: {
                Log.d(TAG, "Animate client health");
                ObjectAnimator scaleAnim = ObjectAnimator
                        .ofFloat(mPlayerHPBarView, "scaleX", currentPercent, newPercent);
                scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleAnim.setDuration(GameDefinitions.HP_ANIM_DUR);
                scaleAnim.start();

                // Calculate new HP with damage
                mPokemonHP -= in[offset];

                if (mPokemonHP < 0) { mPokemonHP = 0; }

                updatePlayerHPText();
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
        if (target == BluetoothPacketHelper.TARGET_HOST) {
            Log.d(TAG, "Host Pokemon fainted");
            faint = "The opposing " + mOppPokemonName;
            conclusion = getString(R.string.message_win);
        }
        else {
            Log.d(TAG, "Client Pokemon fainted");
            faint = mPokemonName;
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
                String msg = "The opposing " + mOppPokemonName + " " +
                        getString(R.string.message_exhaust);
                mMessageQueue.add(msg);
                mMessageQueue.add(getString(R.string.message_win));
                break;
            }

            case BluetoothPacketHelper.TARGET_CLIENT: {
                String msg = mPokemonName + " " + getString(R.string.message_exhaust);
                mMessageQueue.add(msg);
                mMessageQueue.add(getString(R.string.message_lose));
                break;
            }
        }
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

                    MainWindowFragment main = MainWindowFragment.newInstance(mPokemonName);
                    updateFragment(main, MainWindowFragment.MAIN_WINDOW_TAG);
                    break;
                case GameDefinitions.GAME_STATE_BATTLE:
                    if (!mBattleSimulationQueue.isEmpty()) {
                        Log.d(TAG, "Playing next battle message");
                        playBattleSimulationStep();
                    }
                    else if (mWaitingForRemote) {
                        Log.d(TAG, "Waiting for remote");
                        return;
                    }
                    else {
                        MainWindowFragment bat = MainWindowFragment.newInstance(mPokemonName);
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
                        mMoveList.get(0), mMoveList.get(1),
                        mMoveList.get(2), mMoveList.get(3)
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
        Move move = mMoveList.get(moveIndex);

        if (move.getPP() == 0) {
            Toast.makeText(this, "Not enough PP!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Reduce move's PP
            move.reducePP();

            Log.d(TAG, "Sending to host move index " + moveIndex);
            /** Send move selection to host **/
            byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_MOVE_SELECT,
                    moveIndex);
            mBluetoothService.write(send);

            mWaitingForRemote = true;
            MessageWindowFragment frag = MessageWindowFragment.newInstance(
                    getString(R.string.message_waiting));
            updateFragment(frag, MessageWindowFragment.MESSAGE_WINDOW_TAG);
        }
    }

    @Override
    public void onBagItemSelected(int index) {
        Item item = mPlayerBag.getItemAtIndex(index);

        if (item.getQuantity() == 0) {
            Toast.makeText(this, "You have none left!", Toast.LENGTH_SHORT).show();
        }
        else {
            // Reduce quantity
            item.reduceQuantity(1);

            int itemID = item.getItemID();

            Log.d(TAG, "Sending to host item ID " + itemID);
            /** Send item selection to host **/
            byte[] send = BluetoothPacketHelper.createPacket(BluetoothPacketHelper.OP_CODE_ITEM_SELECT,
                    itemID);
            mBluetoothService.write(send);

            mWaitingForRemote = true;
            MessageWindowFragment frag = MessageWindowFragment.newInstance(
                    getString(R.string.message_waiting));
            updateFragment(frag, MessageWindowFragment.MESSAGE_WINDOW_TAG);
        }
    }
}