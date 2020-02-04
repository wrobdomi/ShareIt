package pl.edu.agh.student.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.edu.agh.student.todolist.listview.groups.Group;
import pl.edu.agh.student.todolist.listview.groups.GroupsListViewAdapter;

public class GroupsActivity extends AppCompatActivity {
    private ListView groupsListView;
    private ArrayList<Group> listOfGroups;
    private GroupsListViewAdapter groupsListViewAdapter;

    private EditText groupNameEditText;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsCollectionReference = db.collection("Groups");
    private CollectionReference groupsMembersCollectionReference = db.collection("GroupsMembers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups_activity);

        ToolBar toolBar = new ToolBar(ToolBar.ActivityName.GROUPS, (ViewGroup) getWindow().getDecorView());

        ImageButton tollBarAddButton = findViewById(R.id.toolBarAdd);
        tollBarAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopUpAddGroup();
            }
        });

        ImageButton toolBarLogOutButton = findViewById(R.id.toolBarLogOut);
        toolBarLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                firebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Intent mainIntent = new Intent(GroupsActivity.this, LoginActivity.class);
                GroupsActivity.this.startActivity(mainIntent);
                GroupsActivity.this.finish();
            }
        });

        groupsListView = findViewById(R.id.listView);

        listOfGroups = getListOfGroups();

        groupsListViewAdapter = new GroupsListViewAdapter(listOfGroups, getApplicationContext());
        groupsListView.setAdapter(groupsListViewAdapter);

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listOfGroups = getListOfGroups();
                groupsListViewAdapter.notifyDataSetChanged();

                pullToRefresh.setRefreshing(false);
            }
        });

        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group group = listOfGroups.get(position);

                Intent mainIntent = new Intent(GroupsActivity.this, TasksActivity.class);
                Log.d("GROUP", group.getName());
                mainIntent.putExtra("groupName", group.getName());
                GroupsActivity.this.startActivity(mainIntent);
            }
        });
    }

    private ArrayList<Group> getListOfGroups() {
        final ArrayList<Group> listOfGroups = new ArrayList();

        groupsMembersCollectionReference
                .whereEqualTo("userId", UserDetails.getInstance().getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GroupsMembers gm = document.toObject(GroupsMembers.class);
                                Group group = new Group(gm.getUserId(), gm.getGroupName());
                                group.setFavorite(gm.isFavorite());
                                Log.d("FIREBASE", "This document from firebase contains:");
                                Log.d("FIREBASE", group.getUserId());
                                Log.d("FIREBASE", group.getName());
                                listOfGroups.add(group);
                            }
                            groupsListViewAdapter = new GroupsListViewAdapter(listOfGroups, getApplicationContext());
                            groupsListView.setAdapter(groupsListViewAdapter);
                        } else {
                            Toast.makeText(GroupsActivity.this, "Unable to fetch your groups", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        return listOfGroups;
    }

    /**
     * This method creates pop up and click listener
     * for submitting the pop up
     * <p>
     * When user submit pop up, there are two saves in db:
     * <p>
     * First save adds group to grups collection
     * <p>
     * Second save adds record in GroupsMembers (user group mapping)
     */
    private void createPopUpAddGroup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GroupsActivity.this);
        alertDialogBuilder.setTitle("Create new group!");
        alertDialogBuilder.setCancelable(false);

        LayoutInflater layoutInflater = LayoutInflater.from(GroupsActivity.this);
        final View popupInputDialogView = layoutInflater.inflate(R.layout.popup_create_group, null);

        groupNameEditText = popupInputDialogView.findViewById(R.id.group_name);
        Button saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        Button cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);

        alertDialogBuilder.setView(popupInputDialogView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        saveUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupName = groupNameEditText.getText().toString();

                if (groupName.length() >= 3) {
                    Group newGroup = new Group(UserDetails.getInstance().getUserId(), groupName);
                    groupsCollectionReference.add(newGroup)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    GroupsMembers gm = new GroupsMembers(
                                            documentReference.getId(),
                                            UserDetails.getInstance().getUserId(),
                                            groupName,
                                            false
                                    );

                                    groupsMembersCollectionReference.add(gm)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(GroupsActivity.this, "New group added", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(GroupsActivity.this, "Unable to add the group", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GroupsActivity.this, "Unable to add the group", Toast.LENGTH_LONG).show();
                                }
                            });


                    listOfGroups.add(newGroup);
                    groupsListViewAdapter.notifyDataSetChanged();

                    alertDialog.cancel();
                } else {
                    Toast.makeText(GroupsActivity.this, "Group name should have at least 3 characters", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }


    /**
     * The method updates logged users group with a given name
     * by setting favorite field to true in database.
     * <p>
     * It also updates listOfGroups field and calls
     * notifyDataSetChanged()
     *
     * @param groupName name of the group to be updated
     */
    public void addGroupToFavorite(final String groupName) {
        groupsMembersCollectionReference
                .whereEqualTo("userId", UserDetails.getInstance().getUserId())
                .whereEqualTo("groupName", groupName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<Object, Boolean> map = new HashMap<>();
                                map.put("favorite", true);
                                groupsMembersCollectionReference.document(document.getId()).set(map, SetOptions.merge());
                                Toast.makeText(GroupsActivity.this, "Group addded to favorites !", Toast.LENGTH_LONG).show();


                                for (Group g : listOfGroups) {
                                    if (g.getName().equals(groupName)) {
                                        g.setFavorite(true);
                                    }
                                    break;
                                }

                                groupsListViewAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("FIREBASE", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    /**
     * The method updates logged users group with a given name
     * by setting favorite field to false in database.
     * <p>
     * It also updates listOfGroups field and calls
     * notifyDataSetChanged()
     *
     * @param groupName name of the group to be updated
     */
    private void removeGroupFromFavorite(final String groupName) {
        groupsMembersCollectionReference
                .whereEqualTo("userId", UserDetails.getInstance().getUserId())
                .whereEqualTo("groupName", groupName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<Object, Boolean> map = new HashMap<>();
                                map.put("favorite", false);
                                groupsMembersCollectionReference.document(document.getId()).set(map, SetOptions.merge());
                                Toast.makeText(GroupsActivity.this, "Group removed from favorites !", Toast.LENGTH_LONG).show();

                                for (Group g : listOfGroups) {
                                    if (g.getName().equals(groupName)) {
                                        g.setFavorite(false);
                                    }
                                    break;
                                }

                                groupsListViewAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("FIREBASE", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
