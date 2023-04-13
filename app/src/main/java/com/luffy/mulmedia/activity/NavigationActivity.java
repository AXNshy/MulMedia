package com.luffy.mulmedia.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luffy.mulmedia.R;
import com.luffyxu.mulmedia.model.NavItem;
import com.luffyxu.mulmedia.ui.adapter.NavItemAdapter;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity";
    RecyclerView mLessonList;
    List<NavItem> mLessonPaths;

    NavItemAdapter adapter;

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
            if(checkPermission()) {
                startFileBrowser();
            }else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission(){
        int code = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(code == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"权限申请成功");
            }else {
                Log.d(TAG,"权限申请失败");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLessonPaths = createNavList();
        adapter = new NavItemAdapter();
        adapter.setData(mLessonPaths);
        adapter.setItemClickListener(new Function2<View, Integer, Unit>() {
            @Override
            public Unit invoke(View view, Integer integer) {
                navigate(integer);
                return null;
            }
        });
        mLessonList.setAdapter(adapter);
        mLessonList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
                super.getItemOffsets(outRect, itemPosition, parent);
                outRect.left = 20;
                outRect.right = 20;
                outRect.bottom = 20;
            }
        });

        mLessonList.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
    }

    private List<NavItem> createNavList(){
        String[] pathArrays = getResources().getStringArray(R.array.media_lesson_path_collection);
        String[] titleArrays = getResources().getStringArray(R.array.media_lesson_title_collection);
        List<NavItem> data = new ArrayList<>();
        for(int i=0;i< pathArrays.length;i++){
            data.add(new NavItem(titleArrays[i],pathArrays[i]));
        }
        return data;
    }

    private void navigate(int position){
//        if(selectUri == null){
//            Toast.makeText(this, "please select a video first", Toast.LENGTH_SHORT).show();
//            return;
//        }
        String path = mLessonPaths.get(position).getPath();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("xzq://navigate"+path));
        intent.putExtra("uri",selectUri);
        startActivity(intent);
    }

    private void startFileBrowser(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
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
