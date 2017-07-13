package org.artoolkit.ar.ARPokemonBattle.Window;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainWindowFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainWindowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainWindowFragment extends Fragment {
    public static final String MAIN_WINDOW_TAG = "main_window_fragment";

    private static final String ARG_POKEMON_NAME = "pokemon_name";

    private String mPokemonName;

    private OnFragmentInteractionListener mListener;

    public MainWindowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainWindowFragment.
     */
    public static MainWindowFragment newInstance(String name) {
        MainWindowFragment fragment = new MainWindowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POKEMON_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPokemonName = getArguments().getString(ARG_POKEMON_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_window, container, false);

        TextView left = (TextView) view.findViewById(R.id.leftActionWindow);
        String str = "What will " + mPokemonName + " do?";
        left.setText(str);

        // Buttons and listeners
        Button fightButton = (Button) view.findViewById(R.id.fightButton);
        Button bagButton = (Button) view.findViewById(R.id.bagButton);
        Button pokemonButton = (Button) view.findViewById(R.id.pokemonButton);
        Button runButton = (Button) view.findViewById(R.id.runButton);

        fightButton.setOnClickListener(onButtonPressed);
        bagButton.setOnClickListener(onButtonPressed);
        pokemonButton.setOnClickListener(onButtonPressed);
        runButton.setOnClickListener(onButtonPressed);

        return view;
    }

    View.OnClickListener onButtonPressed = new View.OnClickListener() {
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onMainButtonInteraction(v.getId());
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onMainButtonInteraction(int btnId);
    }
}
