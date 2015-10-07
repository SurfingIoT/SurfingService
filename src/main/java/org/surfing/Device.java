package org.surfing;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 *
 * @author vsenger
 */
public interface Device {
  public String getName();
  public String getResourceString();
  public String getDescription();
  public String getID();
  public List<String> getSendQueue();
  public String getPortName();
  public Map<String, Thing> getThings();
  public Collection<Thing> getThingsList();
  public void send(String s) throws IOException;
  public String receive() throws IOException;
  public void close() throws IOException;
  public void open() throws IOException;
  public void discovery() throws Exception;
  public boolean connected();
  public void addEventListener();
  public void setTimerControl(Timer t);
  public Timer getTimerControl();
}
