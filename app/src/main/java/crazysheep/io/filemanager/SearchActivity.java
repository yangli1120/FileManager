package crazysheep.io.filemanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.filemanager.adapter.SearchFilesAdapter;
import crazysheep.io.filemanager.adapter.SuggestionsAdapter;
import crazysheep.io.filemanager.db.RxDB;
import crazysheep.io.filemanager.io.FileIO;
import crazysheep.io.filemanager.model.SuggestionDto;
import crazysheep.io.filemanager.utils.DialogUtils;
import crazysheep.io.filemanager.widget.DividerItemDecoration;

/**
 * search file
 *
 * Created by crazysheep on 15/12/14.
 */
public class SearchActivity extends BaseSwipeBackActivity {

    public static final String EXTRA_FILE_PATH = "file_path";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.file_rv) RecyclerView mResultRv;
    private LinearLayoutManager mLayoutMgr;
    private SearchFilesAdapter mFilesAdapter;
    private SearchView mSearchView;
    private SuggestionsAdapter mSuggestionsAdapter;

    private Dialog mLoadingDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        initActionBar();
        initUI();
    }

    private void initUI() {
        mLayoutMgr = new LinearLayoutManager(this);
        mResultRv.setLayoutManager(mLayoutMgr);
        mFilesAdapter = new SearchFilesAdapter(this, null, null);
        mResultRv.setAdapter(mFilesAdapter);
        mResultRv.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST,
                getResources().getColor(R.color.light_grey)));
        mFilesAdapter.setOnItemClickListener(new RecyclerViewBaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                File chosenFile = mFilesAdapter.getItem(position);
                // when user click one file, save file name to database,
                // for suggestion
                saveDb(chosenFile.getName());

                // show chosen file's parent directory
                Intent data = new Intent();
                data.putExtra(EXTRA_FILE_PATH, chosenFile.getParentFile().getAbsolutePath());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    private void initActionBar() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);

        mSuggestionsAdapter = new SuggestionsAdapter(this, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // see{#http://stackoverflow.com/questions/18438890/menuitemcompat-getactionview-always-returns-null}
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setIconified(false);
        mSearchView.setQueryHint(getString(R.string.hint_input_file_name_search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                mSearchView.clearFocus();
                mLoadingDlg = DialogUtils.showLoadingDialog(getActivity(),
                        getString(R.string.tv_please_waiting), false, false);

                // search file on disk
                FileIO.list(query, Environment.getExternalStorageDirectory(),
                        new FileIO.OnIOSearchListener() {
                            @Override
                            public void onResult(List<File> files) {
                                DialogUtils.dismissDialog(mLoadingDlg);

                                mFilesAdapter.setData(files, query);
                            }

                            @Override
                            public void onError(String err) {
                                DialogUtils.dismissDialog(mLoadingDlg);
                            }
                        });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText) && newText.length() > 1)
                    queryDb(newText);
                else
                    mSuggestionsAdapter.changeCursor(null);

                return true;
            }
        });
        mSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // auto complete to edit text
                Cursor cursor = (Cursor) mSuggestionsAdapter.getItem(position);
                String suggestion = cursor.getString(
                        cursor.getColumnIndex(SuggestionDto.COLUMN_SUGGESTION));
                mSearchView.setQuery(suggestion, true);

                return true;
            }
        });
        mSuggestionsAdapter.setOnItemRemoveListener(new SuggestionsAdapter.OnItemRemoveListener() {
            @Override
            public void onItemRemove(Cursor cursor, int position) {
                // TODO remove item from database
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    private void queryDb(@NonNull final String keyword) {
        final RxDB.QueryInfo queryInfo = new RxDB.QueryInfo(SuggestionDto.class, null,
                "date ASC");
        RxDB.query(queryInfo, queryInfo.getUUID(),
                new RxDB.OnQueryListener<SuggestionDto>() {
                    @Override
                    public void onResult(List<SuggestionDto> results, String requestKey) {
                        if(queryInfo.getUUID().equals(requestKey)) {
                            // if keyword is not null, compile it or show recent search files
                            List<SuggestionDto> filerResults = new ArrayList<>();
                            for(SuggestionDto item : results)
                                if(item.suggestion.contains(keyword)
                                        || item.suggestion.equalsIgnoreCase(keyword))
                                    filerResults.add(item);
                            Cursor cursor = data2Cursor(filerResults);
                            mSuggestionsAdapter.changeCursor(cursor);
                        }
                    }

                    @Override
                    public void onError(String err) {
                    }
        });
    }

    private void saveDb(@NonNull final String keyword) {
        SuggestionDto suggestionDto = new SuggestionDto(keyword, System.currentTimeMillis());
        List<SuggestionDto> saveDatas = new ArrayList<>();
        saveDatas.add(suggestionDto);
        RxDB.save(saveDatas, null);
    }

    private Cursor data2Cursor(List<SuggestionDto> results) {
        MatrixCursor cursor = new MatrixCursor(SuggestionDto.COLUMNS);
        for(SuggestionDto suggestionDto : results) {
            String[] row = new String[3];
            row[0] = suggestionDto.id;
            row[1] = suggestionDto.suggestion;
            row[2] = String.valueOf(suggestionDto.date);

            cursor.addRow(row);
        }

        return cursor;
    }

}
