package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.R;
import crazysheep.io.filemanager.model.FileItemModel;

/**
 * Created by crazysheep on 15/11/12.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileHolder> {

    private Context mContext;
    private List<FileItemModel> mFiles;
    private LayoutInflater mInflater;

    public FilesAdapter(Context context, List<FileItemModel> files) {
        mContext = context;
        mFiles = files;
        mInflater = LayoutInflater.from(mContext);

        if(mFiles == null)
            mFiles = new ArrayList<>();
    }

    public void setData(List<FileItemModel> files) {
        mFiles = files;
        if(mFiles == null)
            mFiles = new ArrayList<>();

        notifyDataSetChanged();
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(R.layout.layout_file_item, parent, false);

        return new FileHolder(convertView);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        holder.mFileNameTv.setText(mFiles.get(position).filename);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
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
