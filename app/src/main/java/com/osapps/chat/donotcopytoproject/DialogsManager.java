package com.osapps.chat.donotcopytoproject;

import android.app.Dialog;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by osapps on 24/02/2018.
 */

//this class meant to hold all of the open dialogs on current session.
//we doing so cause we need to make sure we dismiss all of them when calling onStop().
public class DialogsManager {

    private static ArrayList<Dialog> openDialogsList;

    private DialogsManager() {
    }

    private static DialogsManager instance;

    public static DialogsManager getInstance() {
        if (instance == null) instance = new DialogsManager();
        return instance;
    }


    public void addDialog(Dialog dialog) {
        if (openDialogsList == null)
            openDialogsList = new ArrayList<>();

        if (!openDialogsList.contains(dialog))
            openDialogsList.add(dialog);
    }


    public void removeDialog(Dialog dialog) {
        if (openDialogsList == null) return;
        if (openDialogsList.contains(dialog))
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
        openDialogsList.remove(dialog);
    }

    public void dismissAllDialogs() {
        try {
            if (openDialogsList == null) return;
            for (Dialog dialog : openDialogsList) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }
            openDialogsList.clear();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

    }


}
