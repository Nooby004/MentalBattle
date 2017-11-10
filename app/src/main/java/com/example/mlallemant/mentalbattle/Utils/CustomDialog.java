package com.example.mlallemant.mentalbattle.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

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

    public interface OnClickBtnListener{
        void onClickBtn1();
        void onClickBtn2();
    }

    private Context context;
    private String idUser;
    private String message;
    private String btnText1;
    private int btnColor1;
    private String btnText2;
    private int btnColor2;

    private Dialog dialog;
    private DatabaseManager db;
    private FirebaseStorage storage;


    public CustomDialog(Context context, String idUser, String message, String btnText1, int btnColor1, String btnText2, int btnColor2){

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

    public void setOnClickBtnListener(OnClickBtnListener listener) {
        this.onClickBtnListener = listener;
    }

    public void create(){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.menu_dialog_template);
        dialog.setCancelable(false);

        final CircleImageView profile_user = (CircleImageView) dialog.findViewById(R.id.dialog_profile_user);
        TextView message = (TextView) dialog.findViewById(R.id.dialog_message);
        TextView btn1 = (TextView) dialog.findViewById(R.id.dialog_btn1);
        TextView btn2 = (TextView) dialog.findViewById(R.id.dialog_btn2);


        StorageReference storageRef = storage.getReference();
        String text = "profilePictures/" + idUser  + ".png";
        StorageReference imagesRef = storageRef.child(text);

        final long ONE_MEGABYTE = 1024 * 1024;
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profile_user.setImageBitmap(bm);
            }
        });


        message.setText(this.message);

        btn1.setText(btnText1);
        btn1.setTextColor(ContextCompat.getColor(context,btnColor1));

        if (btnText2 != null) {
            btn2.setText(btnText2);
            btn2.setTextColor(ContextCompat.getColor(context,btnColor2));
        } else {
            btn2.setVisibility(View.GONE);
        }


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBtnListener.onClickBtn1();
            }
        });

       btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickBtnListener.onClickBtn2();
            }
        });

        dialog.show();

    }

    public void dismiss(){
        dialog.dismiss();
    }

    public boolean isShowing(){
        return dialog.isShowing();
    }
}
