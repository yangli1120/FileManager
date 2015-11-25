package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import crazysheep.io.filemanager.utils.DateUtils;
import crazysheep.io.filemanager.utils.FileUtils;

/**
 * adapter for file RecyclerView
 *
 * Created by crazysheep on 15/11/12.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileHolder> {

    public static final int MODE_SHOW_HIDDEN_FILES = 0;
    public static final int MODE_NOT_SHOW_HIDDEN_FILES = 1;
    private int mCurrentMode = MODE_NOT_SHOW_HIDDEN_FILES;

    public static final int EDIT_MODE_NORMAL = 10;
    public static final int EDIT_MODE_EDITING = 11;
    private int mEditMode = EDIT_MODE_NORMAL;

    private Context mContext;
    private List<FileItemModel> mAllFiles;
    private List<FileItemModel> mFiles;
    private LayoutInflater mInflater;
    private SparseArray<Boolean> mChooseFileMap;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public FilesAdapter(Context context, List<FileItemModel> files, int hiddenMode) {
        mContext = context;
        mAllFiles = files;
        mCurrentMode = hiddenMode;
        mInflater = LayoutInflater.from(mContext);
        mChooseFileMap = new SparseArray<>();

        if(mAllFiles == null)
            mAllFiles = new ArrayList<>();
        sortFiles();
        filterHidden();
        resetItemChooseState();
    }

    public void setData(List<FileItemModel> files) {
        mAllFiles = files;
        if(mAllFiles == null)
            mAllFiles = new ArrayList<>();
        sortFiles();
        filterHidden();
        resetItemChooseState();

        notifyDataSetChanged();
    }

    public void setHiddenMode(int mode) {
        mCurrentMode = mode;
        filterHidden();

        notifyDataSetChanged();
    }

    public void setEditMode(int mode) {
        if(mEditMode != mode) {
            mEditMode = mode;

            if(mEditMode == EDIT_MODE_EDITING)
                ;// nothing
            else
                resetItemChooseState();

            notifyDataSetChanged();
        }
    }

    public boolean isEditingMode() {
        return mEditMode == EDIT_MODE_EDITING;
    }

    public void toggleItemChoose(int position) {
        if(mEditMode == EDIT_MODE_EDITING) {
            mChooseFileMap.put(position, !mChooseFileMap.get(position));

            notifyItemChanged(position);
        }
    }

    private void resetItemChooseState() {
        mChooseFileMap = new SparseArray<>(mFiles.size());
        for(int i = 0; i < mFiles.size(); i++)
            mChooseFileMap.put(i, false);
    }

    public List<FileItemModel> getChoosenItems() {
        List<FileItemModel> choosenItems = new ArrayList<>();
        for(int i = 0; i < mChooseFileMap.size(); i++)
            if(mChooseFileMap.valueAt(i))
                choosenItems.add(mFiles.get(mChooseFileMap.keyAt(i)));

        return choosenItems;
    }

    public void removeItems(@NonNull List<FileItemModel> items) {
        for(FileItemModel item : items) {
            int removeIndex = mFiles.indexOf(item);
            mFiles.remove(item);
            mAllFiles.remove(item);

            notifyItemRemoved(removeIndex);
        }

        resetItemChooseState();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
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
            holder.mFileCoverIv.setImageResource(R.drawable.ic_folder_blue);
        else
            holder.mFileCoverIv.setImageResource(R.drawable.ic_insert_drive_file);

        holder.mFileNameTv.setText(itemModel.filename);
        if(itemModel.isDir())
            holder.mFileSubCountTv.setText(
                    itemModel.subfileCount != FileItemModel.ILLEGAL_SUBFILE_COUNT
                            ? mContext.getString(R.string.tv_file_sub_count, itemModel.subfileCount)
                                    : null);
        else
            holder.mFileSubCountTv.setText(FileUtils.formatFileSize(itemModel.fileByteCount));
        holder.mFileLastModifiedTimeTv.setText(DateUtils.formatTime(itemModel.fileLastModified));
        if(isEditingMode()) {
            holder.mFileChooseCb.setVisibility(View.VISIBLE);
            holder.mFileChooseCb.setChecked(mChooseFileMap.get(position));
        } else {
            holder.mFileChooseCb.setVisibility(View.GONE);
        }

        updateClickListener(holder);
    }

    @Override
    public void onViewRecycled(FileHolder holder) {
        super.onViewRecycled(holder);

        updateClickListener(holder);
    }

    private void updateClickListener(final FileHolder holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onClick(holder.getAdapterPosition(), holder.itemView);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if(mOnItemLongClickListener != null)
                    mOnItemLongClickListener.onLongClick(holder.getAdapterPosition(),
                            holder.itemView);

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public FileItemModel getItem(int position) {
        return mFiles.get(position);
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

    public static interface OnItemClickListener {
        public void onClick(int position, View view);
    }

    public static interface OnItemLongClickListener {
        public boolean onLongClick(int position, View view);
    }

    ///////////////////////////// ViewHolder //////////////////////////////////

    public static class FileHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.file_cover_iv) ImageView mFileCoverIv;
        @Bind(R.id.file_name_tv) TextView mFileNameTv;
        @Bind(R.id.file_sub_count_tv) TextView mFileSubCountTv;
        @Bind(R.id.file_last_modified_time_tv) TextView mFileLastModifiedTimeTv;
        @Bind(R.id.file_item_choose_cb) CheckBox mFileChooseCb;

        public FileHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
