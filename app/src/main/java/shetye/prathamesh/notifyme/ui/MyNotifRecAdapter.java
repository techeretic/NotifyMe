package shetye.prathamesh.notifyme.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import shetye.prathamesh.notifyme.R;
import shetye.prathamesh.notifyme.Utilities;
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
        viewHolder.notifTitle.setText(mNotifs.get(position).getNotification_title());
        viewHolder.notifText.setText(mNotifs.get(position).getNotification_content());
        viewHolder.notifDate.setText(Utilities.getInstance().getDateFromMS(
            mNotifs.get(position).getNotification_when()
        ));
        if (mNotifs.get(position).getNotification_when() <= System.currentTimeMillis()) {
            viewHolder.notifStatus.setText("Completed");
            viewHolder.notifStatus.setBackgroundColor(mContext.getResources().getColor(R.color.completed_notif));
        } else {
            viewHolder.notifStatus.setText("Pending");
            viewHolder.notifStatus.setBackgroundColor(mContext.getResources().getColor(R.color.pending_notif));
        }
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
        public TextView notifStatus;

        public ViewHolder(View view) {
            super(view);
            notifTitle = (TextView) view.findViewById(R.id.notify_title_txt);
            notifText = (TextView) view.findViewById(R.id.notify_txt);
            notifDate = (TextView) view.findViewById(R.id.when_to_notify);
            notifStatus = (TextView) view.findViewById(R.id.notif_status);
        }
    }
}
