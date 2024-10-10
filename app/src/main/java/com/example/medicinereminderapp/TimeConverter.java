package com.example.medicinereminderapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeConverter {
    public String convert24HourTo12Hour(String time24Hour) {
        // Ensure the input time is in the correct format
        if (time24Hour.length() != 4) {
            throw new IllegalArgumentException("Input time must be in the format HHmm (e.g., 0900)");
        }

        // Extract hours and minutes from the input time
        int hour = Integer.parseInt(time24Hour.substring(0, 2));
        int minute = Integer.parseInt(time24Hour.substring(2));

        // Determine AM or PM and adjust hour accordingly
        String amPm;
        if(hour==00){
            hour+=12;
            amPm = "AM";
        }else if (hour < 12) {
            amPm = "AM";
        } else {
            amPm = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        }

        // Format the time in 12-hour format
        return String.format("%d:%02d %s", hour, minute, amPm);
    }

    public String convert12HourTo24Hour(String time12Hour) {
        try {
            // Parse the input time in 12-hour format
            DateFormat dateFormat12 = new SimpleDateFormat("h:mm a", Locale.US);
            Date date = dateFormat12.parse(time12Hour);

            // Format the parsed time in 24-hour format
            DateFormat dateFormat24 = new SimpleDateFormat("HHmm", Locale.US);
            return dateFormat24.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parsing exception
            return null;
        }
    }
}

