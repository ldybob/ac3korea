package com.ldybob.ac3korea;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReplyBaseAdapter extends BaseAdapter {
    public interface RereplyListener {
        public void CallRereply(int pos);
    }

    Context mContext;
    ArrayList<ReplyItem> mList;
    private ViewHolder holder = null;
    RereplyListener listener;

    public ReplyBaseAdapter(Context context, ArrayList<ReplyItem> list, RereplyListener listen) {
        mContext = context;
        mList = list;
        listener = listen;
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ReplyItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.reply_item, parent, false);

            holder = new ViewHolder();

            holder.Answer = (TextView)convertView.findViewById(R.id.txt_answer);
            holder.MemberIcon = (ImageView)convertView.findViewById(R.id.img_member_icon);
            holder.MemberName = (TextView)convertView.findViewById(R.id.txt_member_name);
            holder.Content = (TextView)convertView.findViewById(R.id.reply_content);
            holder.Time = (TextView)convertView.findViewById(R.id.txt_time);
            holder.space = (ImageView)convertView.findViewById(R.id.space);
            holder.reply = (Button)convertView.findViewById(R.id.rereply_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        ReplyItem item = mList.get(position);
        if (item.getAnswer().isEmpty()) {
            holder.Answer.setVisibility(View.GONE);
        } else {
            holder.Answer.setVisibility(View.VISIBLE);
            holder.Answer.setText(item.getAnswer());
        }
        holder.MemberIcon.setImageBitmap(item.getMember_icon());
        holder.MemberName.setText(item.getMember_name());
        holder.Content.setText(Html.fromHtml(strHelper.nl2br(item.getContent())));
        holder.Time.setText(FormatTime(item.getTime()));
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)holder.space.getLayoutParams();
        param.setMargins(item.getSpace() * 50, 0, 0, 0);
        holder.space.setLayoutParams(param);
        holder.reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.CallRereply(position);
            }
        });
        return convertView;
    }

    public String FormatTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat("yyyy.MM.dd <HH:mm>");
        return format.format(date);
    }

    public static class ViewHolder {
        public TextView Answer;
        public ImageView MemberIcon;
        public TextView MemberName;
        public TextView Content;
        public TextView Time;
        public ImageView space;
        public Button reply;
    }
}
