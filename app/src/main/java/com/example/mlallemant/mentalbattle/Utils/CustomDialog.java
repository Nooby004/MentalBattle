package com.example.mlallemant.mentalbattle.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mlallemant.mentalbattle.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by m.lallemant on 10/11/2017.
 */

public class CustomDialog {

    private OnClickBtnListener onClickBtnListener;

    public interface OnClickBtnListener {
        void onClickBtn1();

        void onClickBtn2();
    }

    private final Context context;
    private String idUser;
    private final String message;
    private final String btnText1;
    private final int btnColor1;
    private final String btnText2;
    private final int btnColor2;
    private Bitmap bm = null;

    private Dialog dialog;
    private final DatabaseManager db;
    private final FirebaseStorage storage;


    public CustomDialog(final Context context, final String idUser, final String message, final String btnText1, final int btnColor1, final String btnText2, final int btnColor2) {

        this.context = context;
        this.idUser = idUser;
        this.message = message;
        this.btnText1 = btnText1;
        this.btnColor1 = btnColor1;
        this.btnText2 = btnText2;
        this.btnColor2 = btnColor2;

        storage = FirebaseStorage.getInstance();
        db = DatabaseManager.getInstance();

    }

    public CustomDialog(final Context context, final Bitmap bm, final String message, final String btnText1, final int btnColor1, final String btnText2, final int btnColor2) {

        this.context = context;
        this.bm = bm;
        this.message = message;
        this.btnText1 = btnText1;
        this.btnColor1 = btnColor1;
        this.btnText2 = btnText2;
        this.btnColor2 = btnColor2;

        storage = FirebaseStorage.getInstance();
        db = DatabaseManager.getInstance();

    }

    public void setOnClickBtnListener(final OnClickBtnListener listener) {
        this.onClickBtnListener = listener;
    }

    public void create() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.menu_dialog_template);
        dialog.setCancelable(false);

        final CircleImageView profile_user = (CircleImageView) dialog.findViewById(R.id.dialog_profile_user);
        final TextView message = (TextView) dialog.findViewById(R.id.dialog_message);
        final TextView btn1 = (TextView) dialog.findViewById(R.id.dialog_btn1);
        final TextView btn2 = (TextView) dialog.findViewById(R.id.dialog_btn2);


        final StorageReference storageRef = storage.getReference();
        final String text = "profilePictures/" + idUser + ".png";
        final StorageReference imagesRef = storageRef.child(text);

        message.setText(this.message);

        btn1.setText(btnText1);
        btn1.setTextColor(ContextCompat.getColor(context, btnColor1));

        if (btnText2 != null) {
            btn2.setText(btnText2);
            btn2.setTextColor(ContextCompat.getColor(context, btnColor2));
        } else {
            btn2.setVisibility(View.GONE);
        }


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                onClickBtnListener.onClickBtn1();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                onClickBtnListener.onClickBtn2();
            }
        });

        if (this.bm == null) {
            final long ONE_MEGABYTE = 1024 * 1024;
            imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(final byte[] bytes) {
                    final Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profile_user.setImageBitmap(bm);

                }
            });
        }

        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    public void dismiss() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}
