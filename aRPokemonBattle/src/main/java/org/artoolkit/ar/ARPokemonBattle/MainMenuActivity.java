package org.artoolkit.ar.ARPokemonBattle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.artoolkit.ar.ARPokemonBattle.Util.GameDefinitions;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = MainMenuActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button host = (Button) findViewById(R.id.mainMenuHostButton);
        Button connect = (Button) findViewById(R.id.mainMenuConnectButton);
        Button local = (Button) findViewById(R.id.mainMenuLocalButton);

        if (host != null) {
            host.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainMenuActivity.this, PokemonSelectActivity.class);
                    i.putExtra(PokemonSelectActivity.ARG_MODE, GameDefinitions.MODE_HOST);
                    startActivity(i);
                }
            });
        }

        if (connect != null) {
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainMenuActivity.this, PokemonSelectActivity.class);
                    i.putExtra(PokemonSelectActivity.ARG_MODE, GameDefinitions.MODE_CLIENT);
                    startActivity(i);
                }
            });
        }

        if (local != null) {
            local.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainMenuActivity.this, PokemonSelectActivity.class);
                    i.putExtra(PokemonSelectActivity.ARG_MODE, GameDefinitions.MODE_LOCAL);
                    startActivity(i);
                }
            });
        }
    }
}
