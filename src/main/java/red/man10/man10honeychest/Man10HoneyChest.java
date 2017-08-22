package red.man10.man10honeychest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10mysqlapi.MySQLAPI;
import red.man10.man10punishment.Man10PunishmentAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public final class Man10HoneyChest extends JavaPlugin implements Listener {

    MySQLAPI mysql = null;
    Man10PunishmentAPI punish = null;

    HashMap<String,String> argsO = new HashMap<>();
    HashMap<String,String> argsT = new HashMap<>();

    HashMap<String,String> argsOR = new HashMap<>();
    HashMap<String,String> argsTR = new HashMap<>();

    String createTable = "CREATE TABLE `man10_honeychest` (\n" +
            "\t`id` INT NOT NULL AUTO_INCREMENT,\n" +
            "\t`name` VARCHAR(32) NULL DEFAULT '0',\n" +
            "\t`uuid` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`action` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`mode` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`player_world` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`player_x` DOUBLE NULL DEFAULT '0',\n" +
            "\t`player_y` DOUBLE NULL DEFAULT '0',\n" +
            "\t`player_z` DOUBLE NULL DEFAULT '0',\n" +
            "\t`player_pitch` DOUBLE NULL DEFAULT '0',\n" +
            "\t`player_yaw` DOUBLE NULL DEFAULT '0',\n" +
            "\t`chest_world` VARCHAR(64) NULL DEFAULT '0',\n" +
            "\t`chest_x` DOUBLE NULL DEFAULT '0',\n" +
            "\t`chest_y` DOUBLE NULL DEFAULT '0',\n" +
            "\t`chest_z` DOUBLE NULL DEFAULT '0',\n" +
            "\t`date_time` DATETIME NOT NULL,\n" +
            "\t`time` BIGINT NULL DEFAULT '0',\n" +
            "\t PRIMARY KEY (`id`)\n" +
            ")\n" +
            "COLLATE='utf8_general_ci'\n" +
            "ENGINE=InnoDB\n" +
            ";\n";
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this,this);
        mysql = new MySQLAPI(this,"honeyChest");
        punish = new Man10PunishmentAPI(mysql);
        mysql.execute(createTable);
        argsO.put("jail","j");
        argsO.put("warn","w");
        argsO.put("practice","p");
        argsT.put("self","s");
        argsT.put("op","o");
        argsT.put("global","g");

        argsOR.put("j","Jail");
        argsOR.put("w","Warn");
        argsOR.put("p","Practice");
        argsTR.put("s","Self");
        argsTR.put("o","Op");
        argsTR.put("g","Global");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    String prefix = "§6[MHoneyChest]§f";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("mhoneychest")){
            Player p = (Player) sender;
            if(!p.hasPermission("man10.honeychest.create")){
                p.sendMessage(prefix + "あなたには権限がありません");
                return false;
            }
            String key = "";
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("help")){
                    onHelp(p);
                }
            }
            if(args.length == 3){
                if(args[0].equalsIgnoreCase("create")){
                    if(args[1].equalsIgnoreCase("jail") || args[1].equalsIgnoreCase("warn") || args[1].equalsIgnoreCase("practice")){
                        if(args[2].equalsIgnoreCase("self") || args[2].equalsIgnoreCase("op") || args[2].equalsIgnoreCase("global")){
                            key = argsO.get(args[1]) + "," + argsT.get(args[2]);
                            p.sendMessage(prefix + "アイテムを生成しました");
                            ItemStack item = new SItemStack(Material.CHEST).setDisplayname(codize(key)).build();
                            p.getInventory().addItem(item);
                        }else{
                            p.sendMessage(prefix + "通知モードが存在しません/mhoneychest help");
                        }
                    }else{
                        p.sendMessage(prefix + "刑罰モードが存在しません/mhoneychest help");
                    }
                }
            }
        }
        return true;
    }

    void onHelp(Player p){
        p.sendMessage("§6===============§4[§5Man10HoneyChest§4]§6===============");
        p.sendMessage("");
        p.sendMessage("§d§l/mhoneychest create <jail/warn/practice> <self/op/global>");
        p.sendMessage("");
        p.sendMessage("§6==============================================");
        p.sendMessage("§d§lCreated By Sho0");
    }

    public String currentTimeNoBracket(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
        return sdf.format(date);
    }

    public void createLog (Player p,Location chest,String action,String mode){
        Location l = p.getLocation();
        mysql.execute("INSERT INTO man10_honeychest VALUES('0','"+ p.getName() + "','" + p.getUniqueId() + "','" + action + "','" + mode + "','" + l.getWorld().getName() + "','" + l.getX() + "','" + l.getY() + "','" + l.getZ() +"','" + l.getPitch() + "','" + l.getYaw() + "','" + chest.getWorld().getName() + "','" + chest.getX() + "','" + chest.getY() + "','" + chest.getZ() + "','" + currentTimeNoBracket() + "','" + System.currentTimeMillis()/1000 + "');");
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        try {
            if (e.getInventory().getName().contains("§8Chest§m§a§n§1§0§h§o§n§e§y§c§h§e§s§t")) {
                createLog(((Player)e.getPlayer()),e.getInventory().getLocation(),"Open",mysqlDeCodize(deCodize(e.getInventory().getName())));
            }
        } catch (NullPointerException ee) {
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        try {
            if (e.getClickedInventory().getName().contains("§8Chest§m§a§n§1§0§h§o§n§e§y§c§h§e§s§t")) {
                if(e.getWhoClicked().hasPermission("man10.honeychest.edit")){
                    return;
                }
                e.setCancelled(true);
                if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR){
                    return;
                }
                createLog(((Player) e.getWhoClicked()),e.getClickedInventory().getLocation(),"Take",mysqlDeCodize(deCodize(e.getInventory().getName())));
                String de = deCodize(e.getInventory().getName());
                String[] splited = de.split(",");
                if (splited[0].equalsIgnoreCase("w")) {
                    punish.warnPlayer(e.getWhoClicked().getName(), e.getWhoClicked().getUniqueId(), "HoneyChest", null, "ハニーチェストからアイテムを取った");
                    if (splited[1].equals("s")) {
                        e.getWhoClicked().sendMessage(prefix + "§nあなたは人のからアイテムを取り10ポイント引かれました");
                        return;
                    }
                    if (splited[1].equals("g")) {
                        Bukkit.broadcastMessage("&b[&dMan10Score&b]&f".replaceAll("&", "§") + "§c§l" + e.getWhoClicked().getName() + "さんは『人のチェストからアイテムを取った』の理由で警告され10ポイント引かれました");
                        return;
                    }
                    if (splited[1].equals("o")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(prefix + e.getWhoClicked().getName() + "§nさんはハニーチェストからアイテムを取り10ポイント引かれました");
                            }
                        }
                    }
                    return;
                }
                if (splited[0].equals("j")) {
                    punish.jailPlayer(e.getWhoClicked().getName(), e.getWhoClicked().getUniqueId(), "HoneyChest", null, "ハニーチェストからアイテムを取った", "__default__");
                    if (splited[1].equals("s")) {
                        e.getWhoClicked().sendMessage(prefix + "§nあなたは人のチェストからアイテムを取り投獄されました");
                        return;
                    }
                    if (splited[1].equals("g")) {
                        Bukkit.broadcastMessage("&b[&dMan10Score&b]&f".replaceAll("&", "§") + "§c§l" + e.getWhoClicked().getName() + "さんは『人のチェストからアイテムを取った』の理由で投獄され100ポイント引かれましたました");
                        return;
                    }
                    if (splited[1].equals("o")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(prefix + e.getWhoClicked().getName() + "さんは人のチェストからアイテムを取りjailされた");
                            }
                        }
                    }
                }
                if (splited[0].equals("p")) {
                    if (splited[1].equals("s")) {
                        e.getWhoClicked().sendMessage("§c§l§nドロボー！ドロボー！ほかの人のチェストの物を取るとつかまります！");
                        return;
                    }
                    if (splited[1].equals("g")) {
                        Bukkit.broadcastMessage("§c§l§n" + e.getWhoClicked().getName() + "はドロボーだ！ほかの人のチェストの物を取るとつかまります！");
                        return;
                    }
                    if (splited[1].equals("o")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(prefix + e.getWhoClicked().getName() + "さんは練習用ハニーチェストからアイテムを取った");
                            }
                        }
                    }
                }
            }
        }catch (NullPointerException ee){
        }
    }

    // /mhoneychest create jail op

    String codize(String string){
        String s = "§8Chest§m§a§n§1§0§h§o§n§e§y§c§h§e§s§t";
        char[] caracters = string.toCharArray();
        for(int i = 0;i < caracters.length;i++){
            s += "§" + caracters[i];
        }
        return s;
    }

    String mysqlDeCodize(String code){
        String[] strings = code.split(",");
        String key = "";
        key += argsOR.get(strings[0]) + "," + argsTR.get(strings[1]);
        return key;
    }

    String deCodize(String string){
        String s = string.replace("§8Chest§m§a§n§1§0§h§o§n§e§y§c§h§e§s§t","");
        return  s.replaceAll("§","");
    }
}
