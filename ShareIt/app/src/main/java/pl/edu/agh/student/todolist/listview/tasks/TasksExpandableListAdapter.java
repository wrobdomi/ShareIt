package pl.edu.agh.student.todolist.listview.tasks;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import pl.edu.agh.student.todolist.R;

public class TasksExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> expandableListTitle;
    private LinkedHashMap<String, List<Task>> expandableListDetail;

    public TasksExpandableListAdapter(Context context, List<String> expandableListTitle, LinkedHashMap<String, List<Task>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition).name;
        final Date expandedListDate = this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition).deadline;

        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (listPosition == 0) {
            convertView = layoutInflater.inflate(R.layout.tasks_row_item, null);

            TextView textViewTimeLeft = convertView.findViewById(R.id.textViewTimeLeft);
            ImageView imageViewTimeLeft = convertView.findViewById(R.id.clockImage);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Date currentDate = new Date();

                long seconds = Math.abs(currentDate.getTime() - expandedListDate.getTime()) / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (hours < 1) {
                    textViewTimeLeft.setTextColor(Color.parseColor("#B22222"));
                    String text = minutes + "min";
                    textViewTimeLeft.setText(text);
                }
                if (minutes > 60) {
                    textViewTimeLeft.setTextColor(Color.parseColor("#B22222"));
                    String text = hours + "h";
                    textViewTimeLeft.setText(text);
                }
                if (hours > 24) {
                    textViewTimeLeft.setTextColor(Color.parseColor("#784A24"));

                    String text = days + " days";
                    textViewTimeLeft.setText(text);
                }
                if (days >= 7) {
                    textViewTimeLeft.setTextColor(Color.parseColor("#784A24"));

                    String text = new SimpleDateFormat("dd-MM-yyyy").format(expandedListDate);
                    textViewTimeLeft.setText(text);
                }
            }

        } else {
            convertView = layoutInflater.inflate(R.layout.tasks_row_item2, null);
        }

        TextView expandedListTextView = convertView.findViewById(R.id.txtName);
        expandedListTextView.setText(expandedListText);

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        if (this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).get(expandedListPosition).checked) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition)).size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.task_group_item, null);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);

        notifyDataSetChanged();

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }


}