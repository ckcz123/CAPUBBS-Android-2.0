package com.capubbs.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MyCalendar {

    private static final Calendar standard =
            new GregorianCalendar(2015, 0, 5);

    /**
     * return 1 if Monday, 2 if Saturday, ... 7 if Sunday
     *
     * @param calendar
     * @return
     */
    public static int getWeekDayInNumber(Calendar calendar) {
        return getWeekDayInNumber(calendar, 0);
    }

    public static int getWeekDayInNumber(Calendar calendar, int daysAfter) {
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(calendar.getTimeInMillis() + 86400000 * daysAfter);
        int day = calendar2.get(Calendar.DAY_OF_WEEK);
        day--;
        if (day == 0) day = 7;
        return day;
    }

    /**
     * return "����һ" if Monday, ..., "������" if Sunday
     *
     * @param calendar
     * @return
     */
    public static String getWeekDayName(Calendar calendar) {
        int dof = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dof) {
            case Calendar.SUNDAY:
                return "������";
            case Calendar.MONDAY:
                return "����һ";
            case Calendar.TUESDAY:
                return "���ڶ�";
            case Calendar.WEDNESDAY:
                return "������";
            case Calendar.THURSDAY:
                return "������";
            case Calendar.FRIDAY:
                return "������";
        }
        return "������";
    }

    /**
     * ���� daysAfter��������ڼ������ְ�
     *
     * @param calendar
     * @param daysAfter
     * @return
     */
    public static String getWeekDayName(Calendar calendar, int daysAfter) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(calendar.getTimeInMillis() + 86400000 * daysAfter);
        return getWeekDayName(c);
    }

    /**
     * ������������֮����������
     *
     * @param calendar1
     * @param calendar2
     * @return ��������������ġ�����ֵ��
     */
    public static int getDeltaDays(Calendar calendar1, Calendar calendar2) {
        return Math.abs((int) (((calendar1.getTimeInMillis() - standard.getTimeInMillis()) / (1000 * 86400)
                - (calendar2.getTimeInMillis() - standard.getTimeInMillis()) / (1000 * 86400))));

    }

    /**
     * ���ؽ��վ���date���������
     *
     * @param date ��ʽΪ"yyyy-MM-dd"
     * @return -1 �����Ѿ���ȥ������Ϊ��ʣ����(>0)
     */
    public static int getDaysLeft(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date1 = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date2 = dateFormat.parse(date);
            long deltatime = date2.getTime() - date1.getTime();
            if (deltatime < 0) return -1;
            return (int) (deltatime / (1000 * 86400));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * ������������֮����˶��ٸ�����
     *
     * @param calendar1
     * @param calendar2
     * @return
     */
    public static int getDeltaWeeks(Calendar calendar1, Calendar calendar2) {
        return Math.abs((int) (((calendar1.getTimeInMillis() - standard.getTimeInMillis()) / (7000 * 86400)
                - (calendar2.getTimeInMillis() - standard.getTimeInMillis()) / (7000 * 86400))));
    }

    /**
     * ��ȡ��ȥ�˶�����
     *
     * @param calendar
     * @param daysAfter
     * @return
     */
    public static int getWeekPassed(Calendar calendar, int daysAfter) {
        int today = getWeekDayInNumber(calendar);
        return (today + daysAfter - 1) / 7;
    }

    public static String format(long timestamp) {
        return format(timestamp, "yyyy-MM-dd HH:mm:ss");
    }

    public static String format(long timestamp, String pattern) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
            return simpleDateFormat.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    public static long format(String time) {return format(time, "yyyy-MM-dd HH:mm:ss");}

    public static long format(String time, String pattern) {
        try {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat(pattern, Locale.getDefault());
            return simpleDateFormat.parse(time).getTime();
        }
        catch (Exception e) {return 0;}
    }

}


