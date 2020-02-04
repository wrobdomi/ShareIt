package pl.edu.agh.student.todolist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class ToolBar {
    enum ActivityName {
        SPLASH,
        LOGIN,
        REGISTER,
        GROUPS,
        TASKS;
    }

    private ViewGroup view;
    private ActivityName activityName;
    private TextView toolBarTitle;
    private ImageButton toolBarAddFriend;
    private ImageButton toolBarLogOut;

    ToolBar(ActivityName activityName, ViewGroup view) {
        this.view = view;
        this.activityName = activityName;
        this.toolBarTitle = view.findViewById(R.id.toolBarTitle);
        this.toolBarAddFriend = view.findViewById(R.id.toolBarAdd);
        this.toolBarLogOut = view.findViewById(R.id.toolBarLogOut);

        prepareToolBar();
    }

    private void prepareToolBar() {
        switch (activityName) {
            case LOGIN:
                toolBarTitle.setText(R.string.login_activity_title);

                hideButtonsInToolBar();
                break;

            case REGISTER:
                toolBarTitle.setText(R.string.register_activity_title);

                hideButtonsInToolBar();
                break;

            case GROUPS:
                toolBarTitle.setText(R.string.groups_activity_title);

                showButtonsInToolBar();
                break;

            case TASKS:
                toolBarTitle.setText(R.string.groups_activity_title);
                toolBarLogOut.setImageDrawable(view.getResources().getDrawable(R.drawable.add_user));

                showButtonsInToolBar();
                break;

        }

    }

    public void setToolBarTitle(String title) {
        toolBarTitle.setText(title);
    }

    private void showButtonsInToolBar() {
        toolBarAddFriend.setVisibility(View.VISIBLE);
        toolBarLogOut.setVisibility(View.VISIBLE);
    }

    private void hideButtonsInToolBar() {
        toolBarAddFriend.setVisibility(View.INVISIBLE);
        toolBarLogOut.setVisibility(View.INVISIBLE);
    }
}
