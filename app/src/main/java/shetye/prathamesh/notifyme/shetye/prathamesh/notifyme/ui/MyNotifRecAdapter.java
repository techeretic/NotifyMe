package shetye.prathamesh.notifyme.shetye.prathamesh.notifyme.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by prathamesh on 3/19/15.
 */
public class MyNotifRecAdapter {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView noteTitle;

        public TextView noteText;

        public TextView noteDate;

        public ViewHolder(View view) {
            super(view);
            /*noteTitle = (TextView) view.findViewById(R.id.textView0);
            noteText = (TextView) view.findViewById(R.id.textView1);
            noteDate = (TextView) view.findViewById(R.id.textView2);*/
        }
    }
}
