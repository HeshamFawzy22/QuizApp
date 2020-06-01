package com.example.quizapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quizapp.models.QuizListModel;
import com.example.quizapp.models.QuizListViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class DetailsFragment extends Fragment implements View.OnClickListener {

    //Declare
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String quizName;

    private int position;
    private long totalQuestions = 0;
    private ImageView detailsImage;
    private TextView detailsTitle;
    private TextView detailsDesc;
    private TextView detailsDeff;
    private TextView detailsQuestions;
    private TextView detailsScore;
    private Button detailsStartBtn;

    NavController navController;
    private QuizListViewModel quizListViewModel;
    private String quizId;

    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        detailsImage = view.findViewById(R.id.details_image);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDeff = view.findViewById(R.id.details_difficulty_txt);
        detailsScore = view.findViewById(R.id.details_score_txt);
        detailsDesc = view.findViewById(R.id.details_desc);
        detailsQuestions = view.findViewById(R.id.details_questions_txt);

        detailsStartBtn = view.findViewById(R.id.details_start_btn);
        detailsStartBtn.setOnClickListener(this);

        //Load Previous Results
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                Glide.with(getActivity())
                        .load(quizListModels.get(position).getImage())
                        .placeholder(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(detailsImage);
                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDeff.setText(quizListModels.get(position).getLevel());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsQuestions.setText(quizListModels.get(position).getQuestions() + "");

                //Assign value to quiz id variable
                quizId = quizListModels.get(position).getQuiz_id();
                quizName = quizListModels.get(position).getName();
                totalQuestions = quizListModels.get(position).getQuestions();

                //Load Results
                loadResultsData();
            }
        });

    }

    private void loadResultsData() {

        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    //Get Result
                    DocumentSnapshot result = task.getResult();

                    if (result != null && result.exists()) {
                        //Get Results
                        Long correct = result.getLong("correct");
                        Long wrong = result.getLong("wrong");
                        Long missed = result.getLong("unanswered");


                        //Calculate Progress
                        long total = correct + wrong + missed;
                        long percent = (correct * 100) / total;



                        detailsScore.setText(percent + "%");
                    }else {
                        //Document Doesn't exist, and result should stay N/A

                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.details_start_btn) {
            DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
            action.setQuizId(quizId);
            action.setQuizName(quizName);
            action.setTotalQuestions(totalQuestions);
            navController.navigate(action);
        }
    }
}
