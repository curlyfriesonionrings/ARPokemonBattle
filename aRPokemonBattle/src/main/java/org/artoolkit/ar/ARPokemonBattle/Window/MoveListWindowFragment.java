package org.artoolkit.ar.ARPokemonBattle.Window;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.Pokemon.Move;
import org.artoolkit.ar.ARPokemonBattle.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoveListWindowFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoveListWindowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoveListWindowFragment extends Fragment {
    public static final String MOVE_WINDOW_TAG = "moves_window_fragment";

    private static final String ARG_MOVE_1 = "move_1";
    private static final String ARG_MOVE_2 = "move_2";
    private static final String ARG_MOVE_3 = "move_3";
    private static final String ARG_MOVE_4 = "move_4";

    private ArrayList<Move> mMoveList;
    private int mCurrentMoveSelected;
    private TextView mPPTextView, mTypeTextView;

    /** Cursor and alignment **/
    private View mVerticalShim, mHorizontalShim;
    private FrameLayout mCursor;

    private Button mMove1Button, mMove2Button, mMove3Button, mMove4Button;
    
    private OnFragmentInteractionListener mListener;

    public MoveListWindowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param m1 Move 1.
     * @param m2 Move 2.
     * @param m3 Move 3.
     * @param m4 Move 4.
     * @return A new instance of fragment MoveListWindowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoveListWindowFragment newInstance(Move m1, Move m2, Move m3, Move m4) {
        MoveListWindowFragment fragment = new MoveListWindowFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVE_1, m1);
        args.putParcelable(ARG_MOVE_2, m2);
        args.putParcelable(ARG_MOVE_3, m3);
        args.putParcelable(ARG_MOVE_4, m4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMoveList = new ArrayList<>();

            mMoveList.add((Move)getArguments().getParcelable(ARG_MOVE_1));
            mMoveList.add((Move)getArguments().getParcelable(ARG_MOVE_2));
            mMoveList.add((Move)getArguments().getParcelable(ARG_MOVE_3));
            mMoveList.add((Move)getArguments().getParcelable(ARG_MOVE_4));

            mCurrentMoveSelected = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_list_window, container, false);

        mMove1Button = (Button) view.findViewById(R.id.move1Button);
        mMove2Button = (Button) view.findViewById(R.id.move2Button);
        mMove3Button = (Button) view.findViewById(R.id.move3Button);
        mMove4Button = (Button) view.findViewById(R.id.move4Button);

        mMove1Button.setOnClickListener(onButtonPressed);
        mMove2Button.setOnClickListener(onButtonPressed);
        mMove3Button.setOnClickListener(onButtonPressed);
        mMove4Button.setOnClickListener(onButtonPressed);

        // Update texts
        mMove1Button.setText(mMoveList.get(0).getName());
        mMove2Button.setText(mMoveList.get(1).getName());
        mMove3Button.setText(mMoveList.get(2).getName());
        mMove4Button.setText(mMoveList.get(3).getName());

        mPPTextView = (TextView) view.findViewById(R.id.ppFractionText);
        mTypeTextView = (TextView) view.findViewById(R.id.typeText);

        mCursor = (FrameLayout) view.findViewById(R.id.moveCursor);
        mCursor.setVisibility(View.GONE);

        mVerticalShim = view.findViewById(R.id.centerVerticalShim);
        mHorizontalShim = view.findViewById(R.id.centerHorizontalShim);

        return view;
    }

    private void updateMoveDetails() {
        String pp = mMoveList.get(mCurrentMoveSelected).getPP() + "/" +
                mMoveList.get(mCurrentMoveSelected).getMaxPP();
        mPPTextView.setText(pp);

        mTypeTextView.setText(mMoveList.get(mCurrentMoveSelected).getTypeString());

        // Highlight selected move
        RelativeLayout.LayoutParams align =
                new RelativeLayout.LayoutParams(mCursor.getLayoutParams());
        switch (mCurrentMoveSelected) {
            case 0:
                align.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                align.addRule(RelativeLayout.ALIGN_PARENT_START);
                align.addRule(RelativeLayout.ABOVE, mVerticalShim.getId());
                break;
            case 1:
                align.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                align.addRule(RelativeLayout.END_OF, mHorizontalShim.getId());
                align.addRule(RelativeLayout.ABOVE, mVerticalShim.getId());
                break;
            case 2:
                align.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                align.addRule(RelativeLayout.ALIGN_PARENT_START);
                align.addRule(RelativeLayout.BELOW, mVerticalShim.getId());
                break;
            case 3:
                align.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                align.addRule(RelativeLayout.END_OF, mHorizontalShim.getId());
                align.addRule(RelativeLayout.BELOW, mVerticalShim.getId());
                break;
            default:
                break;
        }

        mCursor.setLayoutParams(align);
    }

    View.OnClickListener onButtonPressed = new View.OnClickListener() {
        public void onClick(View v) {
            if (mCursor.getVisibility() == View.GONE) {
                mCursor.setVisibility(View.VISIBLE);
            }

            int currentSelection;
            switch (v.getId()) {
                case R.id.move1Button:
                    currentSelection = 0;
                    break;
                case R.id.move2Button:
                    currentSelection = 1;
                    break;
                case R.id.move3Button:
                    currentSelection = 2;
                    break;
                case R.id.move4Button:
                    currentSelection = 3;
                    break;
                default:
                    currentSelection = 0;
                    break;
            }

            if (currentSelection == mCurrentMoveSelected) {
                if (mListener != null) {
                    mListener.onMoveSelected(mCurrentMoveSelected);
                }
            }
            else {
                mCurrentMoveSelected = currentSelection;
                updateMoveDetails();
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
        void onMoveSelected(int moveIndex);
    }
}
