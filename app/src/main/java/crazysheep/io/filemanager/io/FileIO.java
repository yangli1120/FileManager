package crazysheep.io.filemanager.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * io operations
 *
 * Created by crazysheep on 15/12/6.
 */
public class FileIO {

    public interface OnIOActionListener {
        void onSuccess();
        void onError(String err);
    }

    public interface OnIOSizeListener {
        void onSizeOf(long size);
        void onError(String err);
    }

    /**
     * move files to target directory
     * */
    public static void move(@NonNull final List<File> sources, @NonNull final File targetDir,
                           @Nullable final OnIOActionListener listener) {
        Observable.from(sources)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        if (file.getParentFile().getAbsolutePath().equals(
                                targetDir.getAbsolutePath()))
                            throw Exceptions.propagate(new Error("target directory to move" +
                                    " can not be current directory"));

                        try {
                            if (file.isDirectory())
                                FileUtils.moveDirectoryToDirectory(file, targetDir, true);
                            else
                                FileUtils.moveFileToDirectory(file, targetDir, true);
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();

                            throw Exceptions.propagate(e);
                        }

                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        if(listener != null)
                            listener.onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(listener != null && e != null)
                            listener.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

    /**
     * copy files to target directory
     * */
    public static void copy(@NonNull List<File> sources, @NonNull final File targetDir,
                     final OnIOActionListener listener) {
        Observable.from(sources)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        if (file.getParentFile().getAbsolutePath().equals(
                                targetDir.getAbsolutePath()))
                            throw Exceptions.propagate(new Error("target directory to copy" +
                                    " can not be current directory"));

                        try {
                            if (file.isDirectory())
                                FileUtils.copyDirectoryToDirectory(file, targetDir);
                            else
                                FileUtils.copyFileToDirectory(file, targetDir);
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();

                            throw Exceptions.propagate(e);
                        }

                        return Boolean.TRUE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        if (listener != null)
                            listener.onSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (listener != null && e != null)
                            listener.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                    }
                });
    }

    /**
     * get size of file or directory
     * */
    public static void size(@NonNull List<File> files, final OnIOSizeListener listener) {
        final List<Long> sizelist = new ArrayList<>(files.size());
        Observable.from(files)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, Long>() {
                    @Override
                    public Long call(File file) {
                        return crazysheep.io.filemanager.utils.FileUtils.sizeOfFile(file);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        long totalSize = 0;
                        for(Long aLong : sizelist)
                            totalSize += aLong;

                        if(listener != null)
                            listener.onSizeOf(totalSize);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(listener != null && e != null)
                            listener.onError(e.getMessage());
                    }

                    @Override
                    public void onNext(Long aLong) {
                        sizelist.add(aLong == null ? 0 : aLong);
                    }
                });
    }

}
