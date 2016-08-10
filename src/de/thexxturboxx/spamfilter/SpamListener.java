package de.thexxturboxx.spamfilter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpamListener implements Listener {
	
	SpamFilter plugin;
	
	public SpamListener(SpamFilter plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		try {
			Statement s = SpamFilter.c.createStatement();
			if(!SpamFilter.isMySQLplayerSet(s, e.getPlayer().getUniqueId())) {
				s.executeUpdate("INSERT INTO " + SpamFilter.TABLE + " (UUID, LastMessage, Anzahl) "
						+ "VALUES ('" + e.getPlayer().getUniqueId().toString() + "', '0', '0');");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	@EventHandler
	public void playerChat(AsyncPlayerChatEvent e) {
		if(e.isAsynchronous() && !e.getPlayer().hasPermission("spamfilter.canspam") && !e.getPlayer().isOp()) {
			try {
				Statement s = SpamFilter.c.createStatement();
				ResultSet rs = s.executeQuery("SELECT * FROM " + SpamFilter.TABLE + " WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
				if(rs.next() && rs.getInt("LastMessage") != 0) {
					Date lastMessage = StringToDate(rs.getString("LastMessage"));
					s.executeUpdate("UPDATE " + SpamFilter.TABLE + " SET LastMessage = '"+ DateToString(currentDate()) + "' WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
					if(difference(DateToString(lastMessage), DateToString(currentDate())) <= plugin.getConfig().getDouble("Cooldown") * 1000) {
						s.executeUpdate("UPDATE " + SpamFilter.TABLE + " SET Anzahl = Anzahl + 1 WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "' ORDER BY Anzahl DESC;");
					} else {
						s.executeUpdate("UPDATE " + SpamFilter.TABLE + " SET Anzahl = '0' WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
					}
					ResultSet rs_neu = s.executeQuery("SELECT * FROM " + SpamFilter.TABLE + " WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
					rs_neu.next();
					if(rs_neu.getInt("Anzahl") >= plugin.getConfig().getInt("Anzahl")) {
						e.getPlayer().sendMessage(SpamFilter.getPrefix() + plugin.getConfig().getString("Message"));
						e.setCancelled(true);
					}
				} else {
					s.executeUpdate("UPDATE " + SpamFilter.TABLE + " SET LastMessage = '"+ DateToString(currentDate()) + "' WHERE UUID = '" + e.getPlayer().getUniqueId().toString() + "';");
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private Date currentDate() {
		return new Date();
	}
	
	private String DateToString(Date date) {
		return format.format(date);
	}
	
	private Date StringToDate(String date) {
		try {
			return format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private long difference(String date1, String date2) {
        Date ddate1 = StringToDate(date1),
        ddate2 = StringToDate(date2);
        return ddate2.getTime() - ddate1.getTime(); 
	}
	
}