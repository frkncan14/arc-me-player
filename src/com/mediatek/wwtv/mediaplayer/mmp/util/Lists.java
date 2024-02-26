package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Lists {
    public static <E> List<E> newArrayList() {
        return new ArrayList<E>();
    }

    public static <E> List<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> Stack<E> newStack(){
    	return new Stack<E>();
    }

    public String toString() {
       return "Lists";
   }
}
