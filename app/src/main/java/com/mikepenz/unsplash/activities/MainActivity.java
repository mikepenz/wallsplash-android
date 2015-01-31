package com.mikepenz.unsplash.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.fragments.ImagesFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;


public class MainActivity extends MaterialNavigationDrawer {


    @Override
    public void init(Bundle bundle) {
        // create sections
        ImagesFragment featured = new ImagesFragment();
        Bundle b = new Bundle();
        b.putInt("type", ImagesFragment.FEATURED);
        featured.setArguments(b);
        this.addSection(newSection("Featured", featured));

        // create sections
        ImagesFragment all = new ImagesFragment();
        b = new Bundle();
        b.putInt("type", ImagesFragment.ALL);
        all.setArguments(b);
        this.addSection(newSection("All", all));

        // aboutLibraries
        Intent aboutLibraries = new Libs.Builder()
                .withFields(R.string.class.getFields())
                .withActivityTitle(getString(R.string.action_open_source))
                .withActivityTheme(R.style.AboutTheme)
                .withLibraries("rxJava", "rxAndroid")
                .intent(this);
        this.addBottomSection(newSection("Open Source", new IconicsDrawable(this, FontAwesome.Icon.faw_github), aboutLibraries));
    }

    @Override
    public void onClick(MaterialSection section) {
        super.onClick(section);
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_shuffle).setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_random).color(Color.WHITE).actionBarSize());

        return true;
    }
}
