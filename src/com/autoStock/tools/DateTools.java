/**
 * 
 */
package com.autoStock.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.autoStock.Co;
import com.autoStock.taLib.MInteger;
import com.autoStock.types.basic.Time;

/**
 * @author Kevin Kowalewski
 *
 */
public class DateTools {
	public static String getPrettyDate(long date){
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		//dateFormat.applyPattern("EEE, MMM d, yyyy hh:mm:ss a");
		dateFormat.applyPattern("yyyy/MM/dd hh:mm:ss a");
		
		return dateFormat.format(new Date(date));
	}
	
	public static String getPrettyDate(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("yyyy/MM/dd hh:mm:ss a");
		
		return dateFormat.format(date);
	}
	
	public static String getSqlDate(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss.S");
		return dateFormat.format(date);
	}
	
	public static Date getDateFromString(String date){
		try {
			return new SimpleDateFormat("yyyy/MM/dd.HH:mm.a").parse(date);
		}catch (ParseException e1){
			try {
				return new SimpleDateFormat("yyyy/MM/dd.HH:mm:ss.a").parse(date);
			}catch (ParseException e2){
				try {
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(date);
				}catch (ParseException e3){
					try {
						return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);	
					}catch (ParseException e4){
						try {
							return new SimpleDateFormat("MM/dd/yyyy").parse(date);
						}catch (ParseException e5){
							Co.println("Could not parse: " + date);
							return null;	
						}
					}
				}
			}
		}
	}
	
	public static Time getTimeFromString(String timeString){
		Time time = new Time();
		time.hour = Integer.valueOf(timeString.substring(0,2));
		time.minute = Integer.valueOf(timeString.substring(4,5));
		time.second = Integer.valueOf(timeString.substring(7,8));
		
		return time;
	}
	
	public static Date getChangedDate(Date date, int minutes){
		Date returnDate = (Date) date.clone();
		returnDate.setTime(date.getTime() - (minutes * 60) * 1000);
		
		return returnDate;
	}
	
	public static Date getDateFromTime(Time time){
		Date date = new Date();
		date.setHours(time.hour);
		date.setMinutes(time.minute);
		date.setSeconds(time.second);
		
		return date;
	}
	
	public static ArrayList<Date> getListOfDatesOnWeekdays(Date startDate, Date endDate){
		ArrayList<Date> listOfDate = new ArrayList<Date>();
		Calendar calendarAtCurrent = Calendar.getInstance();
		Calendar calendarAtEnd = Calendar.getInstance();
		
		calendarAtCurrent.setTime(startDate);
		calendarAtEnd.setTime(endDate);
		
		while (calendarAtCurrent.get(Calendar.MONTH) <= calendarAtEnd.get(Calendar.MONTH)&& calendarAtCurrent.get(Calendar.DAY_OF_MONTH) <= calendarAtEnd.get(Calendar.DAY_OF_MONTH)){
			if (calendarAtCurrent.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && calendarAtCurrent.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
				listOfDate.add(new Date(calendarAtCurrent.getTimeInMillis()));
			}
			
			calendarAtCurrent.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return listOfDate;
	}
}
