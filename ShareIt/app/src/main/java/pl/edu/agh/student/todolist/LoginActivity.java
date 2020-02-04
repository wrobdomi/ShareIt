package pl.edu.agh.student.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView signupTextView;
    private Button signinButton;
    private CallbackManager callbackManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersCollectionReference = db.collection("Users");

    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_klaudia);

        ToolBar toolBar = new ToolBar(ToolBar.ActivityName.LOGIN, (ViewGroup) getWindow().getDecorView());

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupTextView = findViewById(R.id.signinTextView);
        signinButton = findViewById(R.id.signupButton);

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        firebaseAuth = FirebaseAuth.getInstance();

        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                handleFacebookAccessToken(accessToken);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
            }
        });


        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
                    loginUserUsingLoginAndPassword(login, password);
                }
                else {
                    sendMessageToUser("Please enter email and password");
                }
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(mainIntent);
                LoginActivity.this.finish();
            }
        });


    }

    private void loginUserUsingLoginAndPassword(final String email, final String password) {
        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Validating data! Please wait!");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                 firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    assert user != null;
                                    final String currentUserId = user.getUid();

                                    Log.d("FIREBASELog", "Current user Id " + currentUserId);

                                    usersCollectionReference
                                            .whereEqualTo("userId", currentUserId)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                    if (!queryDocumentSnapshots.isEmpty()) {
                                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                            UserDetails userDetails = UserDetails.getInstance();
                                                            userDetails.setUsername(snapshot.getString("username"));
                                                            userDetails.setUserId(snapshot.getString("userId"));
                                                        }

                                                        dialog.dismiss();

                                                        Intent mainIntent = new Intent(LoginActivity.this, GroupsActivity.class);
                                                        LoginActivity.this.startActivity(mainIntent);
                                                        LoginActivity.this.finish();
                                                    }
                                                }
                                            });
                                } else {
                                    dialog.dismiss();
                                    sendMessageToUser("Invalid username or password");
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                sendMessageToUser("Invalid username or password");
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
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(final AccessToken accessToken) {
        Log.d("FIREBASE", "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("FIREBASE", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            assert user != null;
                            final String currentUserId = user.getUid();

                            Log.d("FIREBASELog", "Current user Id " + currentUserId);

                            GraphRequest request = GraphRequest.newMeRequest(
                                    accessToken, new GraphRequest.GraphJSONObjectCallback() {

                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            try {
                                                String name = object.getString("name");

                                                UserDetails userDetails = UserDetails.getInstance();
                                                userDetails.setUsername(name);
                                                userDetails.setUserId(currentUserId);

                                                Intent mainIntent = new Intent(LoginActivity.this, GroupsActivity.class);
                                                LoginActivity.this.startActivity(mainIntent);
                                                LoginActivity.this.finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });

                            Bundle parameters = new Bundle();
                            parameters.putString("usetData", "name");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }
                        else {
                            Log.w("FIREBASE", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();

        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("Trying to reconnect! Please wait!");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    UserDetails userDetails = UserDetails.getInstance();
                    userDetails.setUsername(currentUser.getDisplayName());
                    userDetails.setUserId(currentUser.getUid());

                    Log.d("XYZXYZ", currentUser.getDisplayName());

                    Intent mainIntent = new Intent(LoginActivity.this, GroupsActivity.class);
                    LoginActivity.this.startActivity(mainIntent);
                    LoginActivity.this.finish();
                }

            }
        }).start();

        dialog.dismiss();
    }
}
