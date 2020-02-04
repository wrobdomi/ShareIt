package pl.edu.agh.student.todolist;

import android.app.Application;

public class UserDetails extends Application {
    private String username;
    private String userId;

    private static UserDetails userDetailsInstance;

    // singleton design pattern
    public static UserDetails getInstance(){
        if(userDetailsInstance == null)
            userDetailsInstance = new UserDetails();
        return userDetailsInstance;
    }

    public UserDetails(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
