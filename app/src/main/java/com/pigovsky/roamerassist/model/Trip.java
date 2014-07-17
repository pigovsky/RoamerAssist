package com.pigovsky.roamerassist.model;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pigovsky.roamerassist.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yp on 17.07.2014.
 */
public class Trip extends BaseAdapter
{
    private List<Point> points = new ArrayList<Point>();

    private LayoutInflater inflater;

    private long getMinDuration()
    {
        long res = getDuration(0);

        for(int i=1; i<getPoints().size()-1; i++) {
            long duration = getDuration(i);
            if(duration < res)
                res = duration;
        }
        return res;
    }

    private long getMaxDuration()
    {
        long res = getDuration(0);

        for(int i=1; i<getPoints().size()-1; i++) {
            long duration = getDuration(i);
            if(duration > res)
                res = duration;
        }
        return res;
    }

    private long getDuration(int i) {
        if (getPoints().size()<=1)
            return 0;
        return getPoints().get(i+1).getDateInMilliseconds()-
                getPoints().get(i).getDateInMilliseconds();
    }

    public void addPoint(Point point){
        getPoints().add(point);

        double minDuration = getMinDuration();
        double maxDuration = getMaxDuration();
        double delta = maxDuration-minDuration;

        if (delta>0.0){
            for(int i=0; i<getPoints().size()-1; i++) {
                double alpha = (getDuration(i)-minDuration)/delta * 255;
                int color = Color.rgb((int)alpha, 255-(int)alpha, 0 );
                getPoints().get(i).setColor(color);
            }
        }

        notifyDataSetChanged();
    }

    public Trip(Context c)
    {
        inflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount()
    {
        return getPoints().size();
    }

    @Override
    public Object getItem(int position) {
        return getPoints().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static int[] rowIds = new int[]{
            R.id.textview_time,
            R.id.textview_address,
            };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Point point = getPoints().get(position);
        String[] stringRow = point.asStringRow();
        View v = convertView;
        TextView[] row;
        if (convertView == null || convertView.getTag()==null){
            v = inflater.inflate(R.layout.row, null);

            row = new TextView[rowIds.length];
            for(int i=0; i<row.length; i++)
                row[i]=(TextView) v.findViewById(rowIds[i]);

            v.setTag(row);
        }
        row = (TextView[])v.getTag();

        for(int i=0; i<row.length; i++) {
            row[i].setText(stringRow[i]);
            row[i].setTextColor(point.getColor());
        }

        return v;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Point> getPoints() {
        return points;
    }
}
