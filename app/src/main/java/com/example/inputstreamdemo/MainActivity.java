package com.example.inputstreamdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView receivedText = findViewById(R.id.input);

        Button sendButton = findViewById(R.id.output);
        sendButton.setOnClickListener(view -> {
            receivedText.setText("Piped input stream:\nreceiving...");
            int bufferSize = 1024;

            byte[] bufferSend = new byte[bufferSize];

            PipedOutputStream outputStream = new PipedOutputStream();

            CountDownLatch latch = new CountDownLatch(1);

            ExecutorService sendService = Executors.newSingleThreadExecutor();
            ExecutorService receiveService = Executors.newSingleThreadExecutor();
            sendService.execute(() -> {
                try {
                    latch.await();
                    outputStream.write(bufferSend, 0, bufferSize);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            receiveService.execute(() -> {
                byte[] bufferReceive = new byte[bufferSize];
                PipedInputStream inputStream;
                try {
                    inputStream = new PipedInputStream(outputStream);
                    latch.countDown();
                    int bytesRead = inputStream.read(bufferReceive);
                    runOnUiThread(() -> receivedText.setText(String.format(Locale.ENGLISH, "Piped input stream:\nreceived %d bytes", bytesRead)));
                    while (inputStream.read() != -1) {} // cleanup
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}