package shetye.prathamesh.notifyme.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import shetye.prathamesh.notifyme.NotificationDetail;
import shetye.prathamesh.notifyme.NotifyMe;
import shetye.prathamesh.notifyme.R;
import shetye.prathamesh.notifyme.Utilities;
import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;

/**
 * Created by prathamesh on 3/19/15.
 */
public class MyNotifRecAdapter extends
    RecyclerView.Adapter<MyNotifRecAdapter.ViewHolder> {
    Context mContext;
    List<Notif> mNotifs;

    public MyNotifRecAdapter(Context context, List<Notif> objects) {
        mContext = context;
        mNotifs = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rec_notif_item, viewGroup,
            false);
        v.setSelected(false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (mNotifs.get(position).getNotification_title().isEmpty()) {
            viewHolder.notifTitle.setVisibility(View.GONE);
            viewHolder.notifTitle.setText(mNotifs.get(position).getNotification_title());
        } else {
            viewHolder.notifTitle.setVisibility(View.VISIBLE);
            viewHolder.notifTitle.setText(mNotifs.get(position).getNotification_title());
        }
        viewHolder.notifText.setText(mNotifs.get(position).getNotification_content());
        String statusText="";
        if (mNotifs.get(position).getNotification_when() <= System.currentTimeMillis()) {
            statusText = "Notified at ";
            viewHolder.lineView.setBackgroundColor(mContext.getResources().getColor(R.color.completed_notif));
        } else {
            statusText = "Will Notify at ";
            viewHolder.lineView.setBackgroundColor(mContext.getResources().getColor(R.color.pending_notif));
        }
        viewHolder.notifDate.setText(statusText +
                Utilities.getInstance().getDateFromMS(
                mNotifs.get(position).getNotification_when()
        ));
        viewHolder.item_controller.setMinimumHeight(viewHolder.item_content.getHeight());
        viewHolder.item_content.setTag(R.id.item_content,viewHolder.item_controller);
        viewHolder.item_controller.setTag(R.id.item_content,viewHolder.item_content);

        //viewHolder.archive_notif.setOnClickListener(new OnLayoutClickListener(mNotifs.get(position).get_id(), position, mContext));
        viewHolder.edit_notif.setOnClickListener(new OnLayoutClickListener(mNotifs.get(position).get_id(), position, mContext));
        viewHolder.re_notify_notify.setOnClickListener(new OnLayoutClickListener(mNotifs.get(position).get_id(), position, mContext));
        viewHolder.share_notif.setOnClickListener(new OnLayoutClickListener(mNotifs.get(position).get_id(), position, mContext));
    }

    @Override
    public int getItemCount() {
        return mNotifs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView notifTitle;
        public TextView notifText;
        public TextView notifDate;
        public View lineView;
        public LinearLayout item_content;
        public LinearLayout item_controller;
        //public LinearLayout archive_notif;
        public LinearLayout edit_notif;
        public LinearLayout re_notify_notify;
        public LinearLayout share_notif;

        public ViewHolder(View view) {
            super(view);
            notifTitle = (TextView) view.findViewById(R.id.notify_title_txt);
            notifText = (TextView) view.findViewById(R.id.notify_txt);
            notifDate = (TextView) view.findViewById(R.id.when_to_notify);
            item_content = (LinearLayout) view.findViewById(R.id.item_content);
            item_controller = (LinearLayout) view.findViewById(R.id.item_controllers);
            lineView = view.findViewById(R.id.line_view);
            //archive_notif = (LinearLayout) view.findViewById(R.id.archive_notif);
            edit_notif = (LinearLayout) view.findViewById(R.id.edit_notif);
            re_notify_notify = (LinearLayout) view.findViewById(R.id.re_notif);
            share_notif = (LinearLayout) view.findViewById(R.id.share_notif);
        }
    }

    private class OnLayoutClickListener implements View.OnClickListener {

        int mPosition;
        int mID;
        Context mContext;

        OnLayoutClickListener(int ID, int position, Context context) {
            mPosition = position;
            mContext = context;
            mID = ID;
        }

        @Override
        public void onClick(View v) {
            String message = "";
            switch(v.getId()) {
/*
                case R.id.archive_notif:
                    message = "Clicked Archive";
                    DatabaseHelper.getInstance(mContext).markComplete(mID);
                    break;
*/
                case R.id.edit_notif:
                    Utilities.getInstance().editNote(mContext, mID);
                    break;
                case R.id.re_notif:
                    Utilities.getInstance().reNotify(mContext, mID);
                    break;
                case R.id.share_notif:
                    Utilities.getInstance().shareNote(mContext, mID);
                    break;
            }

            if (!message.isEmpty())
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
