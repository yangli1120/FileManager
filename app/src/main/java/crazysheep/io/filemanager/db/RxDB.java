package crazysheep.io.filemanager.db;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * crud use rx
 *
 * Created by crazysheep on 15/12/15.
 */
public class RxDB {

    public interface OnQueryListener<T extends Model> {
        void onResult(List<T> results);
        void onError(String err);
    }

    public interface OnSaveListener {
        void onSuccess();
        void onError(String err);
    }

    public interface OnDeleteListener {
        void onSuccess(int count);
        void onError(String err);
    }

    public static class QueryInfo<T extends Model> {

        private Class<T> mTable;
        private String mWhere;
        private String mOrderBy;

        public QueryInfo(@NonNull Class<T> table,
                         String where, String orderBy) {
            mTable = table;
            mWhere = where;
            mOrderBy = orderBy;
        }
    }

    /**
     * query database
     * */
    public static <T extends Model> Subscription query(@NonNull final QueryInfo queryInfo,
                                               @NonNull final OnQueryListener<T> listener) {
        return Observable.just(queryInfo)
                .subscribeOn(Schedulers.io())
                .map(new Func1<QueryInfo, List<T>>() {
                    @Override
                    public List<T> call(QueryInfo queryInfo) {
                        From from = new Select().from(queryInfo.mTable);
                        if (!TextUtils.isEmpty(queryInfo.mWhere))
                            from.where(queryInfo.mWhere);
                        if (!TextUtils.isEmpty(queryInfo.mOrderBy))
                            from.orderBy(queryInfo.mOrderBy);
                        return from.execute();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<T>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e == null ? "unknow database exception" : e.getMessage());
                    }

                    @Override
                    public void onNext(List<T> objects) {
                        listener.onResult(objects);
                    }
                });
    }

    /**
     * save database
     * */
    public static <T extends Model> void save(@NonNull List<T> datas,
                                              final OnSaveListener listener) {
        Observable.just(datas)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<T>, Boolean>() {
                    @Override
                    public Boolean call(List<T> ts) {
                        ActiveAndroid.beginTransaction();
                        try {
                            for (T t : ts)
                                t.save();
                            ActiveAndroid.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();

                            throw Exceptions.propagate(e);
                        } finally {
                            ActiveAndroid.endTransaction();
                        }

                        return Boolean.TRUE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        if (listener != null)
                            listener.onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null)
                            listener.onError(e == null ? "unknow save exception" : e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

    /**
     * delete records
     * */
    public static <T extends Model> void delete(@NonNull final List<T> items,
                                                final OnDeleteListener listener) {
        Observable.just(items)
                .subscribeOn(Schedulers.io())
                .map(new Func1<List<T>, Boolean>() {
                    @Override
                    public Boolean call(List<T> ts) {
                        ActiveAndroid.beginTransaction();
                        try {
                            for(T t : ts)
                                t.delete();

                            ActiveAndroid.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();

                            throw Exceptions.propagate(e);
                        } finally {
                            ActiveAndroid.endTransaction();
                        }

                        return Boolean.TRUE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        if(listener != null)
                            listener.onSuccess(items.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(listener != null)
                            listener.onError(e == null
                                    ? "unknow delete exception" : e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

}
