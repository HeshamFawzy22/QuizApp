package com.example.quizapp;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.quizapp.models.QuestionsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class QuizFragment extends Fragment implements View.OnClickListener {


    //Declare
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NavController navController;
    private String quiz_id;
    private boolean canAnswer = false;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;
    private String currentUserId;
    private String quizName;

    //UI Elements
    private TextView quizTitle;
    protected View rootView;
    private ImageView closeBtn;
    private TextView questionNumber;
    private ProgressBar questionProgress;
    private TextView questionTime;
    private TextView questionText;
    private Button optionOneBtn;
    private Button optionTwoBtn;
    private Button optionThreeBtn;
    private TextView questionFeedback;
    private Button nextBtn;

    //Firebase Data
    private List<QuestionsModel> allQuestionsList = new ArrayList<>();

    //private long totalQuestionsToAnswer = 10;
    private long totalQuestionsToAnswer = 2;
    private List<QuestionsModel> questionsToAnswer = new ArrayList<>();
    private CountDownTimer countDownTimer;

    public QuizFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        //get User Id
        if (firebaseAuth.getCurrentUser() != null){
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }else {
            //Go back to home page
        }

        //init UI
        initView(view);

        //Initialize
        firebaseFirestore = FirebaseFirestore.getInstance();

        //get quiz id
        quiz_id = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        //get all questions from the quiz
        firebaseFirestore.collection("QuizList").document(quiz_id)
                .collection("Questions").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            allQuestionsList = task.getResult().toObjects(QuestionsModel.class);

                            //getQuestions
                            pickQuestions();

                            //load Ui
                            loadUI();
                        } else {
                            quizTitle.setText("Error Loading Data ...");
                        }
                    }
                });
    }

    private void loadUI() {
        //Quiz data loaded  , load UI
        quizTitle.setText(quizName);
        questionText.setText("Load First Question");

        //Enable Options
        enableOptions();

        //Load first question
        loadQuestion(1);
    }

    private void loadQuestion(int quesNum) {
        //set Question number
        questionNumber.setText(quesNum+"");

        //Load question text
        questionText.setText(questionsToAnswer.get(quesNum-1).getQuestion());

        //Load Options
        optionOneBtn.setText(questionsToAnswer.get(quesNum-1).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(quesNum-1).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(quesNum-1).getOption_c());

        //Question Loaded , set can answer it
        canAnswer = true;
        currentQuestion = quesNum;

        //Start Timer, progress
        startTimer(quesNum);


    }

    private void startTimer(final int questionNumber) {
        //set Timer text
        final Long timeToAnswer = questionsToAnswer.get(questionNumber-1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        //show Timer progressBar
        questionProgress.setVisibility(View.VISIBLE);

        //start countDown
        countDownTimer = new CountDownTimer(timeToAnswer*1000,10){

            @Override
            public void onTick(long millisUntilFinished) {
                //Update time
                questionTime.setText(millisUntilFinished/1000+"");

                //progress in percent
                Long percent = millisUntilFinished/(timeToAnswer*10);
                questionProgress.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                //Time Up, Can not answer questions any more
                canAnswer = false;

                questionFeedback.setText("Time Up! No answer was submitted.");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary,null));
                notAnswered++;
                showNextBtn();
            }
        };
        countDownTimer.start();
    }

    private void enableOptions() {
        //show all option buttons
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        //Enable option buttons
        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        //hide Feedback and NextBtn
        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void pickQuestions() {
        for (int i = 0; i < totalQuestionsToAnswer; i++) {
            int randomNumber = getRandomInt(allQuestionsList.size());
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber);

            Log.e("Question: " + i, questionsToAnswer.get(i).getQuestion());
        }
    }

    private int getRandomInt(int max) {
        return ((int) (Math.random() * (max)));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.quiz_close_btn) {

        } else if (view.getId() == R.id.question_option_one) {
            verifyAnswer(optionOneBtn);
        } else if (view.getId() == R.id.quiz_option_two) {
            verifyAnswer(optionTwoBtn);
        } else if (view.getId() == R.id.quiz_option_three) {
            verifyAnswer(optionThreeBtn);
        } else if (view.getId() == R.id.quiz_next_btn) {
            if (currentQuestion == totalQuestionsToAnswer){
                //Load Results
                submitResults();
            }else {
                currentQuestion++;
                loadQuestion(currentQuestion);
                resetOptions();
            }
        }
    }

    private void submitResults() {
        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put("correct",correctAnswers);
        resultMap.put("wrong",wrongAnswers);
        resultMap.put("unanswered",notAnswered);

        firebaseFirestore.collection("QuizList")
                .document(quiz_id).collection("Results")
                .document(currentUserId).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Go To Results Page
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizId(quiz_id);
                    navController.navigate(action);
                }else {
                    //Show Error
                    quizTitle.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg,null));

        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText,null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectedAnswerBtn) {
        //check answer
        if (canAnswer){
            //set answer btn text color to black
            selectedAnswerBtn.setTextColor(getResources().getColor(R.color.colorDark,null));

            if (questionsToAnswer.get(currentQuestion-1).getAnswer().equals(selectedAnswerBtn.getText())) {

                //Correct answer
                correctAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg,null));

                //Set Feedback Text
                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary,null));
            }else {
                //Wrong answer
                wrongAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg,null));

                //Set Feedback Text
                questionFeedback.setText("Wrong Answer \n \n Correct Answer : " + questionsToAnswer.get(currentQuestion-1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent,null));
            }
            //Set Can answer to false
            canAnswer = false;

            //stop the timer
            countDownTimer.cancel();

            //show next btn
            showNextBtn();

        }
    }

    private void showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer){
            nextBtn.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
    }

    private void initView(View rootView) {
        quizTitle = rootView.findViewById(R.id.quiz_title);
        closeBtn = (ImageView) rootView.findViewById(R.id.quiz_close_btn);
        closeBtn.setOnClickListener(QuizFragment.this);
        quizTitle = (TextView) rootView.findViewById(R.id.quiz_title);
        questionNumber = (TextView) rootView.findViewById(R.id.quiz_question_number);
        questionProgress = (ProgressBar) rootView.findViewById(R.id.quiz_question_progress);
        questionTime = (TextView) rootView.findViewById(R.id.quiz_question_time);
        questionText = (TextView) rootView.findViewById(R.id.quiz_question);
        optionOneBtn = (Button) rootView.findViewById(R.id.question_option_one);
        optionOneBtn.setOnClickListener(QuizFragment.this);
        optionTwoBtn = (Button) rootView.findViewById(R.id.quiz_option_two);
        optionTwoBtn.setOnClickListener(QuizFragment.this);
        optionThreeBtn = (Button) rootView.findViewById(R.id.quiz_option_three);
        optionThreeBtn.setOnClickListener(QuizFragment.this);
        questionFeedback = (TextView) rootView.findViewById(R.id.quiz_question_feedback);
        nextBtn = (Button) rootView.findViewById(R.id.quiz_next_btn);
        nextBtn.setOnClickListener(QuizFragment.this);
    }
}
