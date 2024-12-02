package de.streblow.gomoku;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.fragment.app.DialogFragment;

public class NewGameFragment extends DialogFragment {

    public interface OnDialogFragmentDismissedListener {
        void onDialogFragmentDismissedListener(int difficulty, int mode);
    }

    private int mDifficulty;
    private int mMode;
    public OnDialogFragmentDismissedListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_new_game, container, false);
        mDifficulty = 1;
        mMode = 1;
        RadioGroup radioGroupDifficulty = (RadioGroup) fragmentView.findViewById(R.id.new_game_difficulty);
        radioGroupDifficulty.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.new_game_difficulty_easy:
                        mDifficulty = 1;
                        break;
                    case R.id.new_game_difficulty_medium:
                        mDifficulty = 2;
                        break;
                    case R.id.new_game_difficulty_hard:
                        mDifficulty = 3;
                        break;
                }
            }
        });
        RadioGroup radioGroupMode = (RadioGroup) fragmentView.findViewById(R.id.new_game_mode);
        radioGroupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.new_game_mode_single_player_first:
                        mMode = 1;
                        break;
                    case R.id.new_game_mode_single_player_second:
                        mMode = 2;
                        break;
                    case R.id.new_game_mode_multi_player:
                        mMode = 3;
                        break;
                }
            }
        });
        return fragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnDialogFragmentDismissedListener)
                listener = (OnDialogFragmentDismissedListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnDialogFragmentDismissedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        listener.onDialogFragmentDismissedListener(mDifficulty, mMode);
    }
}