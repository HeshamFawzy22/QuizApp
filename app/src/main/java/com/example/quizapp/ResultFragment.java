package com.example.quizapp;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ResultFragment extends Fragment implements View.OnClickListener {

    //Declare
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    NavController navController;
    String quizId;
    String currentUserId;

    // UI Elements
    protected View rootView;
    private TextView resultsTitle;
    private ProgressBar resultsProgress;
    private TextView resultsPercent;
    private TextView resultsCorrect;
    private TextView resultsWrong;
    private TextView resultsMissed;
    private Button resultsHomeBtn;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        //get User Id
        if (firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else {
            //Go back to home page
        }

        firebaseFirestore = FirebaseFirestore.getInstance();
        quizId = ResultFragmentArgs.fromBundle(getArguments()).getQuizId();

        //get Results
        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Results")
                .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();

                    Long correct = result.getLong("correct");
                    Long wrong = result.getLong("wrong");
                    Long missed = result.getLong("unanswered");

                    resultsCorrect.setText(correct.toString());
                    resultsWrong.setText(wrong.toString());
                    resultsMissed.setText(missed.toString());

                    //Calculate Progress
                    Long total = correct + wrong + missed;
                    Long percent = (correct * 100)/total;

                    resultsPercent.setText(percent+"%");
                    resultsProgress.setProgress(percent.intValue());
                }else {

                }
            }
        });
    }

    @Override
    public void onClick(View view) {
                if (view.getId() == R.id.results_home_btn) {
                    navController.navigate(R.id.action_resultFragment_to_listFragment);
                }
    }

    private void initView(View rootView) {
        resultsTitle = (TextView) rootView.findViewById(R.id.results_title);
        resultsProgress = (ProgressBar) rootView.findViewById(R.id.results_progress);
        resultsPercent = (TextView) rootView.findViewById(R.id.results_percent);
        resultsCorrect = (TextView) rootView.findViewById(R.id.results_correct_txt);
        resultsWrong = (TextView) rootView.findViewById(R.id.results_wrong_txt);
        resultsMissed = (TextView) rootView.findViewById(R.id.results_missed_txt);
        resultsHomeBtn = (Button) rootView.findViewById(R.id.results_home_btn);
        resultsHomeBtn.setOnClickListener(ResultFragment.this);
    }
}
