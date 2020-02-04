package pl.edu.agh.student.todolist.listview.tasks;

import java.util.Date;

public class Task {

    public String userId;
    public String groupId;
    public String name;
    public Date deadline;
    public boolean checked;

    public Task(String userId, String groupId, String name, Date deadline, boolean checked) {
        this.userId = userId;
        this.groupId = groupId;
        this.name = name;
        this.deadline = deadline;
        this.checked = checked;
    }

    public Task() {
    }
}
