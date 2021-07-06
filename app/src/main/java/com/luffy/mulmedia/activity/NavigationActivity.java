package com.luffy.mulmedia.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.luffy.mulmedia.R;
import com.luffy.mulmedia.utils.FileUtils;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity";
    RecyclerView mLessonList;
    List<String> mLessonPaths;

    Button mSelectFileBtn;
    EditText mSelectFileEt;

    private Uri selectUri;
    private ParcelFileDescriptor descriptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        mLessonList = findViewById(R.id.LessonListView);
        mSelectFileBtn = findViewById(R.id.btn_select_file);
        mSelectFileEt = findViewById(R.id.et_select_file_uri);
        mSelectFileBtn.setOnClickListener((view) -> {
            startFileBrowser();
        });
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
        intent.putExtra("uri",selectUri);
        startActivity(intent);
    }

    private void startFileBrowser(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Uri uri = data.getData();
            Log.d(TAG,"onActivityResult " + uri);
            selectUri = uri;
            mSelectFileEt.setText(uri.toString());
        }
    }
}
