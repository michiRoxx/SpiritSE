package org.michaels.s4a2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import android.database.Cursor;

public class FHSSchedule {
	public final static int LTYPE_LECTURE = 0;
	public final static int LTYPE_EXERCISE = 1;
	public final static int WEEK_EVEN = 2;
	public final static int WEEK_ODD = 1;
	public final static int WEEK_BOTH = 3;
	
	
	/**
	 * Searches the next event from now in database.
	 * @return The next FHSSchedule.Event or null, if there are no events in next two weeks.
	 */
	public static Event getNextEvent(){
		return getNextEvent(Calendar.getInstance());
	}
	
	/**
	 * Searches the next event in database.
	 * @param startSearch A calendar, where to start search.
	 * @return The next FHSSchedule.Event after startSearch or null, if there are no events in next two weeks.
	 */
	public static Event getNextEvent(Calendar startSearch){
		Calendar start = (Calendar) startSearch.clone();
		for(Event e:getEventsOfTheDay(start)){
			if(e.nextOccurence.after(start))
				return e;
		}
		for(int i=0; i<14; i++){
			start.add(Calendar.DAY_OF_MONTH, 1);
			if(getEventsOfTheDay(start).length > 0)
				return getEventsOfTheDay(start)[0];
		}
		return null;
	}
	
	public static Calendar getWeekStart(Calendar day){
		Calendar weekStart = (Calendar) day.clone();
		weekStart.add(Calendar.DATE, -(weekStart.get(Calendar.DAY_OF_WEEK)-1));
		weekStart.set(Calendar.HOUR_OF_DAY, 0);
		weekStart.set(Calendar.MINUTE, 0);
		weekStart.set(Calendar.SECOND, 0);
		weekStart.set(Calendar.MILLISECOND, 0);
		weekStart.setTimeZone(TimeZone.getTimeZone("CET"));			
		if(weekStart.get(Calendar.MONTH) > Calendar.MARCH || weekStart.get(Calendar.MONTH) <= Calendar.OCTOBER)
			weekStart.set(Calendar.DST_OFFSET, 3600000);
		if(weekStart.get(Calendar.MONTH) == Calendar.MARCH || isLastWeekOfMonth(weekStart))
			weekStart.set(Calendar.DST_OFFSET, 3600000);
		else if(weekStart.get(Calendar.MONTH) == Calendar.OCTOBER && isLastWeekOfMonth(weekStart))
			weekStart.set(Calendar.DST_OFFSET,0);
		return weekStart;
	}
	
	public static boolean isLastWeekOfMonth(Calendar dayinweek){
		Calendar nextweek = (Calendar) dayinweek.clone();
		nextweek.add(Calendar.DAY_OF_YEAR, 7);
		return (dayinweek.get(Calendar.MONTH) != nextweek.get(Calendar.MONTH));
	}
	
	public static long getDSTRepairOffset(Calendar day){
		Calendar weekStart = getWeekStart(day);
		if(weekStart.get(Calendar.MONTH) == Calendar.MARCH && isLastWeekOfMonth(weekStart))
			return -3600000;
		if(weekStart.get(Calendar.MONTH) == Calendar.OCTOBER && isLastWeekOfMonth(weekStart))
			return 3600000;
		return 0;
	}
	
	public static Event[] getEventsOfTheDay(Calendar day){
		if(day == null)
			return new Event[0];
		Calendar weekStart = getWeekStart(day);
		Calendar dayStart = (Calendar) day.clone();
		dayStart.set(Calendar.HOUR_OF_DAY, 0);
		dayStart.set(Calendar.MINUTE, 0);
		dayStart.set(Calendar.SECOND, 0);
		dayStart.set(Calendar.MILLISECOND, 0);
		long startSearch = (dayStart.getTimeInMillis()-weekStart.getTimeInMillis())/1000;
		Cursor c = Data.db.rawQuery("SELECT start, room, title, ltype FROM Event e, Lecture l " +
				"WHERE e.lecture = l.id AND start > ? AND start < ? AND (week = 3 OR week = ?) " +
				"AND egroup = 0 UNION " +
				"SELECT start, room, title, ltype FROM Event e, Usergroup u, Lecture l WHERE " +
				"e.lecture = u.lecture AND e.lecture = l.id AND start > ? AND start < ? AND " +
				"(week = 3 OR week = ?) AND egroup = ugroup ORDER BY start",new String[]{
					startSearch+"", (startSearch+86400)+"",
					(dayStart.get(Calendar.WEEK_OF_YEAR)%2 == 1 ? WEEK_ODD : WEEK_EVEN)+"",
					startSearch+"", (startSearch+86400)+"",
					(dayStart.get(Calendar.WEEK_OF_YEAR)%2 == 1 ? WEEK_ODD : WEEK_EVEN)+""
				});
		if(c.moveToFirst()){
			ArrayList<Event> rtn = new ArrayList<Event>();
			while(!c.isAfterLast()){
				Event e = new Event();
				e.nextOccurence = (Calendar) weekStart.clone();
				e.nextOccurence.add(Calendar.SECOND, c.getInt(c.getColumnIndex("start")));
				e.name = c.getString(c.getColumnIndex("title"));
				e.room = c.getString(c.getColumnIndex("room"));
				e.type = c.getInt(c.getColumnIndex("ltype"));
				rtn.add(e);
				c.moveToNext();
			}
			return rtn.toArray(new Event[0]);
		} else 
			return new Event[0];
	}
	
	public static class Event {
		public Calendar nextOccurence;
		public String name;
		public String room;
		public int type;
	}
}
