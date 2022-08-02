import com.google.common.collect.Maps;
import data.*;
import me.reidj.bridgebuilders.BridgeBuildersInstanceKt;
import me.reidj.bridgebuilders.user.User;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import packages.BulkSaveUserPackage;
import packages.SaveUserPackage;
import packages.StatPackage;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;
import user.Stat;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerDataManager implements Listener {

    private final Map<UUID, User> userMap = Maps.newHashMap();

    public Map<UUID, User> getUserMap() {
        return userMap;
    }

    public PlayerDataManager() {
        CoreApi core = CoreApi.get();
        core.bus().register(this, AccountEvent.Load.class, event -> {
            if (event.isCancelled())
                return;
            UUID uuid = event.getUuid();
            if (userMap.containsKey(uuid))
                return;
            try {
                StatPackage statPackage = BridgeBuildersInstanceKt.getClientSocket().writeAndAwaitResponse(new StatPackage(uuid))
                        .get(5L, TimeUnit.SECONDS);
                Stat stat = statPackage.getStat();
                if (stat == null)
                    stat = new Stat(
                            uuid,
                            "",
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            new ArrayList<>(),
                            new HashSet<>(),
                            new ArrayList<>(),
                            KillMessage.NONE,
                            StepParticle.NONE,
                            NameTag.NONE,
                            Corpse.NONE,
                            StarterKit.NONE,
                            0.0,
                            0.0,
                            0L,
                            0L,
                            0L,
                            0,
                            true,
                            false
                    );

                if (stat.getUuid() == null)
                    stat.setUuid(uuid);

                if (stat.getRealm() == null)
                    stat.setRealm("");

                if (stat.getDonate() == null)
                    stat.setDonate(new ArrayList<>());

                if (stat.getDonates() == null)
                    stat.setDonates(new HashSet<>());

                if (stat.getAchievement() == null)
                    stat.setAchievement(new ArrayList<>());

                if (stat.getActiveKillMessage() == null)
                    stat.setActiveKillMessage(KillMessage.NONE);

                if (stat.getActiveParticle() == null)
                    stat.setActiveParticle(StepParticle.NONE);

                if (stat.getActiveNameTag() == null)
                    stat.setActiveNameTag(NameTag.NONE);

                if (stat.getActiveCorpse() == null)
                    stat.setActiveCorpse(Corpse.NONE);

                if (stat.getActiveKit() == null)
                    stat.setActiveKit(StarterKit.NONE);

                if (stat.getIsApprovedResourcepack() == null)
                    stat.setIsApprovedResourcepack(true);

                if (stat.getIsBan() == null)
                    stat.setIsBan(false);

                if (stat.getGameLockTime() == null)
                    stat.setGameLockTime(0);

                if (stat.getBanTime() == null)
                    stat.setBanTime(0.0);

                if (stat.getLeaveTime() == null)
                    stat.setLeaveTime(0.0);

                if (stat.getTimePlayedTotal() == null)
                    stat.setTimePlayedTotal(0L);

                if (stat.getGameExitTime() == null)
                    stat.setGameExitTime(0);

                if (stat.getDailyClaimTimestamp() == null)
                    stat.setDailyClaimTimestamp(0L);

                if (stat.getLastEnter() == null)
                    stat.setLastEnter(0L);

                if (stat.getRewardStreak() == null)
                    stat.setRewardStreak(0);

                userMap.put(uuid, new User(stat));
            } catch (Exception ex) {
                event.setCancelReason("Не удалось загрузить статистику.");
                event.setCancelled(true);
                ex.printStackTrace();
            }
        }, 400);
        core.bus().register(this, AccountEvent.Unload.class, event -> {
            User user = userMap.get(event.getUuid());
            if (user == null)
                return;
            Stat info = user.getStat();
            if (!user.getInGame())
                userMap.remove(event.getUuid());
            BridgeBuildersInstanceKt.getClientSocket().write(new SaveUserPackage(event.getUuid(), info));
        }, 100);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
            userMap.remove(event.getPlayer().getUniqueId());
    }

    public BulkSaveUserPackage bulk() {
        return new BulkSaveUserPackage(Bukkit.getOnlinePlayers().stream().map(pl -> {
            UUID uuid = pl.getUniqueId();
            User user = userMap.remove(uuid);
            if (user == null)
                return null;
            return new SaveUserPackage(uuid, user.getStat());
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public void save() {
        BridgeBuildersInstanceKt.getClientSocket().write(bulk());
        try {
            Thread.sleep(1000L); // Если вдруг он не успеет написать в сокет(хотя вряд ли, конечно)
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
