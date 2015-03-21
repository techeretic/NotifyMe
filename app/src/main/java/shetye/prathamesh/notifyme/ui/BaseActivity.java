package shetye.prathamesh.notifyme.ui;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import shetye.prathamesh.notifyme.R;

public abstract class BaseActivity extends ActionBarActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int getContentView();
}
