package com.example.demoapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;
import java.net.URL;

public class AsyncTaskActivity extends AppCompatActivity {

    private Handler handler;
    private TextView tv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);
        tv = findViewById(R.id.textView);
        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.what == Task.TASK_ID) {
                    tv.setText("Task finished");
                }
                return false;
            }
        });
    }

    private static class Task implements Runnable {

        private static final int TASK_ID = 1;
        private WeakReference<Handler> ref;

        Task(@NonNull Handler handler) {
            ref = new WeakReference<>(handler);
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (ref.get() != null) {
                ref.get().sendEmptyMessage(TASK_ID);
            }
        }
    }

    private void basicBackgroundTask() {
        new Thread(new Task(handler)).start();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.threadTask:
                tv.setText(R.string.started);
                basicBackgroundTask();
                break;
            case R.id.asyncTask:
                new SimpleAsyncTask(tv).execute();
                break;
            case R.id.next:
                startActivity(new Intent(this, LoaderActivity.class));
                break;
        }
    }

    private static class SimpleAsyncTask extends AsyncTask<URL, Integer, Long> {
        private WeakReference<TextView> ref;

        SimpleAsyncTask(TextView tv) {
            ref = new WeakReference<>(tv);
        }

        @Override
        protected void onPreExecute() {
            if (ref.get() != null) {
                ref.get().setText("SimpleAsyncTask starting ");
            }
        }

        protected Long doInBackground(URL... urls) {
            int totalSize = 0;
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(250);
                    totalSize++;
                    publishProgress(totalSize);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCancelled()) { break; }
            }
            return totalSize * 1l;
        }

        protected void onProgressUpdate(Integer... progress) {
            if (ref.get() != null) {
                ref.get().setText("Progress " + progress[0]);
            }
        }

        protected void onPostExecute(Long result) {
            if (ref.get() != null) {
                ref.get().setText("Task Completed with size  " + result);
            }
        }
    }
}
