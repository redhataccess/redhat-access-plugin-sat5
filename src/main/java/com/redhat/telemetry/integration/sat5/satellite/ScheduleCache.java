package com.redhat.telemetry.integration.sat5.satellite;

import java.util.HashMap;

//Keep a cache of the scheduled actions while the server is running.
//This will be cleared when the server restarts.
//At that point, the GUI will display an error for the pending systems instead of a clock
//TODO: put a warning (?) instead of an error (!) when there is an action scheduled for
//      the system and the package is not yet installed.
public class ScheduleCache {
  private static final ScheduleCache INSTANCE = new ScheduleCache();
  private HashMap<Integer, Integer> schedules;
 
  private ScheduleCache() {
    if (this.schedules == null) {
      this.schedules = new HashMap<Integer, Integer>();
    }
  }
 
  public static ScheduleCache getInstance() {
    return INSTANCE;
  }

  public void addSchedule(int sysId, int scheduleId) {
    this.schedules.put(sysId, scheduleId);
  }

  public Integer getSystemSchedule(int sysId) {
    Integer schedule = this.schedules.get(sysId);
    return schedule;
  }

  public HashMap<Integer, Integer> getSchedules() {
    return this.schedules;
  }

  public void remove(int sysId) {
    this.schedules.remove(sysId);
  }
}
