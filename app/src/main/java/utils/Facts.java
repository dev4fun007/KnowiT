package utils;

import io.realm.RealmObject;

/**
 * Created by Luffy on 10-Dec-17.
 */

public class Facts extends RealmObject {

    private String fact;
    private String category;


    public String getFact()
    {
        return fact;
    }

    public void setFact(String fact)
    {
        this.fact = fact;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

}
