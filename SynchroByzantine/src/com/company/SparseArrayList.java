package com.company;

import java.util.ArrayList;

public class SparseArrayList extends ArrayList<Integer> {
    public void SparseAdd(int index, int value) {

        if (index <= this.size())
            this.add(index, value);
        else
        {
            for (int i = this.size(); i < index; i++)
            {
                this.add(null);
            }
            this.add(value);
        }
    }
}
