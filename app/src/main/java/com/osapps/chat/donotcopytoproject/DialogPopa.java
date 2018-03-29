package com.osapps.chat.donotcopytoproject;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by shabat on 10/7/2017.
 */

//this class behaves as the abstract class of all of the dialogs on the application.
public class DialogPopa extends Dialog {

    protected DialogPopa(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public DialogPopa(@NonNull Context context) {
        super(context);
    }


    public void show(Dialog dialog) {
     //   CrashReporter.report(CrashReporterFinals.DIALOG_SHOW + dialog.getClass().getSimpleName());
     //   super.show();
       // DialogsManager.getInstance().addDialog(dialog);
    }

    public void dismiss(Dialog dialog){
     //   CrashReporter.report(CrashReporterFinals.DIALOG_DISMISS + dialog.getClass().getSimpleName());
        if(getWindow() != null)
            super.dismiss();
    //    DialogsManager.getInstance().removeDialog(dialog);
    }
}

