package com.sharmaji.spideystream.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sharmaji.spideystream.R;
import com.sharmaji.spideystream.models.HistoryModel;

public class HistoryAdapter extends ListAdapter<HistoryModel,HistoryAdapter.HistoryViewHolder> {
    private final Context context;
    private final ItemClickListener listener;

    public interface ItemClickListener{
        public void onClick(HistoryModel model);
    }
    // creating a call back for item of recycler view. 
    private static final DiffUtil.ItemCallback<HistoryModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<HistoryModel>() {
        @Override
        public boolean areItemsTheSame(HistoryModel oldItem, HistoryModel newItem) {
            return oldItem.getSource_url_id().equals(newItem.getSource_url_id());
        }

        @Override
        public boolean areContentsTheSame(HistoryModel oldItem, HistoryModel newItem) {
            // below line is to check the course name, description and course duration. 
            return oldItem.getSource_url_id().equals(newItem.getSource_url_id());
        }
    };

    public HistoryAdapter(Context context, ItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryModel item = getItem(position);
        holder.title_txt.setText(item.getName());
        holder.time_txt.setText(item.getTime());
        Drawable movie = ResourcesCompat.getDrawable(context.getResources(), R.drawable.movie, null);
        Drawable series = ResourcesCompat.getDrawable(context.getResources(), R.drawable.series, null);
        holder.icon_img.setImageDrawable(item.getName().contains("Movie") ? movie: series);
        holder.root.setOnClickListener(v -> listener.onClick(item));

    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView title_txt, time_txt ;
        ImageView icon_img;
        ConstraintLayout root;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.item_history);
            title_txt = itemView.findViewById(R.id.txt_title);
            time_txt = itemView.findViewById(R.id.txt_date);
            icon_img = itemView.findViewById(R.id.txt_initial);
        }
    }

}
