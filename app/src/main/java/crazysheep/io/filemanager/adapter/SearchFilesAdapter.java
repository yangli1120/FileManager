package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.R;
import crazysheep.io.filemanager.utils.DateUtils;
import crazysheep.io.filemanager.utils.StringUtils;

/**
 * search files adapter
 *
 * Created by crazysheep on 15/12/16.
 */
public class SearchFilesAdapter extends RecyclerView.Adapter<SearchFilesAdapter.FileHolder> {

    private Context mContext;
    private List<File> mFiles;
    private String mSearchKeyword;
    private LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public SearchFilesAdapter(@NonNull Context context, List<File> items, String keyword) {
        mContext = context;
        mFiles = items;
        mSearchKeyword = keyword;
        if(mFiles == null)
            mFiles = new ArrayList<>();

        mInflater = LayoutInflater.from(mContext);
    }

    public void setData(List<File> itemDtos, String keyword) {
        mFiles = itemDtos;
        mSearchKeyword = keyword;
        if(mFiles == null)
            mFiles = new ArrayList<>();

        notifyDataSetChanged();
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileHolder(mInflater.inflate(R.layout.layout_search_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        File file = getItem(position);
        holder.mFileNameTv.setText(StringUtils.highlight(mSearchKeyword, file.getName(),
                Color.RED));
        holder.mFilePathTv.setText(StringUtils.highlight(mSearchKeyword, file.getAbsolutePath(),
                Color.BLUE));
        holder.mLastModifiedTv.setText(DateUtils.formatTime(file.lastModified()));
        holder.mFileTypeIv.setImageResource(R.drawable.ic_insert_drive_file);

        updateClickListener(holder);
    }

    @Override
    public void onViewRecycled(FileHolder holder) {
        super.onViewRecycled(holder);

        updateClickListener(holder);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public File getItem(int position) {
        return mFiles.get(position);
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
                if (mOnItemLongClickListener != null)
                    mOnItemLongClickListener.onLongClick(holder.getAdapterPosition(),
                            holder.itemView);

                return true;
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(int position, View view);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(int position, View view);
    }

    static class FileHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.file_name_tv) TextView mFileNameTv;
        @Bind(R.id.file_path_tv) TextView mFilePathTv;
        @Bind(R.id.file_last_modified_time_tv) TextView mLastModifiedTv;
        @Bind(R.id.file_type_iv) ImageView mFileTypeIv;

        public FileHolder(View parent) {
            super(parent);
            ButterKnife.bind(this, parent);
        }
    }

}
