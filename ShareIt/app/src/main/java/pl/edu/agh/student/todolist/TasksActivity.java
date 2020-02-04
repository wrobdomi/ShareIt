package pl.edu.agh.student.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.student.todolist.listview.tasks.TasksExpandableListAdapter;
import pl.edu.agh.student.todolist.listview.tasks.Task;

public class TasksActivity extends AppCompatActivity {
    private ExpandableListView expandableListView;
    private TasksExpandableListAdapter expandableListAdapter;

    private LinkedHashMap<String, List<Task>> expandableListDetail;
    private List<String> expandableListTitle;

    private String chosenGroupName;
    private String chosenGroupId;
    private ArrayList<Task> usersTasksToDo = new ArrayList<>();
    private ArrayList<Task> usersTasksDone = new ArrayList<>();
    private ArrayList<Task> usersTasksMissed = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference tasksCollectionReference = db.collection("Tasks");
    private CollectionReference groupsCollectionReference = db.collection("Groups");
    private CollectionReference usersCollectionReference = db.collection("Users");
    private CollectionReference groupsMembersCollectionReference = db.collection("GroupsMembers");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_activity);

        this.chosenGroupName = getIntent().getStringExtra("groupName");

        ToolBar toolBar = new ToolBar(ToolBar.ActivityName.TASKS, (ViewGroup) getWindow().getDecorView());
        toolBar.setToolBarTitle(chosenGroupName);

        ImageButton tollBarAddButton = findViewById(R.id.toolBarAdd);
        tollBarAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopUpAddGroup();
            }
        });

        ImageButton toolBarSettingsButton = findViewById(R.id.toolBarLogOut);
        toolBarSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPopUpAddUser();
            }
        });

        groupsCollectionReference
                .whereEqualTo("name", chosenGroupName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot documentGroup : task.getResult()) {
                                chosenGroupId = documentGroup.getId();
                            }

                            tasksCollectionReference
                                    .whereEqualTo("userId", UserDetails.getInstance().getUserId())
                                    .whereEqualTo("groupId", chosenGroupId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Task t = document.toObject(Task.class);
                                                    usersTasksToDo.add(t);
                                                }

                                                expandableListDetail = new LinkedHashMap<>();
                                                expandableListDetail.put(getResources().getString(R.string.tasks_activity_todo), usersTasksToDo);
                                                expandableListDetail.put(getResources().getString(R.string.tasks_activity_done), usersTasksDone);
                                                expandableListDetail.put(getResources().getString(R.string.tasks_activity_missed), usersTasksMissed);

                                                expandableListView = findViewById(R.id.listView);

                                                expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                                                expandableListAdapter = new TasksExpandableListAdapter(TasksActivity.this, expandableListTitle, expandableListDetail);
                                                expandableListView.setAdapter(expandableListAdapter);

                                                expandableListView.expandGroup(0);

                                                expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                                                    @Override
                                                    public void onGroupExpand(int groupPosition) {

                                                        expandableListAdapter.notifyDataSetChanged();
                                                    }
                                                });

                                                expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                                                    @Override
                                                    public void onGroupCollapse(int groupPosition) {

                                                        expandableListAdapter.notifyDataSetChanged();
                                                    }
                                                });

                                                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                                                    @Override
                                                    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                                        if (groupPosition == 0) {
                                                            setTaskToDone(expandableListDetail.get("Current").get(childPosition).name);

                                                            expandableListDetail.get("Current").get(childPosition).checked = true;
                                                            expandableListDetail.get("Done").add(0, expandableListDetail.get("Current").get(childPosition));
                                                            expandableListDetail.get("Current").remove(childPosition);
                                                        }

                                                        expandableListAdapter.notifyDataSetChanged();

                                                        return false;
                                                    }
                                                });

                                                sortTaskBetweenLists();
                                            } else {
                                                Toast.makeText(TasksActivity.this, "Unable to fetch your tasks", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(TasksActivity.this, "Group does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sortTaskBetweenLists() {
        Date currentDate = new Date();

        for (int i = 0; i < usersTasksToDo.size(); i++) {
            Task task = usersTasksToDo.get(i);

            if (task.checked) {
                usersTasksDone.add(task);
                usersTasksToDo.remove(i);

                i--;
            } else if (currentDate.compareTo(task.deadline) >= 0) {
                usersTasksMissed.add(task);
                usersTasksToDo.remove(i);

                i--;
            }

        }

        expandableListAdapter.notifyDataSetChanged();
    }

    /**
     * gets list of tasks in a group for the logged user
     * the method is never used but may be useful
     * tasks are fetched in onCreate method when the activity starts
     * but it does not use this method
     *
     * @param groupId id of the group
     * @return list of tasks in the group for the logged user
     */
    private ArrayList<Task> getListOfUserTasks(String groupId) {
        final ArrayList<Task> listOfTasks = new ArrayList();

        tasksCollectionReference
                .whereEqualTo("userId", UserDetails.getInstance().getUserId())
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Task t = document.toObject(Task.class);
                                listOfTasks.add(t);
                            }

                        } else {
                            Toast.makeText(TasksActivity.this, "Unable to fetch your tasks", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return listOfTasks;
    }


    /**
     * the method adds task to the user in the group
     * must be called out of onCreate
     * updating UI is needed
     *
     * @param username login provided during sign up
     * @param taskName name of the task
     * @param deadline deadline
     */
    private void addTask(String username, final String taskName, final Date deadline) {
        usersCollectionReference
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String chosenUserId = (String) document.get("userId");

                                final Task newTask = new Task(
                                        chosenUserId,
                                        chosenGroupId,
                                        taskName,
                                        deadline,
                                        false);

                                tasksCollectionReference.add(newTask)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                usersTasksToDo.add(newTask);
                                                sortTaskBetweenLists();

                                                Toast.makeText(TasksActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(TasksActivity.this, "Unable to add the task", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(TasksActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /**
     * sets task checked attribute to true
     * adding UI update is needed
     *
     * @param taskName name of the task
     */
    private void setTaskToDone(final String taskName) {

        tasksCollectionReference
                .whereEqualTo("name", taskName)
                .whereEqualTo("groupId", chosenGroupId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<Object, Boolean> map = new HashMap<>();
                                map.put("checked", true);
                                tasksCollectionReference.document(document.getId()).set(map, SetOptions.merge());
                                Toast.makeText(TasksActivity.this, "Task done!", Toast.LENGTH_SHORT).show();

                                for (Task t : usersTasksDone) {
                                    if (t.name.equals(taskName)) {
                                        t.checked = true;
                                    }
                                    break;
                                }

                            }
                        } else {
                            Log.d("FIREBASE", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    /**
     * The method adds user to group, after you call the method,
     * the group should be visible on the user account.
     * <p>
     * The method first send request to get group id
     * for a given group name,
     * <p>
     * then it send request
     * to get id for a given user login
     *
     * @param groupName name of the group
     * @param userLogin user login (you set it when you register)
     */
    private void addUserToGroup(final String groupName, final String userLogin) {
        groupsCollectionReference
                .whereEqualTo("name", groupName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentGroup : task.getResult()) {
                                final String groupId = documentGroup.getId();

                                usersCollectionReference
                                        .whereEqualTo("username", userLogin)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {


                                                        GroupsMembers gm = new GroupsMembers(
                                                                groupId,
                                                                (String) document.get("userId"),
                                                                groupName,
                                                                false
                                                        );

                                                        groupsMembersCollectionReference.add(gm)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        Toast.makeText(TasksActivity.this, "User added to the group", Toast.LENGTH_LONG).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(TasksActivity.this, "Unable to add user to the group", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });


                                                    }
                                                } else {
                                                    Toast.makeText(TasksActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(TasksActivity.this, "Group does not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }

    private void createPopUpAddGroup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TasksActivity.this);
        alertDialogBuilder.setTitle("Create new group!");
        alertDialogBuilder.setCancelable(false);

        LayoutInflater layoutInflater = LayoutInflater.from(TasksActivity.this);
        final View popupInputDialogView = layoutInflater.inflate(R.layout.popup_add_task, null);

        final EditText taskEditText = popupInputDialogView.findViewById(R.id.taskEditText);
        final Spinner userSpinner = popupInputDialogView.findViewById(R.id.userSpinner);
        final EditText dateTimeEditText = popupInputDialogView.findViewById(R.id.dateTimeEditText);
        Button saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        Button cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);

        //TODO: Get usernames from group and assing then to usernamesArray
        String[] usernamesArray = new String[]{UserDetails.getInstance().getUsername()};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usernamesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        userSpinner.setAdapter(adapter);

        userSpinner.setSelection(0);

        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, final int selectedYear, final int selectedMonth, final int selectedDay) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(TasksActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        dateTimeEditText.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear + " " + selectedHour + ":" + selectedMinute);
                    }
                }, 23, 59, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        };

        dateTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(TasksActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        alertDialogBuilder.setView(popupInputDialogView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        saveUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date currentDate = new Date();

                try {
                    if (!dateTimeEditText.getText().equals("") && !taskEditText.getText().equals("") && currentDate.compareTo(new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(dateTimeEditText.getText().toString())) < 0) {
                        try {
                            addTask(userSpinner.getSelectedItem().toString(), taskEditText.getText().toString(), new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(dateTimeEditText.getText().toString()));
                        } catch (ParseException e) {
                            Toast.makeText(TasksActivity.this, "Unable to add new task!", Toast.LENGTH_SHORT).show();
                        } finally {
                            alertDialog.cancel();
                        }
                    }
                } catch (ParseException e) {
                    Toast.makeText(TasksActivity.this, "Unable to add new task!", Toast.LENGTH_SHORT).show();
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


    private void createPopUpAddUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TasksActivity.this);
        alertDialogBuilder.setTitle("Add new user!");
        alertDialogBuilder.setCancelable(false);

        LayoutInflater layoutInflater = LayoutInflater.from(TasksActivity.this);
        final View popupInputDialogView = layoutInflater.inflate(R.layout.popup_add_user, null);

        final EditText usernameEditText = popupInputDialogView.findViewById(R.id.usernameEditText);
        Button saveUserDataButton = popupInputDialogView.findViewById(R.id.button_save_user_data);
        Button cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);

        alertDialogBuilder.setView(popupInputDialogView);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        saveUserDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!usernameEditText.getText().toString().equals("")) {
                    addUserToGroup(chosenGroupName, usernameEditText.getText().toString());

                    alertDialog.cancel();
                } else {
                    Toast.makeText(TasksActivity.this, "Username filed can't be empty!", Toast.LENGTH_SHORT).show();
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


}
