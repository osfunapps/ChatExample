package com.osapps.chat.login.usernamedialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.osapps.chat.R;
import com.osapps.chat.donotcopytoproject.DialogPopa;


public class SelectNameDialog extends DialogPopa {

    //views
    private EditText usernameFieldET;
    private Button submitBtn;

    //etc
    private static final String SELECT_NAME_DIALOG = "ozvi";
    private final Context context;
    private SelectNameDialogCallback callback;
    private ChatNameValidator chatNameValidator;

    public SelectNameDialog(Context context) {
        super(context, R.style.FullHeightDialog);
        setContentView(R.layout.dialog_chat_select_name);
        this.context = context;
        prepareDialog();
        setViews();
    }


    private void prepareDialog() {

        //make transparent!
       /* if(getWindow()!=null)
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
*/
        if(getWindow()!=null)
            getWindow().setDimAmount(0.5f);
        setCanceledOnTouchOutside(true);
        setCancelable(false);
    }


    private void setViews() {
        usernameFieldET = findViewById(R.id.chat_username_name);
        submitBtn = findViewById(R.id.chat_username_submit);
        submitBtn.setOnClickListener(onSubmitClicked);
    }

    private View.OnClickListener onSubmitClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String username = usernameFieldET.getText().toString();
            if(chatNameValidator.isNameValidated(username)){
                Toast.makeText(context, context.getString(R.string.chat_name_approved), Toast.LENGTH_SHORT).show();
                callback.onNameApproved(username);
            } else {
                Toast.makeText(context, context.getString(R.string.chat_name_not_approved), Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public void dismiss() {
        //kill all views here!
        usernameFieldET = null;
        submitBtn = null;
        super.dismiss(this);
    }


    public interface SelectNameDialogCallback{
        void onNameApproved(String username);
    }
}