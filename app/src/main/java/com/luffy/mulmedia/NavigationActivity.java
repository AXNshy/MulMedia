package com.luffy.mulmedia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {

    RecyclerView mLessonList;
    List<String> mLessonPaths;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mLessonList = findViewById(R.id.LessonListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLessonPaths = Arrays.asList(getResources().getStringArray(R.array.media_lesson_path_collection));
        mLessonList.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text,parent,false);

                return new RecyclerView.ViewHolder(view){};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                TextView view = (TextView) holder.itemView;
                view.setText(mLessonPaths.get(position));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigate(position);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mLessonPaths.size();
            }
        });
        mLessonList.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
    }

    private void navigate(int position){
        String path = mLessonPaths.get(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("xzq://navigate"+path));
        startActivity(intent);
    }
}
