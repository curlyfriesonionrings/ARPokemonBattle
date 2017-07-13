package org.artoolkit.ar.ARPokemonBattle.Window;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessageWindowFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MessageWindowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageWindowFragment extends Fragment {
    public static final String MESSAGE_WINDOW_TAG = "message_window_fragment";

    private static final String ARG_MESSAGE = "display_message";

    private String mDisplayMessage;

    private OnFragmentInteractionListener mListener;

    public MessageWindowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param msg String to display in the window
     * @return A new instance of fragment MessageWindowFragment.
     */
    public static MessageWindowFragment newInstance(String msg) {
        MessageWindowFragment fragment = new MessageWindowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, msg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDisplayMessage = getArguments().getString(ARG_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_window, container, false);

        // Touch listener
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN && mListener != null) {
                    mListener.onMessageWindowFragmentInteraction();
                }
                return true;
            }
        });

        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        messageTextView.setText(mDisplayMessage);

        // Hide down arrow image for "Waiting" message
        if (mDisplayMessage.equals(getString(R.string.message_waiting))) {
            ImageView down = (ImageView) view.findViewById(R.id.messageDownImage);
            if (down != null) {
                down.setVisibility(View.GONE);
            }
        }

        return view;
    }


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
        void onMessageWindowFragmentInteraction();
    }
}
