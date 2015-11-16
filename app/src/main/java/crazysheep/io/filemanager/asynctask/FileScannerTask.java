package crazysheep.io.filemanager.asynctask;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import crazysheep.io.filemanager.model.FileItemModel;

/**
 * async task for scanner target directory
 *
 * Created by crazysheep on 15/11/12.
 */
public class FileScannerTask extends AsyncTask<File, Integer, List<FileItemModel>> {

    private OnScannerListener mListener;

    public FileScannerTask(OnScannerListener listener) {
        mListener = listener;
    }

    @Override
    protected List<FileItemModel> doInBackground(@NonNull File... params) {
        File targetFile = params[0];

        if(targetFile == null)
            throw new RuntimeException("target scan directory is NULL");

        if(!targetFile.isDirectory())
            throw new RuntimeException("target file is NOT a directory");

        List<FileItemModel> files = new ArrayList<>();
        if(targetFile.isDirectory()) {
            for(File file : targetFile.listFiles())
                files.add(createFileItemModelFromFile(file));
        } else {
            throw new RuntimeException("target file is NOT a directory, file path : "
                    + targetFile.getAbsolutePath());
        }

        return files;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if(mListener != null)
            mListener.onProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(List<FileItemModel> fileItemModels) {
        super.onPostExecute(fileItemModels);

        if(mListener != null)
            mListener.onScanDone(fileItemModels);
    }

    //////////////////// listener /////////////////////////////

    public static class OnScannerListener {
        public void onProgressUpdate(int progress) {}
        public void onScanDone(List<FileItemModel> files) {}
    }

    private FileItemModel createFileItemModelFromFile(@NonNull File file) {
        FileItemModel itemModel = new FileItemModel();
        itemModel.filename = file.getName();
        itemModel.filepath = file.getAbsolutePath();
        itemModel.filetype = file.isDirectory() ? FileItemModel.TYPE_DIR : FileItemModel.TYPE_FILE;
        itemModel.isHidden = file.isHidden();
        itemModel.subfileCount = file.isDirectory()
                ? file.listFiles().length : FileItemModel.ILLEGAL_SUBFILE_COUNT;
        itemModel.fileByteCount = file.isDirectory()
                ? FileItemModel.ILEGAL_FILE_BYTE_COUNT : file.length();
        itemModel.fileLastModified = file.lastModified();

        return itemModel;
    }

}
