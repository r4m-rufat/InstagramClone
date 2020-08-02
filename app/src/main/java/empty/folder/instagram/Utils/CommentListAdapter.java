package empty.folder.instagram.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import empty.folder.instagram.Models.Comment;
import empty.folder.instagram.Models.UserAccountSettings;
import empty.folder.instagram.R;

import static android.view.View.GONE;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context mcontext;

    public CommentListAdapter(@NonNull Context context, int resource,
                              @NonNull List<Comment> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        mcontext = context;
        layoutResource = resource;
    }

    private static class ViewHolder{
        TextView comment, username, date, reply, likes;
        CircleImageView imageView;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null){
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.comment = convertView.findViewById(R.id.comment);
            viewHolder.username = convertView.findViewById(R.id.comment_username);
            viewHolder.date = convertView.findViewById(R.id.comment_time);
            viewHolder.reply = convertView.findViewById(R.id.comment_reply);
            viewHolder.likes = convertView.findViewById(R.id.comment_likes);
            viewHolder.imageView = convertView.findViewById(R.id.circleImageViewForComments);
            viewHolder.like = convertView.findViewById(R.id.comment_like_icon);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // set the comment
        viewHolder.comment.setText(getItem(position).getComment());

        // set the time difference for the comment
        final String dateTimeDifference = getDateTimeDifference(getItem(position));

        if (!dateTimeDifference.equals("0")){
            viewHolder.date.setText(dateTimeDifference + "d");
        }else{
            viewHolder.date.setText("today");
        }

        // set the username and profile image for comment adapter
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(mcontext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mcontext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){

                    viewHolder.username.setText(dataSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(dataSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), viewHolder.imageView);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{
            if (position == 0){
                viewHolder.like.setVisibility(GONE);
                viewHolder.likes.setVisibility(GONE);
                viewHolder.reply.setVisibility(GONE);
            }
        }catch (NullPointerException e){

        }
        return convertView;
    }

    /**
     * returns a string representing the number of days ago the post was shared
     * @return
     */
    private String getDateTimeDifference(Comment comment){
        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        Date time;
        final String photoTime = comment.getDate();
        try {
            time = simpleDateFormat.parse(photoTime);
            difference = String.valueOf(Math.round((today.getTime() - time.getTime()) / 100 / 60 / 60 / 24));
        }catch (ParseException e){
            difference = "0";
        }

        return difference;
    }
}
