package com.ldh.photopager;

import com.ldh.photopager.PhotoPager.OnPageChangeListener;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

  class MyAdapter extends BaseAdapter {
    @Override
    public int getCount() {
      return 50;
    }

    @Override
    public Object getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView textView = new TextView(MainActivity.this);
      textView.setText("test");
      textView.setTextSize(20);
      textView.setPadding(10, 10, 10, 10);
      return textView;
    }
  }

  private ListView lv_test;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
  }

  private void addView(PhotoPager viewGroup, int len) {
    for (int i = 0; i < len; i++) {
      TextView textView = new TextView(MainActivity.this);
      textView.setText("测试" + i);
      textView.setTextSize(18);
      if (i % 3 == 0) {
        textView.setBackgroundColor(Color.GREEN);
      }
      if (i % 3 == 1) {
        textView.setBackgroundColor(Color.RED);
      }
      if (i % 3 == 2) {
        textView.setBackgroundColor(Color.YELLOW);
      }
      viewGroup.addViewToList(textView);
    }
  }

  private void initView() {
    lv_test = (ListView) findViewById(R.id.lv_test);
    PhotoPager photoPager = new PhotoPager(MainActivity.this);
    photoPager.setLoop(true);
    photoPager.setOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void OnPageChange(int position) {
        System.out.println("position=" + position);
      }
    });
    addView(photoPager, 3);
    photoPager.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 100));
    lv_test.addHeaderView(photoPager);
    lv_test.setAdapter(new MyAdapter());
  }

}
