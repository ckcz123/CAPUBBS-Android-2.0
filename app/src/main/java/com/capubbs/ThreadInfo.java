package com.capubbs;

import com.capubbs.lib.MyCalendar;

public class ThreadInfo {
    int rank;
    String board;
    String boardName;
    String author;
    String replyer;
    long time;
    String title;
    int threadid;
    boolean isTop, isExtr, isLock;
    int pid;

    /**
     * ʮ�����ӵĳ�ʼ��
     *
     * @param _rank
     * @param _board
     * @param _boardName
     * @param _author
     * @param _time
     * @param _title
     * @param _threadid
     */
    public ThreadInfo(int _rank, String _board, String _boardName,
                      String _author, String _replyer, String _time, String _title, int _threadid, int _pid) throws Exception {
        rank = _rank;
        board = _board;
        boardName = new String(_boardName);
        author = _author;
        replyer=_replyer;
        title = new String(_title);
        threadid = _threadid;
        //time=_time;
        time= MyCalendar.format(_time);
        pid=_pid;
    }

    /**
     * һ����ͨ���ӵĳ�ʼ��
     *
     * @param _board
     * @param _boardName
     * @param _author
     * @param _time      ʱ���������Ϊ��λ��
     * @param _title
     * @param _threadid
     */
    public ThreadInfo(String _board, String _boardName,
                      String _author, String _replyer, String _time,
                      String _title, int _threadid, int top, int extr, int lock) {
        rank = 0;
        board = new String(_board);
        boardName = new String(_boardName);
        author = new String(_author);
        replyer = _replyer;
        title = new String(_title);
        threadid = _threadid;
        time = MyCalendar.format(_time);
        isTop = top == 1;
        isExtr=extr==1;
        isLock=lock==1;
    }

}
