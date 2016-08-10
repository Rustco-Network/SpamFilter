package de.thexxturboxx.spamfilter;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.BanList.Type;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.huskehhh.mysql.mysql.MySQL;

public class SpamFilter extends JavaPlugin {
	
	public static SpamFilter instance;
	public static File path = new File("plugins/SpamFilter"), dataPath;
	public static MySQL MySQL = null;
    public static Connection c = null;
    public static final String TABLE = "SpamFilter",
    		DATABASE = "AutoNick";
	
	public static SpamFilter getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		try {
			loadConfiguration();
			if(!getConfig().contains("MySQL.hostname") || getConfig().getString("MySQL.hostname").equals("null")) {
				set("MySQL.hostname", "null");
				set("MySQL.port", "null");
				set("MySQL.username", "null");
				set("MySQL.password", "null");
				getServer().getLogger().info("Bitte gib Deine MySQL-Daten in der Config ein!");
				getServer().shutdown();
				return;
			} else {
				MySQL = new MySQL(getConfig().getString("MySQL.hostname"),
								  getConfig().getString("MySQL.port"),
								  DATABASE,
								  getConfig().getString("MySQL.username"),
								  getConfig().getString("MySQL.password"));
				c = MySQL.openConnection();
				Statement s = c.createStatement();
				s.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE + " (UUID VARCHAR(40) PRIMARY KEY, LastMessage VARCHAR(20), Anzahl INT);");
			}
			initStandard("Cooldown", 3);
			initStandard("Anzahl", 3);
			initStandard("Message", ChatColor.DARK_RED + "Komm runter, bitte!");
			getServer().getPluginManager().registerEvents(new SpamListener(this), this);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public void set(String key, Object value) {
		getConfig().set(key, value);
		saveConfig();
	}
	
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void initStandard(String key, Object standardValue) {
		if(!getConfig().isSet(key)) {
			getConfig().set(key, standardValue);
			saveConfig();
		}
	}
	
	public void simplePardon(String name) {
		getServer().getBanList(Type.NAME).pardon(name);
	}
	
	public void simpleBan(String name, String reason) {
		getServer().getBanList(Type.NAME).addBan(name, reason, null, null);
	}
	
	public static File getPluginPath() {
		return path;
	}
	
	public static File getDataPath() {
		return dataPath;
	}
	
	public static String getPrefix() {
		return ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "SpamFilter" + ChatColor.GRAY + "] ";
	}
	
	public static boolean isMySQLplayerSet(Statement s, UUID uuid) throws SQLException {
		ResultSet rs = s.executeQuery("SELECT * FROM " + TABLE + " WHERE UUID = '" + uuid.toString() + "';");
		return rs.next();
	}
	
	public static double round(double value, int decimal) {
	    return (double) Math.round(value * Math.pow(10d, decimal)) / Math.pow(10d, decimal);
	}
	
}