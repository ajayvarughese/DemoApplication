package com.example.demoapplication;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class SimpleLoader extends AsyncTaskLoader<String> {

    public SimpleLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public String loadInBackground() {
        int totalSize = 0;
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(250);
                totalSize++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "Loader finished " + totalSize;
    }
}
