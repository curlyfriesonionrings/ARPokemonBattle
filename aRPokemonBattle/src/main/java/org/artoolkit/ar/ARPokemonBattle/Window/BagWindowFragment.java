package org.artoolkit.ar.ARPokemonBattle.Window;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.artoolkit.ar.ARPokemonBattle.Bag.Bag;
import org.artoolkit.ar.ARPokemonBattle.Bag.Item;
import org.artoolkit.ar.ARPokemonBattle.R;
import org.artoolkit.ar.ARPokemonBattle.RecyclerView.BagViewAdapter;
import org.artoolkit.ar.ARPokemonBattle.RecyclerView.BagViewClickListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BagWindowFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BagWindowFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BagWindowFragment extends Fragment {
    public static final String BAG_WINDOW_TAG = "bag_window_fragment";

    private static final String ARG_BAG = "arg_bag";

    private ArrayList<Item> mItemList;
    private int mCurrentItemSelected;
    private TextView mItemDescription;

    protected RecyclerView mRecyclerView;
    protected BagViewAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    public BagWindowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param bag Bag containing items to list
     * @return A new instance of fragment MoveListWindowFragment.
     */
    public static BagWindowFragment newInstance(Bag bag) {
        BagWindowFragment fragment = new BagWindowFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_BAG, bag.getBag());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItemList = getArguments().getParcelableArrayList(ARG_BAG);

            mCurrentItemSelected = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bag_window, container, false);
        
        mItemDescription = (TextView) view.findViewById(R.id.itemDescription);
        mItemDescription.setText(getString(R.string.select_item));

        /** Set up recycler view */
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bagInventoryWindow);

        mAdapter = new BagViewAdapter(mItemList);

        mRecyclerView.addOnItemTouchListener(
                new BagViewClickListener(getActivity(), new BagViewClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // Clear selection for next run
                        for (Item i : mItemList) {
                            i.setSelected(false);
                        }
                        // Double-clicked
                        if (position == mCurrentItemSelected) {
                            if (mListener != null) {
                                mListener.onBagItemSelected(position);
                            }
                        }
                        else {
                            mItemList.get(position).setSelected(true);
                            mCurrentItemSelected = position;
                            mAdapter.notifyDataSetChanged();

                            updateDescription(position);
                        }
                    }
                })
        );
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(
//                ContextCompat.getDrawable(getApplicationContext(), R.drawable.list_divider_h)));

        return view;
    }

    private void updateDescription(int position) {
        Item item = mItemList.get(position);

        mItemDescription.setText(item.getDescription());
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
        for (Item i : mItemList) {
            i.setSelected(false);
        }
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
        void onBagItemSelected(int moveIndex);
    }
}
