package shetye.prathamesh.notifyme;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;
import shetye.prathamesh.notifyme.ui.MyNotifRecAdapter;
import shetye.prathamesh.notifyme.ui.NotifAnimator;
import shetye.prathamesh.notifyme.ui.RecyclerItemClickListener;


public class SearchResults extends BaseActivity {

    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;
    private MyNotifRecAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(Utilities.SHARED_PREF_APP_DATA, MODE_PRIVATE);
        prefs.edit().putBoolean(Utilities.SHARED_PREF_SEARCH_KEY,true).commit();
        mContext = this;
        Intent intent = getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.searchResults);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            setTitle(getResources().getString(R.string.search_results) + " for '" + query + "'");
            // use the query to search your data somehow
            mNotifications = DatabaseHelper.getInstance(mContext).searchNotes(query);
            mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,
                    LinearLayoutManager.VERTICAL, false));


            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView,
                    new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemLongClick(View view, int position) {

                }

                @Override
                public void onItemClick(View view, int position) {
                    LinearLayout content = (LinearLayout) view.findViewById(R.id.item_content);
                    if (content.getVisibility()==View.INVISIBLE) {
                        return;
                    }
                    Notif note = mNotifications.get(position);
                    ActivityOptionsCompat options;
                    TextView titleV = (TextView) view.findViewById(R.id.notify_title_txt);
                    if (titleV.getVisibility()==View.GONE) {
                        options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                SearchResults.this,
                                Pair.create(view.findViewById(R.id.when_to_notify), "when_to_notify"),
                                Pair.create(view.findViewById(R.id.notify_txt), "notify_txt"),
                                Pair.create(view.findViewById(R.id.line_view), "line_view")
                        );
                    } else {
                        options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                SearchResults.this,
                                Pair.create(view.findViewById(R.id.when_to_notify), "when_to_notify"),
                                Pair.create(view.findViewById(R.id.notify_txt), "notify_txt"),
                                Pair.create(view.findViewById(R.id.notify_title_txt), "notify_title_txt"),
                                Pair.create(view.findViewById(R.id.line_view), "line_view")
                        );
                    }
                    Intent intent = new Intent(SearchResults.this, NotifyMe.class);
                    intent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, note.get_id());
                    ActivityCompat.startActivityForResult(SearchResults.this, intent,
                            Utilities.UPDATED, options.toBundle());
                }
            }));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utilities.UPDATED) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
            } else {
                setResult(RESULT_CANCELED);
            }
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_search_results;
    }

}
