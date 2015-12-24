package crazysheep.io.filemanager.io;

import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

    public interface OnIOSearchListener {
        void onResult(List<File> files);
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

    /**
     * delete file or directory
     */
    public static void delete(@NonNull List<File> files, final OnIOActionListener listener) {
        Observable.from(files)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException | NullPointerException e) {
                            e.printStackTrace();

                            throw Exceptions.propagate(e);
                        }

                        return Boolean.TRUE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        if(listener != null)
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
     * list files under target directory with compile name
     * */
    public static Subscription list(@NonNull final String compileName, @NonNull final File targetDir,
                              @NonNull final OnIOSearchListener listener) {
        return Observable.just(targetDir)
                .subscribeOn(Schedulers.io())
                .map(new Func1<File, List<File>>() {
                    @Override
                    public List<File> call(File file) {
                        Collection<File> files =  FileUtils.listFiles(targetDir,
                                new RegexFileFilter(".*?" + compileName + ".*?",
                                        IOCase.INSENSITIVE), TrueFileFilter.TRUE);

                        return new ArrayList<>(files);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<File>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e == null ? "unknow list exception" : e.getMessage());
                    }

                    @Override
                    public void onNext(List<File> files) {
                        listener.onResult(files);
                    }
                });
    }

    /////////////////////// io with system storage //////////////////////////

    public static final long ERR_UNMOUNTED = -1;

    public static boolean isExternalMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * get external storage available space, not need background action, {@link StatFs} is efficient
     * */
    public static long externalAvailable() {
        return isExternalMounted() ? available(Environment.getExternalStorageDirectory())
                : ERR_UNMOUNTED;
    }

    /**
     * get external storage total space, not need background action, {@link StatFs} is efficient
     * */
    public static long externalTotal() {
        return isExternalMounted() ? total(Environment.getExternalStorageDirectory())
                : ERR_UNMOUNTED;
    }

    /**
     * get external storage used space, not need background action, {@link StatFs} is efficient
     * */
    public static long externalUsed() {
        File externalDir = Environment.getExternalStorageDirectory();
        return isExternalMounted() ? total(externalDir) - available(externalDir) : ERR_UNMOUNTED;
    }

    /**
     * get internal storage available space
     * */
    public static long internalAvailable() {
        return available(Environment.getRootDirectory());
    }

    /**
     * get internal storage total space
     * */
    public static long internalTotal() {
        return total(Environment.getRootDirectory());
    }

    // see{@link http://stackoverflow.com/questions/4799643/getting-all-the-total-and-available-space-on-android]
    private static long available(@NonNull File file) {
        return new StatFs(file.getAbsolutePath()).getAvailableBytes();
    }

    private static long total(@NonNull File file) {
        return new StatFs(file.getAbsolutePath()).getTotalBytes();
    }

}
