package com.example.quizapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.example.quizapp.adapters.QuizListAdapter;
import com.example.quizapp.models.QuizListModel;
import com.example.quizapp.models.QuizListViewModel;
import com.google.android.gms.common.data.DataBufferObserver;

import java.util.List;

public class ListFragment extends Fragment implements QuizListAdapter.OnQuizListItemClicked {

    private QuizListViewModel quizListViewModel;
    private RecyclerView recyclerView;

    private ProgressBar listProgress;
    private QuizListAdapter adapter;

    private Animation fadeInAnim;
    private Animation fadeOutAnim;

    private NavController navController;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        recyclerView = view.findViewById(R.id.recycler_view);
        listProgress = view.findViewById(R.id.list_progress);
        adapter = new QuizListAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        fadeInAnim = AnimationUtils.loadAnimation(getContext(),R.anim.fade_in);
        fadeOutAnim = AnimationUtils.loadAnimation(getContext(),R.anim.fade_out);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                //load recycler view

                recyclerView.startAnimation(fadeInAnim);
                listProgress.setAnimation(fadeOutAnim);

                adapter.setQuizListModels(quizListModels);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        ListFragmentDirections.ActionListFragmentToDetailsFragment action = ListFragmentDirections.actionListFragmentToDetailsFragment();
        action.setPosition(position);


        navController.navigate(action);
    }
}
