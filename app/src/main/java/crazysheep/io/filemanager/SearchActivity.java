package crazysheep.io.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.activeandroid.query.Select;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.adapter.RecyclerViewBaseAdapter;
import crazysheep.io.filemanager.adapter.SearchFilesAdapter;
import crazysheep.io.filemanager.db.RxDB;
import crazysheep.io.filemanager.io.FileIO;
import crazysheep.io.filemanager.model.FileSuggestion;
import crazysheep.io.filemanager.model.SuggestionDto;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * search file
 *
 * Created by crazysheep on 15/12/14.
 */
public class SearchActivity extends BaseSwipeBackActivity {

    public static final String EXTRA_FILE_PATH = "file_path";

    @Bind(R.id.file_rv) RecyclerView mResultRv;
    @Bind(R.id.floating_sv) FloatingSearchView mFloatingSv;
    private LinearLayoutManager mLayoutMgr;
    private SearchFilesAdapter mFilesAdapter;

    private String mLastQueryString;
    private Subscription mCurSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        initUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelSubscription();
    }

    private void initUI() {
        mLayoutMgr = new LinearLayoutManager(this);
        mResultRv.setLayoutManager(mLayoutMgr);
        mFilesAdapter = new SearchFilesAdapter(this, null, null);
        mResultRv.setAdapter(mFilesAdapter);
        mFilesAdapter.setOnItemClickListener(new RecyclerViewBaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                File chosenFile = mFilesAdapter.getItem(position);
                // show chosen file's parent directory
                Intent data = new Intent();
                data.putExtra(EXTRA_FILE_PATH, chosenFile.getParentFile().getAbsolutePath());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        // search view
        mFloatingSv.setSearchFocused(true);
        mFloatingSv.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                mLastQueryString = newQuery;

                if (!TextUtils.isEmpty(newQuery) && newQuery.length() > 1) {
                    queryDb(newQuery);
                } else {
                    mFloatingSv.clearSuggestions();
                }
            }
        });
        mFloatingSv.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                finish();
            }
        });
        mFloatingSv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                searchFiles(searchSuggestion.getBody());
            }

            @Override
            public void onSearchAction() {
                if (!TextUtils.isEmpty(mLastQueryString)) {
                    saveOrUpdateDb(mLastQueryString);
                    searchFiles(mLastQueryString);
                }

                mLastQueryString = null;
            }
        });
        mFloatingSv.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                mFloatingSv.hideProgress();
                cancelSubscription();
            }

            @Override
            public void onFocusCleared() {
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void queryDb(@NonNull final String keyword) {
        cancelSubscription();

        final RxDB.QueryInfo queryInfo = new RxDB.QueryInfo(SuggestionDto.class, null,
                "date ASC");
        mCurSubscription = RxDB.query(queryInfo, new RxDB.OnQueryListener<SuggestionDto>() {
            @Override
            public void onResult(List<SuggestionDto> results) {
                // if keyword is not null, compile it or show recent search files
                List<SuggestionDto> filerResults = new ArrayList<>();
                for (SuggestionDto item : results)
                    if (item.suggestion.contains(keyword)
                            || item.suggestion.equalsIgnoreCase(keyword))
                        filerResults.add(item);
                List<FileSuggestion> suggestions = new ArrayList<>(filerResults.size());
                for (SuggestionDto suggestionDto : filerResults)
                    suggestions.add(new FileSuggestion(suggestionDto));

                // show search suggestions
                mFloatingSv.swapSuggestions(suggestions);
            }

            @Override
            public void onError(String err) {
            }
        });
    }

    private void saveOrUpdateDb(@NonNull String keyword) {
        // first query if keyword is exist
        Observable.just(keyword)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        List<SuggestionDto> results = new Select().from(SuggestionDto.class)
                                .where(SuggestionDto.COLUMN_SUGGESTION + " = ?", s)
                                .execute();
                        if (results.size() > 0) {
                            // suggestion already exist, update
                            SuggestionDto result = results.get(0);
                            result.date = System.currentTimeMillis();
                            result.save();

                            return Boolean.TRUE;
                        } else {
                            // save a new record
                            SuggestionDto suggestionDto = new SuggestionDto(s,
                                    System.currentTimeMillis());
                            suggestionDto.save();

                            return Boolean.FALSE;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                    }
                });
    }

    private void searchFiles(@NonNull final String query) {
        mFloatingSv.showProgress();
        mCurSubscription = FileIO.list(query, Environment.getExternalStorageDirectory(),
                new FileIO.OnIOSearchListener() {
                    @Override
                    public void onResult(List<File> files) {
                        mFloatingSv.hideProgress();

                        mFilesAdapter.setData(files, query);
                    }

                    @Override
                    public void onError(String err) {
                        mFloatingSv.hideProgress();
                    }
                });
    }

    private void cancelSubscription() {
        if(mCurSubscription != null && !mCurSubscription.isUnsubscribed()) {
            mCurSubscription.unsubscribe();
        }
        mCurSubscription = null;
    }

}
