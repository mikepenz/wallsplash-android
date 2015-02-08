package com.mikepenz.unsplash.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.IDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.unsplash.R;


public class MainActivity extends ActionBarActivity {
    public enum Category {
        ALL(1000),
        FEATURED(1001),
        LOVED(1002),
        BUILDINGS(1),
        FOOD(2),
        NATURE(4),
        OBJECTS(8),
        PEOPLE(16),
        TECHNOLOGY(32);

        public int id;

        private Category(int id) {
            this.id = id;
        }
    }

    private OnFilterChangedListener onFilterChangedListener;

    public void setOnFilterChangedListener(OnFilterChangedListener onFilterChangedListener) {
        this.onFilterChangedListener = onFilterChangedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);


        Drawer.Result result = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.category_all).withIdentifier(Category.ALL.id),
                        new PrimaryDrawerItem().withName(R.string.category_featured).withIdentifier(Category.FEATURED.id),
                        new SectionDrawerItem().withName(R.string.category_section_categories),
                        new PrimaryDrawerItem().withName(R.string.category_buildings).withIdentifier(Category.BUILDINGS.id),
                        new PrimaryDrawerItem().withName(R.string.category_food).withIdentifier(Category.FOOD.id),
                        new PrimaryDrawerItem().withName(R.string.category_nature).withIdentifier(Category.NATURE.id),
                        new PrimaryDrawerItem().withName(R.string.category_objects).withIdentifier(Category.OBJECTS.id),
                        new PrimaryDrawerItem().withName(R.string.category_people).withIdentifier(Category.PEOPLE.id),
                        new PrimaryDrawerItem().withName(R.string.category_technology).withIdentifier(Category.TECHNOLOGY.id)
                )
                .withSelectedItem(1)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem drawerItem) {
                        if (onFilterChangedListener != null) {
                            onFilterChangedListener.onFilterChanged(drawerItem.getIdentifier());
                        }
                    }
                })
                .build();

        //disable scrollbar :D it's ugly
        result.getListView().setVerticalScrollBarEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_open_source).setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_github).color(Color.WHITE).actionBarSize());
        menu.findItem(R.id.action_shuffle).setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_random).color(Color.WHITE).actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open_source) {
            new Libs.Builder()
                    .withFields(R.string.class.getFields())
                    .withActivityTitle(getString(R.string.action_open_source))
                    .withActivityTheme(R.style.AboutTheme)
                    .withLibraries("rxJava", "rxAndroid")
                    .start(this);

            return true;
        }
        return false; //super.onOptionsItemSelected(item);
    }

    public interface OnFilterChangedListener {
        public void onFilterChanged(int filter);
    }
}
