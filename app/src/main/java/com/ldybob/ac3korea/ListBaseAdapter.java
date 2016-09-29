package com.ldybob.ac3korea;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListBaseAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ListItem> bbsList;
    private ViewHolder holder = null;

    public ListBaseAdapter(Context context, ArrayList<ListItem> list) {
        mContext = context;
        bbsList = list;
    }

    @Override
    public int getCount() {
        return bbsList.size();
    }

    @Override
    public ListItem getItem(int position) {
        return bbsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);

            holder = new ViewHolder();

            holder.MemberIcon = (ImageView)convertView.findViewById(R.id.img_member_icon);
            holder.MemberName = (TextView)convertView.findViewById(R.id.txt_member_name);
            holder.TypeIcon = (ImageView)convertView.findViewById(R.id.img_include_type);
            holder.Title = (TextView)convertView.findViewById(R.id.txt_title);
            holder.Time = (TextView)convertView.findViewById(R.id.txt_time);
            holder.Comment = (TextView)convertView.findViewById(R.id.txt_comment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        ListItem item = getItem(position);
        holder.MemberIcon.setImageBitmap(item.getMember_icon());
        if (item.getType_icon() != null) {
            holder.TypeIcon.setImageBitmap(item.getType_icon());
            holder.TypeIcon.setVisibility(View.VISIBLE);
        } else {
            holder.TypeIcon.setVisibility(View.GONE);
        }
        holder.MemberName.setText(item.getMember_name());
        holder.Title.setText(Html.fromHtml(item.getTitle().toString()));
        if (item.getTime().indexOf(":") >= 0) {
            holder.Time.setText(item.getTime());
        } else {
            holder.Time.setText(FormatTime(item.getTime()));
        }
        holder.Comment.setText(item.getReply());
        return convertView;
    }

    public String FormatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("yyyy.MM.dd");
        return format.format(date);
    }

    public static class ViewHolder {
        public ImageView MemberIcon;
        public TextView MemberName;
        public ImageView TypeIcon;
        public TextView Title;
        public TextView Time;
        public TextView Comment;
    }
}
