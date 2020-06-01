package com.example.quizapp.models;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import androidx.annotation.NonNull;

public class FirebaseReposoitory {

    private OnFirestoreTaskComplete onFirestoreTaskComplete;

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    //load public quiz only
    Query quizRef = firebaseFirestore.collection("QuizList").whereEqualTo("visibility","public");

    public FirebaseReposoitory(OnFirestoreTaskComplete onFirestoreTaskComplete) {
        this.onFirestoreTaskComplete = onFirestoreTaskComplete;
    }

    public void getQuizData(){
        quizRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    onFirestoreTaskComplete.quizListDataAdded(task.getResult().toObjects(QuizListModel.class));
                }else {
                    onFirestoreTaskComplete.onError(task.getException());
                }
            }
        });
    }

    public interface OnFirestoreTaskComplete{
        void quizListDataAdded(List<QuizListModel> quizListModelsList);
        void onError(Exception e);
    }
}
