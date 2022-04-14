import com.google.common.collect.Maps;
import data.*;
import me.reidj.bridgebuilders.BridgeBuildersInstanceKt;
import me.reidj.bridgebuilders.user.User;
import packages.SaveUserPackage;
import packages.StatPackage;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.event.AccountEvent;
import user.Stat;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                            new ArrayList<>(),
                            KillMessage.NONE,
                            StepParticle.NONE,
                            NameTag.NONE,
                            Corpse.NONE,
                            StarterKit.NONE,
                            0L
                    );
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
}
