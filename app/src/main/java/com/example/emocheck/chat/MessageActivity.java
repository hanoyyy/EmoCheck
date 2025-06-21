package com.example.emocheck.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emocheck.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private ArrayList<Message> messages;
    private RecyclerView recyclerView;
    private recyclerAdapter adapter;
    private ImageButton sendButton;
    private EditText msgInput;
    private getRequest request;

    // List of off-topic keywords or phrases
    private List<String> offTopicKeywords = Arrays.asList(
            "sky", "color", "blue", "space", "weather",
            "animals", "planets", "universe", "physics"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize request
        request = new getRequest(this);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Initialize messages and adapter
        messages = new ArrayList<>();
        adapter = new recyclerAdapter(messages);
        recyclerView.setAdapter(adapter);

        // Initialize UI elements
        sendButton = findViewById(R.id.msgButton);
        msgInput = findViewById(R.id.msgInput);

        // Set onClickListener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msgInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    if (isOffTopic(message)) {
                        handleOffTopicMessage(message);
                    } else {
                        addMessageToChat(true, message);
                        msgInput.setText(""); // Clear input
                        getReply(message);
                    }
                } else {
                    Toast.makeText(MessageActivity.this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Checks if the user's input is off-topic based on defined keywords.
     *
     * @param message The user's input message.
     * @return True if the message is off-topic, otherwise false.
     */
    private boolean isOffTopic(String message) {
        for (String keyword : offTopicKeywords) {
            if (message.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles off-topic input with a specific response.
     *
     * @param userMessage The off-topic message from the user.
     */
    private void handleOffTopicMessage(String userMessage) {
        addMessageToChat(true, userMessage); // Add user's message
        addMessageToChat(false, "I'm here to help with mental health and emotions. Let's focus on that!"); // Bot response
        msgInput.setText("");
    }

    /**
     * Adds a message to the chat and updates the RecyclerView.
     *
     * @param isUserMessage True if the message is from the user, false if it's from the bot.
     * @param content       The message content.
     */
    private void addMessageToChat(boolean isUserMessage, String content) {
        messages.add(new Message(isUserMessage, content));
        int newPosition = messages.size() - 1;
        adapter.notifyItemInserted(newPosition);
        recyclerView.scrollToPosition(newPosition);
    }

    /**
     * Sends the user's input to the API and processes the response.
     *
     * @param userMessage The user's input message.
     */
    private void getReply(String userMessage) {
        request.getResponse(userMessage, new getRequest.VolleyResponseListener() {
            @Override
            public void onError(String errorMessage) {
                Log.e("API ERROR", errorMessage);
                addMessageToChat(false, "Sorry, I couldn't process your request. Please try again later.");
            }

            @Override
            public void onResponse(String reply) {
                addMessageToChat(false, reply);
            }
        });
    }
}
