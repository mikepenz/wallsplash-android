package com.mikepenz.unsplash.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.unsplash.R;
import com.mikepenz.unsplash.models.ImageList;
import com.mikepenz.unsplash.network.UnsplashApi;


public class MainActivity extends AppCompatActivity {
    public enum Category {
        ALL(1000),
        FEATURED(1001),
        LOVED(1002),
        BUILDINGS(1),
        FOOD(2),
        NATURE(4),
        PEOPLE(8),
        TECHNOLOGY(16),
        OBJECTS(32);

        public final int id;

        private Category(int id) {
            this.id = id;
        }
    }

    public Drawer result;

    private OnFilterChangedListener onFilterChangedListener;

    public void setOnFilterChangedListener(OnFilterChangedListener onFilterChangedListener) {
        this.onFilterChangedListener = onFilterChangedListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.category_all).withIdentifier(Category.ALL.id).withIcon(GoogleMaterial.Icon.gmd_landscape),
                        new PrimaryDrawerItem().withName(R.string.category_featured).withIdentifier(Category.FEATURED.id).withIcon(GoogleMaterial.Icon.gmd_grade),
                        new SectionDrawerItem().withName(R.string.category_section_categories),
                        new PrimaryDrawerItem().withName(R.string.category_buildings).withIdentifier(Category.BUILDINGS.id).withIcon(GoogleMaterial.Icon.gmd_location_city),
                        new PrimaryDrawerItem().withName(R.string.category_food).withIdentifier(Category.FOOD.id).withIcon(GoogleMaterial.Icon.gmd_local_bar),
                        new PrimaryDrawerItem().withName(R.string.category_nature).withIdentifier(Category.NATURE.id).withIcon(GoogleMaterial.Icon.gmd_local_florist),
                        new PrimaryDrawerItem().withName(R.string.category_objects).withIdentifier(Category.OBJECTS.id).withIcon(GoogleMaterial.Icon.gmd_style),
                        new PrimaryDrawerItem().withName(R.string.category_people).withIdentifier(Category.PEOPLE.id).withIcon(GoogleMaterial.Icon.gmd_person),
                        new PrimaryDrawerItem().withName(R.string.category_technology).withIdentifier(Category.TECHNOLOGY.id).withIcon(GoogleMaterial.Icon.gmd_local_see)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            if (drawerItem instanceof Nameable) {
                                toolbar.setTitle(((Nameable) drawerItem).getName().getText(MainActivity.this));
                            }
                            if (onFilterChangedListener != null) {
                                onFilterChangedListener.onFilterChanged(drawerItem.getIdentifier());
                            }
                        }

                        return false;
                    }
                })
                .build();

        //disable scrollbar :D it's ugly
        result.getRecyclerView().setVerticalScrollBarEnabled(false);
    }

    /**
     * @param images
     */
    public void setCategoryCount(ImageList images) {
        if (result.getDrawerItems() != null && result.getDrawerItems().size() == 9 && images != null && images.getData() != null) {
            result.updateBadge(Category.ALL.id, new StringHolder(images.getData().size() + ""));
            result.updateBadge(Category.FEATURED.id, new StringHolder(UnsplashApi.countFeatured(images.getData()) + ""));

            result.updateBadge(Category.BUILDINGS.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.BUILDINGS.id) + ""));
            result.updateBadge(Category.FOOD.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.FOOD.id) + ""));
            result.updateBadge(Category.NATURE.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.NATURE.id) + ""));
            result.updateBadge(Category.OBJECTS.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.OBJECTS.id) + ""));
            result.updateBadge(Category.PEOPLE.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.PEOPLE.id) + ""));
            result.updateBadge(Category.TECHNOLOGY.id, new StringHolder(UnsplashApi.countCategory(images.getData(), Category.TECHNOLOGY.id) + ""));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_open_source).setIcon(new IconicsDrawable(this, FontAwesome.Icon.faw_github).color(Color.WHITE).actionBar());
        menu.findItem(R.id.action_shuffle).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_shuffle).paddingDp(1).color(Color.WHITE).actionBar());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open_source) {
            new LibsBuilder()
                    .withFields(R.string.class.getFields())
                    .withActivityTitle(getString(R.string.action_open_source))
                    .withActivityTheme(R.style.MaterialDrawerTheme)
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
