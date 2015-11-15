package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.R;
import crazysheep.io.filemanager.model.FileItemModel;

/**
 * Created by crazysheep on 15/11/12.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileHolder> {

    public static final int MODE_SHOW_HIDDEN_FILES = 0;
    public static final int MODE_NOT_SHOW_HIDDEN_FILES = 1;
    private int mCurrentMode = MODE_NOT_SHOW_HIDDEN_FILES;

    private Context mContext;
    private List<FileItemModel> mAllFiles;
    private List<FileItemModel> mFiles;
    private LayoutInflater mInflater;

    public FilesAdapter(Context context, List<FileItemModel> files, int mode) {
        mContext = context;
        mAllFiles = files;
        mCurrentMode = mode;
        mInflater = LayoutInflater.from(mContext);

        if(mAllFiles == null)
            mAllFiles = new ArrayList<>();
        sortFiles();
        filterHidden();
    }

    public void setData(List<FileItemModel> files) {
        mAllFiles = files;
        if(mAllFiles == null)
            mAllFiles = new ArrayList<>();
        sortFiles();
        filterHidden();

        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        mCurrentMode = mode;
        filterHidden();

        notifyDataSetChanged();
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(R.layout.layout_file_item, parent, false);

        return new FileHolder(convertView);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        FileItemModel itemModel = mFiles.get(position);
        if(itemModel.isDir())
            holder.mFileCoverIv.setImageResource(R.drawable.ic_folder_black_48dp);
        else
            holder.mFileCoverIv.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);

        holder.mFileNameTv.setText(itemModel.filename);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    private void sortFiles() {
        List<FileItemModel> dirs = new ArrayList<>();
        List<FileItemModel> files = new ArrayList<>();
        for(FileItemModel itemModel : mAllFiles)
            if(itemModel.isDir())
                dirs.add(itemModel);
            else
                files.add(itemModel);

        // first sort directory files
        Collections.sort(dirs, new FileComparator());
        // second sort files
        Collections.sort(files, new FileComparator());

        List<FileItemModel> totalFiles = new ArrayList<>(mAllFiles.size());
        totalFiles.addAll(dirs);
        totalFiles.addAll(files);
        mAllFiles = totalFiles;
    }

    private static class FileComparator implements Comparator<FileItemModel> {

        @Override
        public int compare(FileItemModel lhs, FileItemModel rhs) {
            return lhs.filename.compareTo(rhs.filename);
        }
    }

    private void filterHidden() {
        if(mCurrentMode == MODE_SHOW_HIDDEN_FILES) {
            mFiles = mAllFiles;
        } else {
            mFiles = new ArrayList<>();
            for(FileItemModel itemModel : mAllFiles)
                if(!itemModel.isHidden())
                    mFiles.add(itemModel);
        }
    }

    ///////////////////////////// ViewHolder //////////////////////////////////

    public static class FileHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.file_cover_iv) ImageView mFileCoverIv;
        @Bind(R.id.file_name_tv) TextView mFileNameTv;

        public FileHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
