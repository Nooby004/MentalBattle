package com.example.mlallemant.mentalbattle.UI.Friends;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 30/10/2017.
 */

public class FriendAdapter extends ArrayAdapter<FriendModel> implements View.OnClickListener {

    private ArrayList<FriendModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView username;
        TextView level;
        TextView rank;
        CircleImageView profile_picture;
        CircleImageView connect;
    }

    public FriendAdapter(ArrayList<FriendModel> data, Context context){
        super(context, R.layout.friend_row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v){
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        FriendModel friendModel = (FriendModel) object;

        //makeToast("click on " + friendModel.player.getName());
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Get the data item for this position
        FriendModel friendModel = getItem(position);

        //Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder;

        final View result;

        if (convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.friend_row_item_template, parent, false);

            viewHolder.username = (TextView) convertView.findViewById(R.id.row_item_tv_username);
            viewHolder.connect = (CircleImageView) convertView.findViewById(R.id.row_item_iv_connect);
            viewHolder.rank = (TextView) convertView.findViewById(R.id.row_item_rank);
            viewHolder.level = (TextView) convertView.findViewById(R.id.row_item_tv_level);
            viewHolder.profile_picture = (CircleImageView) convertView.findViewById(R.id.row_item_profile_picture);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        if (friendModel != null){

            //USERNAME
            if (friendModel.getPlayer() != null){
                String splitName = friendModel.getPlayer().getName().split(" ")[0];
                viewHolder.username.setText(splitName);
            }

            //CONNECTED/DISCONNECTED
            if (friendModel.getFriendAcq() != null) {
                if (friendModel.getFriendAcq().equals(Utils.ACK_OK)) {
                    if (friendModel.getConnected()) {
                        viewHolder.connect.setImageResource(R.color.greenColor);
                    } else {
                        viewHolder.connect.setImageResource(R.color.redColor);
                        //gd.setColor(ContextCompat.getColor(mContext, R.color.redColor));
                    }
                } else {
                    viewHolder.connect.setImageResource(R.color.whiteGrayColor);
                }
            }

            //LEVEL / RANK
            if (friendModel.getXp()!=null){
                int level = getLevelByXp(Integer.parseInt(friendModel.getXp()));
                String text = "LVL " + level;
                viewHolder.level.setText(text);
                viewHolder.rank.setText(getRankByLevel(level));
            }


            //PROFILE PICTURE
            if (friendModel.getProfilePicture() == null) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                String text = "profilePictures/" + friendModel.getPlayer().getId()  + ".png";
                StorageReference imagesRef = storageRef.child(text);

                final long ONE_MEGABYTE = 1024 * 1024;
                imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        viewHolder.profile_picture.setImageBitmap(bm);
                    }
                });

            }
        }
        return convertView;
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }


    private String getRankByLevel(int level){

        String rank = "Unknown";
        if (level > 0 && level <= 5) rank = "Brainless";
        if (level > 5 && level <= 10) rank = "Little Head";
        if (level > 10 && level <= 20) rank = "Genius";
        if (level > 20 && level <= 35) rank = "Brain Master";
        if (level > 35 && level <= 60) rank = "Super Calculator";
        if (level > 60 && level <= 100) rank = "God";
        if (level > 100) rank = "Chuck Norris";
        return rank;
    }

    private int getLevelByXp(int XP){
        int level;
        level = (int) Math.round ((Math.sqrt(100 * (2 * XP + 25) + 50) / 100));
        return level;
    }

}
