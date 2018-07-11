package com.example.xyzreader.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

  /**
   *
   * Formats a given date or returns current date
   */
  public static String formatDate(String date) {
    DateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    try {
      Date formattedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss", Locale.US)
          .parse(date);
      return outputDateFormat.format(formattedDate);
    } catch (ParseException ex) {
      return outputDateFormat.format(new Date());
    }
  }
}
