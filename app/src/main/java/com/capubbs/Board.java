package com.capubbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by oc on 2016/10/14.
 */
public class Board {
    String board, name, category;
    boolean anonymous;
    int total;
    public static HashMap<String, Board> boards = new HashMap<String, Board>();

    public static void load() {
        boards.clear();
        boards.put("1",new Board("1","��Э������"));
        boards.put("2",new Board("2","��������"));
        boards.put("3",new Board("3","���ѱ���"));
        boards.put("4",new Board("4","����ˮ"));
        boards.put("5",new Board("5","���������"));
        boards.put("6",new Board("6","����ĺ�"));
        boards.put("7",new Board("7","һ��֮��"));
        boards.put("9",new Board("9","��������"));
        boards.put("28",new Board("28","��վά��"));
    }

    public static ArrayList<String> getNames() {
        if (boards.isEmpty()) load();
        ArrayList<String> arrayList=new ArrayList<>();
        for (Map.Entry<String, Board> entry: boards.entrySet())
            arrayList.add(entry.getValue().name);
        //return arrayList;
        Collections.sort(arrayList, (o1, o2) -> Integer.parseInt(getIdByName(o1))-Integer.parseInt(getIdByName(o2)));
        return arrayList;
    }

    public static String getIdByName(String name) {
        if (boards.isEmpty()) load();
        for (Map.Entry<String, Board> entry: boards.entrySet())
            if (entry.getValue().name.equals(name))
                return entry.getKey();
        return "0";
    }

    public static String getNameById(String id) {
        if (boards.isEmpty()) load();
        if (boards.containsKey(id)) return boards.get(id).name;
        else return "δ֪����";
    }

    public static boolean containBoard(String id) {
        if (boards.isEmpty()) load();
        return boards.containsKey(id);
    }

    public Board(String _board, String _name) {this(_board,_name,"",0,0);}
    public Board(String _board, String _name, String _category, int _anonymous,
                 int total) {
        board = new String(_board);
        name = new String(_name);
        category = new String(_category);
        anonymous = _anonymous == 1;
        this.total = total;
    }
}
