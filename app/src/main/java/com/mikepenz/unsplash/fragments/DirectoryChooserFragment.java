package com.mikepenz.unsplash.fragments;
/**
 * https://github.com/passy/Android-DirectoryChooser
 *
 Copyright 2013-2014 Pascal Hartig

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gu.option.Option;
import com.gu.option.UnitFunction;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.unsplash.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Activities that contain this fragment must implement the
 * {@link DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DirectoryChooserFragment extends DialogFragment {
    public static final String KEY_CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
    private static final String ARG_NEW_DIRECTORY_NAME = "NEW_DIRECTORY_NAME";
    private static final String ARG_INITIAL_DIRECTORY = "INITIAL_DIRECTORY";
    private static final String TAG = DirectoryChooserFragment.class.getSimpleName();
    private String mNewDirectoryName;
    private String mInitialDirectory;

    private Option<OnFragmentInteractionListener> mListener = Option.none();

    private Button mBtnConfirm;
    private Button mBtnCancel;
    private ImageButton mBtnNavUp;
    private ImageButton mBtnCreateFolder;
    private TextView mTxtvSelectedFolder;
    private ListView mListDirectories;

    private ArrayAdapter<String> mListDirectoriesAdapter;
    private ArrayList<String> mFilenames;
    /**
     * The directory that is currently being shown.
     */
    private File mSelectedDir;
    private File[] mFilesInDir;
    private FileObserver mFileObserver;


    public DirectoryChooserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param newDirectoryName Name of the directory to create.
     * @param initialDirectory Optional argument to define the path of the directory
     *                         that will be shown first.
     *                         If it is not sent or if path denotes a non readable/writable directory
     *                         or it is not a directory, it defaults to
     *                         {@link android.os.Environment#getExternalStorageDirectory()}
     * @return A new instance of fragment DirectoryChooserFragment.
     */
    public static DirectoryChooserFragment newInstance(
            @NonNull final String newDirectoryName,
            @Nullable final String initialDirectory) {
        DirectoryChooserFragment fragment = new DirectoryChooserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NEW_DIRECTORY_NAME, newDirectoryName);
        args.putString(ARG_INITIAL_DIRECTORY, initialDirectory);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSelectedDir != null) {
            outState.putString(KEY_CURRENT_DIRECTORY, mSelectedDir.getAbsolutePath());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException(
                    "You must create DirectoryChooserFragment via newInstance().");
        } else {
            mNewDirectoryName = getArguments().getString(ARG_NEW_DIRECTORY_NAME);
            mInitialDirectory = getArguments().getString(ARG_INITIAL_DIRECTORY);
        }

        if (savedInstanceState != null) {
            mInitialDirectory = savedInstanceState.getString(KEY_CURRENT_DIRECTORY);
        }

        if (this.getShowsDialog()) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        } else {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        assert getActivity() != null;
        final View view = inflater.inflate(R.layout.fragment_directory_chooser, container, false);

        mBtnConfirm = (Button) view.findViewById(R.id.btnConfirm);
        mBtnCancel = (Button) view.findViewById(R.id.btnCancel);
        mBtnNavUp = (ImageButton) view.findViewById(R.id.btnNavUp);
        mBtnCreateFolder = (ImageButton) view.findViewById(R.id.btnCreateFolder);
        mTxtvSelectedFolder = (TextView) view.findViewById(R.id.txtvSelectedFolder);
        mListDirectories = (ListView) view.findViewById(R.id.directoryList);

        mBtnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isValidFile(mSelectedDir)) {
                    returnSelectedFolder();
                }
            }
        });

        mBtnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                    @Override
                    public void apply(final OnFragmentInteractionListener f) {
                        f.onCancelChooser();
                    }
                });
            }
        });

        mListDirectories.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                debug("Selected index: %d", position);
                if (mFilesInDir != null && position >= 0
                        && position < mFilesInDir.length) {
                    changeDirectory(mFilesInDir[position]);
                }
            }
        });

        mBtnNavUp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                File parent;
                if (mSelectedDir != null
                        && (parent = mSelectedDir.getParentFile()) != null) {
                    changeDirectory(parent);
                }
            }
        });

        mBtnCreateFolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewFolderDialog();
            }
        });

        if (!getShowsDialog()) {
            mBtnCreateFolder.setVisibility(View.GONE);
        }

        adjustResourceLightness();

        mFilenames = new ArrayList<>();
        mListDirectoriesAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mFilenames);
        mListDirectories.setAdapter(mListDirectoriesAdapter);

        final File initialDir;
        if (mInitialDirectory != null && isValidFile(new File(mInitialDirectory))) {
            initialDir = new File(mInitialDirectory);
        } else {
            initialDir = Environment.getExternalStorageDirectory();
        }

        changeDirectory(initialDir);

        return view;
    }

    private void adjustResourceLightness() {
        // change up button to light version if using dark theme
        int color = 0xFFFFFF;
        final Resources.Theme theme = getActivity().getTheme();

        if (theme != null) {
            TypedArray backgroundAttributes = theme.obtainStyledAttributes(
                    new int[]{android.R.attr.colorBackground});

            if (backgroundAttributes != null) {
                color = backgroundAttributes.getColor(0, 0xFFFFFF);
                backgroundAttributes.recycle();
            }
        }

        // convert to greyscale and check if < 128
        if (color != 0xFFFFFF && 0.21 * Color.red(color) +
                0.72 * Color.green(color) +
                0.07 * Color.blue(color) < 128) {


            mBtnNavUp.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_expand_less).sizeDp(16).color(Color.WHITE));
            mBtnCreateFolder.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_add).sizeDp(16).color(Color.WHITE));
        } else {
            mBtnNavUp.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_expand_less).sizeDp(16).color(Color.BLACK));
            mBtnCreateFolder.setImageDrawable(new IconicsDrawable(getActivity(), GoogleMaterial.Icon.gmd_add).sizeDp(16).color(Color.BLACK));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = Option.some((OnFragmentInteractionListener) activity);
        } catch (ClassCastException ignore) {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFileObserver != null) {
            mFileObserver.startWatching();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /*
        inflater.inflate(R.menu.directory_chooser, menu);

        final MenuItem menuItem = menu.findItem(R.id.new_folder_item);

        if (menuItem == null) {
            return;
        }

        menuItem.setVisible(isValidFile(mSelectedDir) && mNewDirectoryName != null);
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        final int itemId = item.getItemId();

        if (itemId == R.id.new_folder_item) {
            openNewFolderDialog();
            return true;
        }

        */
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a confirmation dialog that asks the user if he wants to create a
     * new folder.
     */
    private void openNewFolderDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_folder_label)
                .setMessage(
                        String.format(getString(R.string.create_folder_msg),
                                mNewDirectoryName))
                .setNegativeButton(R.string.cancel_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.confirm_label,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                int msg = createFolder();
                                Toast t = Toast.makeText(
                                        getActivity(), msg,
                                        Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }).create().show();
    }

    private void debug(String message, Object... args) {
        Log.d(TAG, String.format(message, args));
    }

    /**
     * Change the directory that is currently being displayed.
     *
     * @param dir The file the activity should switch to. This File must be
     *            non-null and a directory, otherwise the displayed directory
     *            will not be changed
     */
    private void changeDirectory(File dir) {
        if (dir == null) {
            debug("Could not change folder: dir was null");
        } else if (!dir.isDirectory()) {
            debug("Could not change folder: dir is no directory");
        } else {
            File[] contents = dir.listFiles();
            if (contents != null) {
                int numDirectories = 0;
                for (File f : contents) {
                    if (f.isDirectory()) {
                        numDirectories++;
                    }
                }
                mFilesInDir = new File[numDirectories];
                mFilenames.clear();
                for (int i = 0, counter = 0; i < numDirectories; counter++) {
                    if (contents[counter].isDirectory()) {
                        mFilesInDir[i] = contents[counter];
                        mFilenames.add(contents[counter].getName());
                        i++;
                    }
                }
                Arrays.sort(mFilesInDir);
                Collections.sort(mFilenames);
                mSelectedDir = dir;
                mTxtvSelectedFolder.setText(dir.getAbsolutePath());
                mListDirectoriesAdapter.notifyDataSetChanged();
                mFileObserver = createFileObserver(dir.getAbsolutePath());
                mFileObserver.startWatching();
                debug("Changed directory to %s", dir.getAbsolutePath());
            } else {
                debug("Could not change folder: contents of dir were null");
            }
        }
        refreshButtonState();
    }

    /**
     * Changes the state of the buttons depending on the currently selected file
     * or folder.
     */
    private void refreshButtonState() {
        final Activity activity = getActivity();
        if (activity != null && mSelectedDir != null) {
            mBtnConfirm.setEnabled(isValidFile(mSelectedDir));
            getActivity().invalidateOptionsMenu();
        }
    }

    /**
     * Refresh the contents of the directory that is currently shown.
     */
    private void refreshDirectory() {
        if (mSelectedDir != null) {
            changeDirectory(mSelectedDir);
        }
    }

    /**
     * Sets up a FileObserver to watch the current directory.
     */
    private FileObserver createFileObserver(String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(int event, String path) {
                debug("FileObserver received event %d", event);
                final Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshDirectory();
                        }
                    });
                }
            }
        };
    }

    /**
     * Returns the selected folder as a result to the activity the fragment's attached to. The
     * selected folder can also be null.
     */
    private void returnSelectedFolder() {
        if (mSelectedDir != null) {
            debug("Returning %s as result", mSelectedDir.getAbsolutePath());
            mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                @Override
                public void apply(final OnFragmentInteractionListener f) {
                    f.onSelectDirectory(mSelectedDir.getAbsolutePath());
                }
            });
        } else {
            mListener.foreach(new UnitFunction<OnFragmentInteractionListener>() {
                @Override
                public void apply(final OnFragmentInteractionListener f) {
                    f.onCancelChooser();
                }
            });
        }

    }

    /**
     * Creates a new folder in the current directory with the name
     * CREATE_DIRECTORY_NAME.
     */
    private int createFolder() {
        if (mNewDirectoryName != null && mSelectedDir != null
                && mSelectedDir.canWrite()) {
            File newDir = new File(mSelectedDir, mNewDirectoryName);
            if (!newDir.exists()) {
                boolean result = newDir.mkdir();
                if (result) {
                    changeDirectory(newDir);
                    return R.string.create_folder_success;
                } else {
                    return R.string.create_folder_error;
                }
            } else {
                return R.string.create_folder_error_already_exists;
            }
        } else if (mSelectedDir != null && !mSelectedDir.canWrite()) {
            return R.string.create_folder_error_no_write_access;
        } else {
            return R.string.create_folder_error;
        }
    }

    /**
     * Returns true if the selected file or directory would be valid selection.
     */
    private boolean isValidFile(File file) {
        return (file != null && file.isDirectory() && file.canRead() && file
                .canWrite());
    }

    @Nullable
    public OnFragmentInteractionListener getDirectoryChooserListener() {
        return mListener.get();
    }

    public void setDirectoryChooserListener(@Nullable OnFragmentInteractionListener listener) {
        mListener = Option.option(listener);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * Triggered when the user successfully selected their destination directory.
         */
        public void onSelectDirectory(@NonNull String path);

        /**
         * Advices the activity to remove the current fragment.
         */
        public void onCancelChooser();
    }

}