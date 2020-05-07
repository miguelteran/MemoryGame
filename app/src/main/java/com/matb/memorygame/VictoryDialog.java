package com.matb.memorygame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class VictoryDialog extends DialogFragment
{
    public interface VictoryDialogListener
    {
        void onNewGameClick(DialogFragment dialog);
    }

    VictoryDialogListener listener;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            listener = (VictoryDialogListener) context;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().get(getString(R.string.winner_key)).toString() + " won the game!")
                .setPositiveButton(R.string.new_game_button, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        listener.onNewGameClick(VictoryDialog.this);
                    }
                });

        return builder.create();
    }
}
