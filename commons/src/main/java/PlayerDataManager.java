import com.google.common.collect.Maps;
import data.*;
import me.reidj.bridgebuilders.BridgeBuildersInstanceKt;
import me.reidj.bridgebuilders.user.User;
import org.bukkit.Bukkit;
import packages.BulkSaveUserPackage;
import packages.SaveUserPackage;
import packages.StatPackage;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;
import user.Stat;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerDataManager {

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
            try {
                StatPackage statPackage = BridgeBuildersInstanceKt.getClientSocket().writeAndAwaitResponse(new StatPackage(uuid)).get(5L, TimeUnit.SECONDS);
                Stat stat = statPackage.getStat();
                if (stat == null)
                    stat = new Stat(
                            uuid,
                            null,
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
                            0L
                    );

                if (stat.getDonates() == null)
                    stat.setDonates(new HashSet<>());

                if (stat.getRealm() == null)
                    stat.setRealm("");

                userMap.put(uuid, new User(stat));
            } catch (Exception ex) {
                event.setCancelReason("Не удалось загрузить статистику.");
                event.setCancelled(true);
                ex.printStackTrace();
            }
        }, 400);
        core.bus().register(this, AccountEvent.Unload.class, event -> {
            User user = userMap.remove(event.getUuid());
            if (user == null || user.getStat() == null)
                return;
            Stat info = user.getStat();
            BridgeBuildersInstanceKt.getClientSocket().write(new SaveUserPackage(event.getUuid(), info));
        }, 100);
    }

    public BulkSaveUserPackage bulk(boolean remove) {
        return new BulkSaveUserPackage(Bukkit.getOnlinePlayers().stream().map(pl -> {
            UUID uuid = pl.getUniqueId();
            User user = remove ? userMap.remove(uuid) : userMap.get(uuid);
            if (user == null)
                return null;
            return new SaveUserPackage(uuid, user.getStat());
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public void save() {
        BridgeBuildersInstanceKt.getClientSocket().write(bulk(true));
        try {
            Thread.sleep(3000L); // Если вдруг он не успеет написать в сокет(хотя вряд ли, конечно)
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
