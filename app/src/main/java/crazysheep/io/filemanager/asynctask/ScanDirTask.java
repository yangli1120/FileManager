package crazysheep.io.filemanager.asynctask;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import crazysheep.io.filemanager.model.FileItemModel;
import crazysheep.io.filemanager.model.FileItemModelHelper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * async task to scan target directory
 *
 * Created by crazysheep on 15/12/5.
 */
public class ScanDirTask {

    public interface OnScanListener {
        void onScanDone(List<FileItemModel> items);
        void onError(String err);
    }

    /**
     * A async task to scan target directory and call listener after scan done.
     * The listener should call at UI thread.
     * */
    public static void doScan(@NonNull File dir, @NonNull final OnScanListener listener) {
        Observable
                .just(dir)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, List<FileItemModel>>() {
                    @Override
                    public List<FileItemModel> call(File targetFile) {
                        if(targetFile == null)
                            throw OnErrorThrowable.from(new Error("target scan directory is NULL"));

                        if(!targetFile.isDirectory())
                            throw OnErrorThrowable.from(new Error("target file is NOT a directory"));

                        List<FileItemModel> files = new ArrayList<>();
                        for(File file : targetFile.listFiles())
                            files.add(FileItemModelHelper.createFileItemModelFromFile(file));

                        return files;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<FileItemModel>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e.toString());
                    }

                    @Override
                    public void onNext(List<FileItemModel> fileItemModels) {
                        listener.onScanDone(fileItemModels);
                    }
                });
    }
}
