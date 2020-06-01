package com.example.quizapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quizapp.R;
import com.example.quizapp.models.QuizListModel;
import com.example.quizapp.models.QuizListViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder>{

    private List<QuizListModel> quizListModels;
    private OnQuizListItemClicked onQuizListItemClicked;

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked) {
        this.onQuizListItemClicked = onQuizListItemClicked;
    }

    public void setQuizListModels(List<QuizListModel> quizListModels) {
        this.quizListModels = quizListModels;
    }
    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final QuizViewHolder holder, int position) {

        final QuizListModel model = quizListModels.get(position);
        String url = model.getImage();
        Glide.with(holder.itemView.getContext())
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .into(holder.listImage);
        holder.listTitle.setText(model.getName());

        String listDescription = model.getDesc();
        if (listDescription.length() > 150) {
            listDescription = listDescription.substring(0, 150);
        }
        holder.listDesc.setText(listDescription + "...");

        holder.listDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.listDesc.setText(model.getDesc());
            }
        });
        holder.listLevel.setText(model.getLevel());

    }

    @Override
    public int getItemCount() {
        return quizListModels == null ? 0 : quizListModels.size();
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        ImageView listImage;
        TextView listTitle;
        TextView listDesc;
        TextView listLevel;
        Button listBtn;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            listImage = itemView.findViewById(R.id.list_image);
            listTitle = itemView.findViewById(R.id.list_title);
            listDesc = itemView.findViewById(R.id.list_desc);
            listLevel = itemView.findViewById(R.id.list_difficulty);
            listBtn = itemView.findViewById(R.id.list_btn);
            listBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.list_btn) {
                onQuizListItemClicked.onItemClicked(getAdapterPosition());
            }
        }
    }
    public interface OnQuizListItemClicked{
        void onItemClicked(int position);
    }
}
