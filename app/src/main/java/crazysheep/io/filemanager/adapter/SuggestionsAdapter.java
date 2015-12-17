package crazysheep.io.filemanager.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import crazysheep.io.filemanager.R;
import crazysheep.io.filemanager.model.SuggestionDto;

/**
 * suggestions adapter for search file
 *
 * Created by crazysheep on 15/12/16.
 */
public class SuggestionsAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private OnItemRemoveListener mListener;

    public SuggestionsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = mInflater.inflate(R.layout.layout_suggestion_item, parent, false);
        ViewHolder holder = new ViewHolder(convertView);
        convertView.setTag(holder);

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mSuggestionTv.setText(cursor.getString(
                cursor.getColumnIndex(SuggestionDto.COLUMN_SUGGESTION)));
        holder.mRemoveIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null)
                    mListener.onItemRemove(cursor, cursor.getPosition());
            }
        });
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        mListener = listener;
    }

    public interface OnItemRemoveListener {
        void onItemRemove(Cursor cursor, int position);
    }

    protected static class ViewHolder {

        public View itemView;
        public @Bind(R.id.suggestion_tv) TextView mSuggestionTv;
        public @Bind(R.id.remove_iv) ImageView mRemoveIv;

        public ViewHolder(View parent) {
            itemView = parent;
            ButterKnife.bind(this, parent);
        }
    }
}
