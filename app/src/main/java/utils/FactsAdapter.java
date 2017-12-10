package utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import bytes.sync.knowit.R;

/**
 * Created by Luffy on 10-Dec-17.
 */

public class FactsAdapter extends RecyclerView.Adapter<FactsViewHolder> {

    private static final String TAG = FactsAdapter.class.getCanonicalName();

    Context context;
    List<Facts> factsList;


    public FactsAdapter(Context context, List<Facts> factsList)
    {
        this.context = context;
        this.factsList = factsList;
    }


    @Override
    public int getItemCount() {
        return factsList.size();
    }


    @Override
    public FactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fav_item_layout, parent,false);
        return new FactsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FactsViewHolder holder, int position) {

        Facts fact = factsList.get(position);
        Log.d(TAG,"BindViewHolder, Fact: "+fact.toString());
        Log.d(TAG,"FactList: "+factsList.toString());

        holder.factsTextView.setText(fact.getFact());
        String category = fact.getCategory();

        if("trivia".equals(category))
            holder.categoryImageView.setImageResource(R.drawable.trivia);
        else if("math".equals(category))
            holder.categoryImageView.setImageResource(R.drawable.math);
        else if("date".equals(category))
            holder.categoryImageView.setImageResource(R.drawable.date);
        else if("year".equals(category))
            holder.categoryImageView.setImageResource(R.drawable.year);


        if(category != null && !category.isEmpty())
            category = String.valueOf(category.charAt(0)).toUpperCase() + category.substring(1);
        holder.categoryTextView.setText(category);

        Log.d(TAG,"ViewHolder set, category was: "+category);

    }


}
