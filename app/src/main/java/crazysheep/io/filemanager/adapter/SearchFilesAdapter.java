package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
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
public class SearchFilesAdapter extends RecyclerViewBaseAdapter<SearchFilesAdapter.FileHolder, File> {

    private String mSearchKeyword;

    public SearchFilesAdapter(@NonNull Context context, List<File> items, String keyword) {
        super(context, items);
        mSearchKeyword = keyword;
    }

    public void setData(List<File> items, String keyword) {
        super.setData(items);
        mSearchKeyword = keyword;
    }

    @Override
    public FileHolder onCreateHolder(ViewGroup parent, int viewType) {
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
