package pl.edu.agh.student.todolist.listview.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import pl.edu.agh.student.todolist.R;

public class GroupsListViewAdapter extends ArrayAdapter {

    private ArrayList<Group> listOfGroups;
    private Context mContext;

    public GroupsListViewAdapter(ArrayList<Group> data, Context context) {
        super(context, R.layout.groups_row_item, data);

        this.listOfGroups = data;
        this.mContext = context;
    }

    private static class ViewHolder {
        TextView txtName;
        ImageButton imageButton;
    }

    @Override
    public int getCount() {
        return listOfGroups.size();
    }

    @Override
    public Group getItem(int position) {
        return listOfGroups.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_row_item, parent, false);
            viewHolder.txtName = convertView.findViewById(R.id.group_name);
            viewHolder.imageButton = convertView.findViewById(R.id.deleteGroup);

            result = convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        final Group item = getItem(position);

        assert item != null;
        viewHolder.txtName.setText(item.getName());

        if (item.isFavorite()) {
            viewHolder.imageButton.setImageResource(R.drawable.star_yellow);
        } else {
            viewHolder.imageButton.setImageResource(R.drawable.star_white);
        }

        viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item.setFavorite(!item.isFavorite());

                if (item.isFavorite()) {
                    if (position != 0) {
                        listOfGroups.remove(position);
                        listOfGroups.add(0, item);
                    }
                } else {
                    for (int i = 0; i < listOfGroups.size(); i++) {
                        if (!listOfGroups.get(i).isFavorite() && i != position) {
                            listOfGroups.remove(position);
                            listOfGroups.add(i - 1, item);

                            break;
                        }
                    }
                }

                notifyDataSetChanged();
            }
        });

        return result;
    }
}