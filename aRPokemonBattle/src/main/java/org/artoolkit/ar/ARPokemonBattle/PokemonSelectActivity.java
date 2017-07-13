package org.artoolkit.ar.ARPokemonBattle;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.ARPokemonBattle.Bluetooth.BluetoothFragment;
import org.artoolkit.ar.ARPokemonBattle.RecyclerView.PokeViewAdapter;
import org.artoolkit.ar.ARPokemonBattle.RecyclerView.PokeViewModel;
import org.artoolkit.ar.ARPokemonBattle.RecyclerView.PokeViewClickListener;
import org.artoolkit.ar.ARPokemonBattle.Util.BluetoothPacketHelper;
import org.artoolkit.ar.ARPokemonBattle.Util.DividerItemDecoration;
import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class PokemonSelectActivity extends AppCompatActivity {
    private static final String TAG = PokemonSelectActivity.class.getSimpleName();

    public static final String ARG_MODE = "mode";

    public static final int POKEMON_SELECT_BULBASAUR_INDEX = 0;
    public static final int POKEMON_SELECT_CHARMANDER_INDEX = 1;
    public static final int POKEMON_SELECT_SQUIRTLE_INDEX = 2;
    public static final int POKEMON_SELECT_PIKACHU_INDEX = 3;
    public static final int POKEMON_SELECT_MEOWTH_INDEX = 4;
    public static final int POKEMON_SELECT_EEVEE_INDEX = 5;

    /** Static pokemon select list **/
    public static PokeViewModel[] POKEMON_SELECT_LIST = {
            new PokeViewModel(false, R.drawable.ic_bulbasaur, GameDefinitions.MODEL_PATH_BULBASAUR,
                    "Bulbasaur", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Growl, Tackle, Vine Whip, Razor Leaf"),
            new PokeViewModel(false, R.drawable.ic_charmander, GameDefinitions.MODEL_PATH_CHARMANDER,
                    "Charmander", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Growl, Cut, Ember, Smokescreen"),
            new PokeViewModel(false, R.drawable.ic_squirtle, GameDefinitions.MODEL_PATH_SQUIRTLE,
                    "Squirtle", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Tackle, Tail Whip, Water Gun, Withdraw"),
            new PokeViewModel(false, R.drawable.ic_pikachu, GameDefinitions.MODEL_PATH_PIKACHU,
                    "Pikachu", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Tail Whip, Thunder Shock, Quick Attack, Slam"),
            new PokeViewModel(false, R.drawable.ic_meowth, GameDefinitions.MODEL_PATH_MEOWTH,
                    "Meowth", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Growl, Scratch, Bite, Screech"),
            new PokeViewModel(false, R.drawable.ic_eevee, GameDefinitions.MODEL_PATH_EEVEE,
                    "Eevee", "Lv " + GameDefinitions.POKEMON_DEFAULT_LEVEL,
                    "Tackle, Sand Attack, Swift, Quick Attack")
    };

    protected RecyclerView mRecyclerView;
    protected PokeViewAdapter mAdapter;

    private BluetoothFragment btFrag;

    // Index of selected Pokemon
    private int mSelected = -1;
    private int mOpponentSelectedIndex = -1;
    private boolean mOpponentReady = false;

    private Switch mToggleSwitch;

    private boolean mStartingBattle = false;

    // Display mode
    private int mMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pokemon_select);

        Button chooseButton = (Button) findViewById(R.id.chooseButton);
        mToggleSwitch = (Switch) findViewById(R.id.toggleSwitch);
        final TextView toggleText = (TextView) findViewById(R.id.togglebarText);

        /** Set up display based on mode **/
        mMode = getIntent().getIntExtra(ARG_MODE, GameDefinitions.MODE_LOCAL);
        switch (mMode) {
            case GameDefinitions.MODE_HOST:
                // Intentional fall-through
            case GameDefinitions.MODE_CLIENT:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                btFrag = new BluetoothFragment();
                Bundle args = new Bundle();
                args.putInt(BluetoothFragment.ARG_MODE, mMode);
                btFrag.setArguments(args);
                transaction.replace(R.id.bluetoothContainer, btFrag);
                transaction.commit();

                if (chooseButton != null) {
                    chooseButton.setVisibility(View.GONE);
                }

                if (mToggleSwitch != null) {
                    mToggleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (mSelected < 0) {
                                Toast.makeText(PokemonSelectActivity.this, "You can't fight a Pokemon by yourself!",
                                        Toast.LENGTH_SHORT).show();
                                mToggleSwitch.setChecked(false);
                                return;
                            }
                            if (toggleText != null) {
                                if (b) {
                                    if (btFrag.sendMessage(BluetoothPacketHelper.OP_CODE_POKEMON_SELECT, mSelected, b)) {
                                        toggleText.setText(R.string.choose_pokemon);
                                        toggleText.setTextColor(Color.BLACK);

                                        checkForProgression();
                                    }
                                    else
                                    {
                                        mToggleSwitch.setChecked(false);
                                    }
                                }
                                else {
                                    toggleText.setText(R.string.ready_question);
                                    toggleText.setTextColor(Color.GRAY);

                                    btFrag.sendMessage(BluetoothPacketHelper.OP_CODE_POKEMON_SELECT, -1, b);
                                }
                            }

                        }
                    });
                }
                break;

            case GameDefinitions.MODE_LOCAL:
                if (chooseButton != null) {
                    chooseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mSelected < 0) {
                                Toast.makeText(PokemonSelectActivity.this, "You can't fight a Pokemon by yourself!",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            /** Select opponent **/
                            Random rand = new Random();
                            int opponent = rand.nextInt(POKEMON_SELECT_EEVEE_INDEX + 1);

                            Intent battle = new Intent(PokemonSelectActivity.this, ARPokemonBattleActivity.class);
                            battle.putExtra(ARPokemonBattleActivity.ARG_PLAYER_POKEMON, mSelected);
                            battle.putExtra(ARPokemonBattleActivity.ARG_OPPONENT_POKEMON, opponent);
                            finish();
                            startActivity(battle);
                        }
                    });
                }
                if (mToggleSwitch != null) {
                    mToggleSwitch.setVisibility(View.GONE);
                }
                if (toggleText != null) {
                    toggleText.setVisibility(View.GONE);
                }
                break;
        }

        /** Set up recycler view */
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final ArrayList<PokeViewModel> list = new ArrayList<>();
        list.addAll(Arrays.asList(POKEMON_SELECT_LIST));
        mAdapter = new PokeViewAdapter(list);

        mRecyclerView.addOnItemTouchListener(
                new PokeViewClickListener(this, new PokeViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        for (PokeViewModel i : list) {
                            i.setSelected(false);
                        }
                        list.get(position).setSelected(true);
                        mSelected = position;
                        mAdapter.notifyDataSetChanged();
                    }
                })
        );
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider_h)));
    }

    public void read(byte[] data) {
        mOpponentSelectedIndex = data[1];
        mOpponentReady = data[2] == 1;

        Log.d(TAG, "Opponent selected: " + mOpponentSelectedIndex);

        checkForProgression();
    }

    private void checkForProgression() {
        if (mToggleSwitch.isChecked() && mOpponentReady) {
            if (mMode == GameDefinitions.MODE_HOST) {
                Intent battle = new Intent(PokemonSelectActivity.this, ARPokemonBattleActivityHost.class);
                battle.putExtra(ARPokemonBattleActivityHost.ARG_PLAYER_POKEMON, mSelected);
                battle.putExtra(ARPokemonBattleActivityHost.ARG_OPPONENT_POKEMON, mOpponentSelectedIndex);

                Log.d(TAG, "Finish activity for host mode");
                finish();
                if (!mStartingBattle) {
                    startActivity(battle);
                }
                else {
                    Log.d(TAG, "Battle is already starting...");
                }
                mStartingBattle = true;
            }
            else {
                // Only other option is client
                Intent battle = new Intent(PokemonSelectActivity.this, ARPokemonBattleActivityClient.class);
                battle.putExtra(ARPokemonBattleActivityClient.ARG_PLAYER_POKEMON, mSelected);
                battle.putExtra(ARPokemonBattleActivityClient.ARG_OPPONENT_POKEMON, mOpponentSelectedIndex);

                Log.d(TAG, "Finish activity for client mode");
                finish();
                startActivity(battle);
            }
        }
    }
}