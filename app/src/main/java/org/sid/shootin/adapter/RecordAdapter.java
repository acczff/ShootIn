package org.sid.shootin.adapter;


import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.sid.shootin.R;
import org.sid.shootin.entity.GameInfo;

import java.util.List;

public class RecordAdapter extends BaseAdapter {
    private List<GameInfo> list;

    public RecordAdapter(List<GameInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, null);
            vh = new ViewHolder();
            vh.te_date = convertView.findViewById(R.id.te_date);
            vh.te_win = convertView.findViewById(R.id.te_win);
            vh.te_score = convertView.findViewById(R.id.te_score);
            vh.te_transport = convertView.findViewById(R.id.te_transport);
            vh.result = convertView.findViewById(R.id.result);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        GameInfo info = list.get(list.size() - 1 - position);
        vh.te_date.setText(info.getDate());
        vh.te_win.setText(info.getYour());
        vh.te_score.setText(info.getYourscore() + ":" + info.getHierscore());
        vh.te_transport.setText(info.getHier());
        if (info.getYourscore() > info.getHierscore()) {
            vh.result.setText("胜");
            vh.result.setTextColor(0xffff8800);
            vh.te_win.setTypeface(vh.te_win.getTypeface(), Typeface.BOLD);
            vh.te_transport.setTypeface(vh.te_transport.getTypeface(), Typeface.NORMAL);
        } else {
            vh.result.setText("败");
            vh.result.setTextColor(0xdacccccc);
            vh.te_transport.setTypeface(vh.te_transport.getTypeface(), Typeface.BOLD);
            vh.te_win.setTypeface(vh.te_win.getTypeface(), Typeface.NORMAL);
        }
        return convertView;
    }

    class ViewHolder {
        private TextView te_date, te_win, te_score, te_transport, result;
    }
}
