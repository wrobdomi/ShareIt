package pl.edu.agh.student.todolist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText, repeatPasswordEditText;
    private TextView errorLabel, signinTextView;
    private Button signupButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersCollectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_klaudia);

        ToolBar toolBar = new ToolBar(ToolBar.ActivityName.REGISTER, (ViewGroup) getWindow().getDecorView());

        // errorLabel = findViewById(R.id.errorLabel);

        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);

        signinTextView = findViewById(R.id.signinTextView);
        signupButton = findViewById(R.id.signupButton);

        firebaseAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email =  emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String repassword = repeatPasswordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();

                if (checkIfAllFieldsAreFilled()) {
                    if(password.equals(repassword)) {
                        createUserInDB(username, email, password);
                    }
                    else {
                        sendMessageToUser("Passwords are not equal");
                    }
                }
                else {
                    sendMessageToUser("All fields are required to be filled");
                }

            }
        });

        signinTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                RegisterActivity.this.startActivity(mainIntent);
                RegisterActivity.this.finish();
            }
        });

    }

    private void createUserInDB(final String username, final String email, final String password) {
        final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setMessage("Validating data! Please wait!");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        // save username and id to collection Users
                                        currentUser = firebaseAuth.getCurrentUser();
                                        final String currentUserId = currentUser.getUid();

                                        Map<String, String> userObject = new HashMap<>();
                                        userObject.put("userId", currentUserId);
                                        userObject.put("username", username);

                                        usersCollectionReference.add(userObject)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        documentReference.get()
                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (Objects.requireNonNull(task).getResult().exists()) {
                                                                            String name = task.getResult().getString("username");

                                                                            UserDetails userDetails = UserDetails.getInstance();
                                                                            userDetails.setUsername(name);
                                                                            userDetails.setUserId(currentUserId);

                                                                            dialog.dismiss();

                                                                            Intent mainIntent = new Intent(RegisterActivity.this, GroupsActivity.class);
                                                                            RegisterActivity.this.startActivity(mainIntent);
                                                                            RegisterActivity.this.finish();
                                                                        } else {
                                                                            dialog.dismiss();
                                                                            sendMessageToUser("Unexpected error");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        sendMessageToUser("Unexpected error");
                                                    }
                                                });

                                    } else {
                                        dialog.dismiss();
                                        sendMessageToUser("Unexpected error");
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    sendMessageToUser("Unexpected error");
                                }
                            });
            }

        }).start();
    }

    public void sendMessageToUser(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkIfAllFieldsAreFilled() {
        for (String fieldText : new String[]{emailEditText.getText().toString(), usernameEditText.getText().toString(), passwordEditText.getText().toString(), repeatPasswordEditText.getText().toString()}) {
            if (isEmpty(fieldText)) {

                return false;
            }
        }

        return true;
    }
}
