package crazysheep.io.filemanager;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.adapter.FilesAdapter;
import crazysheep.io.filemanager.animator.FabAnimatorHelper;
import crazysheep.io.filemanager.animator.FabMenuAnimatorHelper;
import crazysheep.io.filemanager.asynctask.FileScannerTask;
import crazysheep.io.filemanager.model.FileItemModel;
import crazysheep.io.filemanager.model.ScanDirBean;
import crazysheep.io.filemanager.prefs.SettingsPrefs;
import crazysheep.io.filemanager.utils.DateUtils;
import crazysheep.io.filemanager.utils.DialogUtils;
import crazysheep.io.filemanager.utils.FileUtils;
import crazysheep.io.filemanager.utils.L;
import crazysheep.io.filemanager.utils.ViewUtils;
import io.codetail.animation.arcanimator.Side;
import io.codetail.widget.RevealFrameLayout;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.file_rv) RecyclerView mFileRv;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.fab_sheet_cv) CardView mFabSheetCv;
    @Bind(R.id.fab_sheet_rfl) RevealFrameLayout mFabRevealFl;
    @Bind(R.id.action_copy_iv) ImageView mFileCopyIv;
    @Bind(R.id.action_cut_iv) ImageView mFileCutIv;
    @Bind(R.id.action_delete_iv) ImageView mFileDeleteIv;
    @Bind(R.id.action_info_iv) ImageView mFileInfoIv;
    @Bind(R.id.fab_menu_rll) RevealFrameLayout mFabMenuRevealLl;
    @Bind(R.id.fab_menu_fl) View mFabMenuParentFl;
    @Bind(R.id.create_folder_fab) FloatingActionButton mCreateFolderFab;
    @Bind(R.id.edit_mode_fab) FloatingActionButton mEditModeFab;

    private FabMenuAnimatorHelper.Builder mMenuAnimatorBuilder;

    private LinearLayoutManager mLayoutMgr;
    private FilesAdapter mFileAdapter;
    private SettingsPrefs mSettingsPrefs;

    private File mCurrentDir;
    private LinkedList<ScanDirBean> mFileStack = new LinkedList<>();

    private static final int STATE_CLOSED = 0;
    private static final int STATE_ANIMATING = 1;
    private static final int STATE_EXPANDED = 2;
    private int mFabState = STATE_CLOSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSettingsPrefs = new SettingsPrefs(this);

        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initUI();
    }

    private void initUI() {
        mMenuAnimatorBuilder = FabMenuAnimatorHelper.wrap(mFabMenuRevealLl, mFabMenuParentFl, mFab)
                .addFab(mCreateFolderFab, R.id.create_folder_fab)
                .addFab(mEditModeFab, R.id.edit_mode_fab)
                .setOnFabMenuItemClickListener(new FabMenuAnimatorHelper.OnFabItemClickListener() {
                    @Override
                    public void onFabItemClick(FloatingActionButton fab, int id) {
                        switch (id) {
                            case R.id.edit_mode_fab: {
                                animateFab();
                            }break;

                            case R.id.create_folder_fab: {
                                showCreateFolderDialog();
                            }break;
                        }
                    }
                });
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mMenuAnimatorBuilder.isAnimating() && !mMenuAnimatorBuilder.isExpanded())
                    mMenuAnimatorBuilder.expanded();
            }
        });

        mFileCopyIv.setOnClickListener(this);
        mFileCutIv.setOnClickListener(this);
        mFileDeleteIv.setOnClickListener(this);
        mFileInfoIv.setOnClickListener(this);

        mLayoutMgr = new LinearLayoutManager(this);
        mFileRv.setLayoutManager(mLayoutMgr);
        mFileAdapter = new FilesAdapter(this, null, mSettingsPrefs.getShowHiddenFiles()
                ? FilesAdapter.MODE_SHOW_HIDDEN_FILES : FilesAdapter.MODE_NOT_SHOW_HIDDEN_FILES);
        mFileRv.setAdapter(mFileAdapter);
        mFileAdapter.setOnItemClickListener(new FilesAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View view) {
                if (mFileAdapter.isEditingMode()) {
                    mFileAdapter.toggleItemChoose(position);
                } else {
                    // click item file
                    FileItemModel itemModel = mFileAdapter.getItem(position);
                    File file = new File(itemModel.filepath);
                    if (file.isDirectory()) {
                        // save last directory first visible position and offset
                        int firstVisibleItemPosition = mLayoutMgr.findFirstVisibleItemPosition();
                        ScanDirBean dirBean = new ScanDirBean(mCurrentDir, firstVisibleItemPosition,
                                mLayoutMgr.findViewByPosition(firstVisibleItemPosition).getTop());
                        mFileStack.push(dirBean);

                        doScanDir(new ScanDirBean(file, 0, 0));
                    } else {
                        Intent openIntent = new Intent(Intent.ACTION_VIEW);
                        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        openIntent.setDataAndType(Uri.fromFile(file), FileUtils.getMimeType(file));
                        try {
                            startActivity(openIntent);
                        } catch (ActivityNotFoundException anfe) {
                            anfe.printStackTrace();

                            Snackbar.make(mFileRv, R.string.msg_can_not_open_file,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        mFileRv.setItemAnimator(new DefaultItemAnimator());

        // request permission: READ_EXTERNAL_STORAGE
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        doScanDir(new ScanDirBean(Environment.getExternalStorageDirectory(), 0, 0));
                    }

                    @Override
                    public void onDenied(String permission) {
                        Snackbar.make(mFileRv, getString(R.string.msg_permission_denied),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        // TODO
        switch (v.getId()) {
            case R.id.action_copy_iv: {
                if(hasChosenFiles())
                    Snackbar.make(mFileRv,
                            "copy " + mFileAdapter.getChosenItems().size() + " item",
                            Snackbar.LENGTH_SHORT).show();
            }break;

            case R.id.action_cut_iv: {
                if(hasChosenFiles())
                    Snackbar.make(mFileRv,
                            "cut " + mFileAdapter.getChosenItems().size() + " item",
                            Snackbar.LENGTH_SHORT).show();
            }break;

            case R.id.action_delete_iv: {
                if(hasChosenFiles()) {
                    final List<FileItemModel> chooseFiles = mFileAdapter.getChosenItems();
                    String dialogTitle = chooseFiles.size() == 1
                            ? getString(R.string.tv_delete_sing_file, chooseFiles.get(0).filename)
                                    : getString(R.string.tv_delete_files, chooseFiles.size());
                    DialogUtils.showConfirmDialog(this, dialogTitle, null,
                            new DialogUtils.ButtonAction() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.ok);
                                }

                                @Override
                                public void onClick(DialogInterface dialog) {
                                    DialogUtils.dismissDialog((Dialog)dialog);

                                    for(FileItemModel item : chooseFiles) {
                                        FileUtils.deleteFile(item.filepath);
                                    }

                                    // refresh UI
                                    mFileAdapter.removeItems(chooseFiles);
                                }
                            },
                            new DialogUtils.ButtonAction() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.cancel);
                                }

                                @Override
                                public void onClick(DialogInterface dialog) {
                                    DialogUtils.dismissDialog((Dialog) dialog);
                                }
                            });
                }
            }break;

            case R.id.action_info_iv: {
                if(hasChosenFiles())
                    Snackbar.make(mFileRv,
                            "info " + mFileAdapter.getChosenItems().size() + " item",
                            Snackbar.LENGTH_SHORT).show();
            }break;
        }
    }

    private boolean hasChosenFiles() {
        if(mFileAdapter.getChosenItems().size() == 0) {
            Snackbar.make(mFileRv, R.string.msg_have_not_choose_file,
                    Snackbar.LENGTH_LONG).show();

            return false;
        }

        return true;
    }

    private void animateFab() {
        mFabState = STATE_ANIMATING;

        FabAnimatorHelper.wrap(mFab, mFabSheetCv)
                .arc(ViewUtils.getRelativeLeft(mFabSheetCv) + mFabSheetCv.getWidth() / 2,
                        ViewUtils.getRelativeTop(mFabSheetCv) + mFabSheetCv.getHeight() / 2,
                        270f, Side.RIGHT)
                .setArcDuration(150)
                .setArcInterpolator(new AccelerateInterpolator())
                .listenArc(new FabAnimatorHelper.DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mFabRevealFl.setVisibility(View.VISIBLE);
                        mFab.setVisibility(View.GONE);
                    }
                })
                .reveal(mFabSheetCv.getLeft() + mFabSheetCv.getWidth() / 2,
                        mFabSheetCv.getTop() + mFabSheetCv.getHeight() / 2,
                        (float) Math.hypot(mFab.getWidth(), mFab.getHeight()),
                        (float) Math.hypot(mFabSheetCv.getWidth(), mFabSheetCv.getHeight()))
                .setRevealDuration(200)
                .setRevealInterpolator(new AccelerateDecelerateInterpolator())
                .listenReveal(new FabAnimatorHelper.DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mFabState = STATE_EXPANDED;

                        toggleEditMode(true);
                    }
                })
                .arcComboReveal()
                .animate();
    }

    private void reverseAnimateFab() {
        mFabState = STATE_ANIMATING;

        FabAnimatorHelper.wrap(mFab, mFabSheetCv)
                .reveal(mFabSheetCv.getLeft() + mFabSheetCv.getWidth() / 2,
                        mFabSheetCv.getTop() + mFabSheetCv.getHeight() / 2,
                        (float) Math.hypot(mFabSheetCv.getWidth(), mFabSheetCv.getHeight()),
                        (float) Math.hypot(mFab.getWidth(), mFab.getHeight()))
                .setRevealDuration(200)
                .setRevealInterpolator(new AccelerateInterpolator())
                .listenReveal(new FabAnimatorHelper.DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mFabRevealFl.setVisibility(View.GONE);
                        mFab.setVisibility(View.VISIBLE);
                    }
                })
                .arc(ViewUtils.getRelativeLeft(mFab) + mFab.getWidth() / 2,
                        ViewUtils.getRelativeTop(mFab) + mFab.getHeight() / 2,
                        270, Side.RIGHT)
                .setArcDuration(200)
                .setArcInterpolator(new AccelerateDecelerateInterpolator())
                .listenArc(new FabAnimatorHelper.DefaultAnimatorListener() {
                    @Override
                    public void onAnimationEnd() {
                        mFabState = STATE_CLOSED;

                        toggleEditMode(false);
                    }
                })
                .revealComboArc()
                .animate();
    }

    private void toggleEditMode(boolean goEditMode) {
        if(goEditMode) {
            invalidateOptionsMenu(); // recreate options menu

            mFileAdapter.setEditMode(FilesAdapter.EDIT_MODE_EDITING);
        } else {
            mFileAdapter.setEditMode(FilesAdapter.EDIT_MODE_NORMAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(mMenuAnimatorBuilder.isExpanded()) {
            mMenuAnimatorBuilder.closed();
        } else if(mFabState == STATE_ANIMATING) {
            // nothing
        } else if(mFabState == STATE_EXPANDED) {
            reverseAnimateFab();
        } else if(mFileAdapter.isEditingMode()) {
            mFileAdapter.setEditMode(FilesAdapter.EDIT_MODE_NORMAL);

            invalidateOptionsMenu();
        } else if(mFileStack.size() > 0) {
            doScanDir(mFileStack.pop());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mFabState == STATE_ANIMATING) {
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mFileAdapter != null && mFileAdapter.isEditingMode()) {
            getMenuInflater().inflate(R.menu.edit_mode_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);

            menu.findItem(R.id.action_show_hidden_files)
                    .setTitle(mSettingsPrefs.getShowHiddenFiles()
                            ? R.string.action_not_show_hidden_files
                            : R.string.action_show_hidden_files);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_show_hidden_files: {
                mSettingsPrefs.setShowHiddenFiles(!mSettingsPrefs.getShowHiddenFiles());
                mFileAdapter.setHiddenMode(mSettingsPrefs.getShowHiddenFiles()
                        ? FilesAdapter.MODE_SHOW_HIDDEN_FILES
                        : FilesAdapter.MODE_NOT_SHOW_HIDDEN_FILES);

                // update menu item title
                item.setTitle(mSettingsPrefs.getShowHiddenFiles()
                        ? R.string.action_not_show_hidden_files : R.string.action_show_hidden_files);
            }break;

            /////////////////// edit mode ///////////////////
            case R.id.action_delete: {
                DialogUtils.showConfirmDialog(this, null,
                        getString(R.string.msg_delete_items, mFileAdapter.getChosenItems().size()),
                        new DialogUtils.ButtonAction() {
                            @Override
                            public String getTitle() {
                                return getString(R.string.ok);
                            }

                            @Override
                            public void onClick(DialogInterface dialog) {
                                // delete items real
                                requestToDeleteChosenFilesAndNotify();
                            }
                        },
                        new DialogUtils.ButtonAction() {
                            @Override
                            public String getTitle() {
                                return getString(R.string.cancel);
                            }

                            @Override
                            public void onClick(DialogInterface dialog) {
                            }
                        });
            }break;

            case R.id.action_info: {
                if(mFileAdapter.getChosenItems().size() == 1) {
                    FileItemModel itemModel = mFileAdapter.getChosenItems().get(0);
                    File chosenFile = new File(itemModel.filepath);

                    View contentView = LayoutInflater.from(this).inflate(R.layout.dialog_file_info,
                            null);
                    TextView fileTypeTv = ButterKnife.findById(contentView, R.id.file_type_tv);
                    fileTypeTv.setText(FileUtils.getMimeType(chosenFile));
                    TextView filePathTv = ButterKnife.findById(contentView, R.id.file_path_tv);
                    filePathTv.setText(chosenFile.getAbsolutePath());
                    TextView fileSizeTv = ButterKnife.findById(contentView, R.id.file_size_tv);
                    fileSizeTv.setText(FileUtils.formatFileSize(chosenFile.length()));
                    TextView fileLastModifiedTv = ButterKnife.findById(contentView,
                            R.id.file_last_modified_time_tv);
                    fileLastModifiedTv.setText(DateUtils.formatTime(chosenFile.lastModified()));

                    DialogUtils.showCustomDialog(this, contentView);
                }
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void requestToDeleteChosenFilesAndNotify() {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        List<FileItemModel> deleteItems = new ArrayList<>();
                        for (FileItemModel deleteItem : mFileAdapter.getChosenItems()) {
                            boolean deleteResult = FileUtils.deleteFile(deleteItem.filepath);
                            if (deleteResult)
                                deleteItems.add(deleteItem);

                            L.d(TAG, "okAction, delete file result: " + deleteResult
                                    + ", delele file: " + deleteItem.filepath);
                        }

                        mFileAdapter.removeItems(deleteItems);
                    }

                    @Override
                    public void onDenied(String permission) {
                        Snackbar.make(mFileRv, getString(R.string.msg_permission_denied),
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void showCreateFolderDialog() {
        DialogUtils.showInputDialog(getActivity(), getString(R.string.tv_create_folder),
                getString(R.string.hint_input_new_folder_name),
                new DialogUtils.InputCallback() {
                    @Override
                    public void onInput(DialogInterface dialog, final String s) {
                        final File newFolder = new File(mCurrentDir, s);
                        if (newFolder.exists()) {
                            Snackbar.make(mFileRv, R.string.msg_folder_is_exist,
                                    Snackbar.LENGTH_LONG).show();

                            return;
                        }

                        if(newFolder.mkdirs()) {
                            // refresh current dir
                            doScanDir(new ScanDirBean(mCurrentDir, 0, 0));

                            // TODO for good UX, auto select the new folder
                        }
                    }
                });
    }

    private void doScanDir(final ScanDirBean dirBean) {
        if(dirBean.dir.isDirectory() && dirBean.dir.exists()) {
            mCurrentDir = dirBean.dir;

            new FileScannerTask(new FileScannerTask.OnScannerListener() {

                @Override
                public void onScanDone(List<FileItemModel> files) {
                    // add parent directory
                    mFileAdapter.setData(files);

                    mLayoutMgr.scrollToPositionWithOffset(dirBean.lastTopPosition,
                            dirBean.lastTopPositionOffset);
                }
            }).execute(mCurrentDir);
        }
    }

}
