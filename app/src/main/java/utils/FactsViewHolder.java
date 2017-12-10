package utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import bytes.sync.knowit.R;

/**
 * Created by Luffy on 10-Dec-17.
 */

public class FactsViewHolder extends RecyclerView.ViewHolder {

    ImageView categoryImageView;
    TextView categoryTextView, factsTextView;

    public FactsViewHolder(View itemView)
    {
        super(itemView);
        categoryImageView = (ImageView) itemView.findViewById(R.id.fav_category_imageView);
        categoryTextView = (TextView) itemView.findViewById(R.id.fac_category_textView);
        factsTextView = (TextView) itemView.findViewById(R.id.fav_facts_textView);
    }

}
