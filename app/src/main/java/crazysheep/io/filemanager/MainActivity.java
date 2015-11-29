package crazysheep.io.filemanager;

import android.Manifest;
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
import android.support.v4.view.ViewCompat;
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
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.nineoldandroids.animation.Animator;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.adapter.FilesAdapter;
import crazysheep.io.filemanager.asynctask.FileScannerTask;
import crazysheep.io.filemanager.model.FileItemModel;
import crazysheep.io.filemanager.prefs.SettingsPrefs;
import crazysheep.io.filemanager.utils.DateUtils;
import crazysheep.io.filemanager.utils.DialogUtils;
import crazysheep.io.filemanager.utils.FileUtils;
import crazysheep.io.filemanager.utils.L;
import crazysheep.io.filemanager.widget.DefaultAnimatorListener;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;
import io.codetail.widget.RevealFrameLayout;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.file_rv) RecyclerView mFileRv;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.fab_sheet_cv) CardView mFabSheetCv;
    @Bind(R.id.fab_sheet_rfl) RevealFrameLayout mFabRevealFl;
    private LinearLayoutManager mLayoutMgr;
    private FilesAdapter mFileAdapter;
    private SettingsPrefs mSettingsPrefs;

    private ArcAnimator mArcAnimator;
    private ArcAnimator mReverseArcAnimator;
    private SupportAnimator mRevealAnimator;
    private SupportAnimator mReverseRevealAnimator;

    private File mCurrentDir;
    private LinkedList<ScanDirBean> mFileStack = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSettingsPrefs = new SettingsPrefs(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initUI();
    }

    private final static int[] mFabStartLocation = new int[2];
    private void initUI() {
        mFab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFab.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                mFab.getLocationOnScreen(mFabStartLocation);
            }
        });
        mFabSheetCv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mFabSheetCv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        mRevealAnimator = ViewAnimationUtils.createCircularReveal(mFabSheetCv,
                                mFabSheetCv.getLeft(),
                                mFabSheetCv.getTop(),
                                0,
                                (float) Math.sqrt(Math.pow(mFabSheetCv.getWidth(), 2)
                                        + Math.pow(mFabSheetCv.getHeight(), 2)));
                        mRevealAnimator.setDuration(300);
                        mRevealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                        mReverseRevealAnimator = mRevealAnimator.reverse();
                        mReverseRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {
                            }

                            @Override
                            public void onAnimationEnd() {
                                mFabRevealFl.setVisibility(View.GONE);
                                //mFab.setVisibility(View.VISIBLE);

                                mReverseArcAnimator = ArcAnimator.createArcAnimator(mFab,
                                        mFabStartLocation[0],
                                        mFabStartLocation[1],
                                        360,
                                        Side.RIGHT);
                                mReverseArcAnimator.setDuration(300);
                                mReverseArcAnimator.setInterpolator(new AccelerateInterpolator());
                                mReverseArcAnimator.start();
                            }

                            @Override
                            public void onAnimationCancel() {
                            }

                            @Override
                            public void onAnimationRepeat() {
                            }
                        });

                        int[] sheetLocations = new int[2];
                        mFabSheetCv.getLocationOnScreen(sheetLocations);
                        mArcAnimator = ArcAnimator.createArcAnimator(mFab,
                                sheetLocations[0],
                                sheetLocations[1],
                                360,
                                Side.LEFT);
                        mArcAnimator.setDuration(300);
                        mArcAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        mArcAnimator.addListener(new DefaultAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                //mFab.setVisibility(View.GONE);
                                mFabRevealFl.setVisibility(View.VISIBLE);
                                mRevealAnimator.start();
                            }
                        });
                    }
                });
        mFab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                animateFab();
            }
        });

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
        mFileAdapter.setOnItemLongClickListener(new FilesAdapter.OnItemLongClickListener() {
            @Override
            public boolean onLongClick(int position, View view) {
                // TODO item long click
                if (!mFileAdapter.isEditingMode()) {
                    invalidateOptionsMenu(); // recreate options menu

                    mFileAdapter.setEditMode(FilesAdapter.EDIT_MODE_EDITING);
                    mFileAdapter.toggleItemChoose(position);
                }

                return true;
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

    private boolean isAnimatedFab = false;
    private void animateFab() {
        isAnimatedFab = true;

        mArcAnimator.start();
    }
    private void reverseAnimateFab() {
        isAnimatedFab = false;

        mReverseRevealAnimator.start();
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
        } if(isAnimatedFab) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mFileAdapter != null && mFileAdapter.isEditingMode()) {
            getMenuInflater().inflate(R.menu.edit_mode_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);

            menu.findItem(R.id.action_show_hidden_files).setTitle(mSettingsPrefs.getShowHiddenFiles()
                    ? R.string.action_not_show_hidden_files : R.string.action_show_hidden_files);
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
                        getString(R.string.msg_delete_items, mFileAdapter.getChoosenItems().size()),
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
                if(mFileAdapter.getChoosenItems().size() == 1) {
                    FileItemModel itemModel = mFileAdapter.getChoosenItems().get(0);
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
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

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
                        for (FileItemModel deleteItem : mFileAdapter.getChoosenItems()) {
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

    private static class ScanDirBean {

        public File dir;
        public int lastTopPosition = 0;
        public int lastTopPositionOffset = 0;

        public ScanDirBean(@NonNull File dir, int topPosition, int topPositionOffset) {
            this.dir = dir;
            this.lastTopPosition = topPosition;
            this.lastTopPositionOffset = topPositionOffset;
        }
    }

}
