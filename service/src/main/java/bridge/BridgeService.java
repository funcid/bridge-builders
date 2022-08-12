package bridge;

import com.mongodb.client.model.Filters;
import lombok.val;
import me.reidj.bridgebuilders.packages.BulkSaveUserPackage;
import me.reidj.bridgebuilders.packages.SaveUserPackage;
import me.reidj.bridgebuilders.packages.StatPackage;
import me.reidj.bridgebuilders.packages.TopPackage;
import me.reidj.bridgebuilders.user.Stat;
import org.bson.conversions.Bson;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.Capability;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.permissions.PermissionService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Updates.unset;

/**
 * @author Рейдж 03.10.2021
 * @project BridgeBuilders
 */
public class BridgeService {

    private static MongoAdapter mongoAdapter;

    public static void main(String[] args) {

        MicroserviceBootstrap.bootstrap(new MicroServicePlatform(4));

        mongoAdapter = new MongoAdapter(System.getenv("db_url"), System.getenv("db_data"), "data");

        ISocketClient socketClient = ISocketClient.get();

        CoreApi.get().registerService(IPermissionService.class, new PermissionService(socketClient));

        socketClient.registerCapabilities(
                Capability.builder()
                        .className(StatPackage.class.getName())
                        .notification(true)
                        .build(),
                Capability.builder()
                        .className(SaveUserPackage.class.getName())
                        .notification(true)
                        .build(),
                Capability.builder()
                        .className(BulkSaveUserPackage.class.getName())
                        .notification(true)
                        .build(),
                Capability.builder()
                        .className(TopPackage.class.getName())
                        .notification(true)
                        .build()
                );

        socketClient.addListener(StatPackage.class, (channel, pckg) -> {
            System.out.println("Received UserInfoPackage from " + channel.getRealmName() + " for " + pckg.getUuid().toString());

            mongoAdapter.find(pckg.getUuid()).thenAccept(info -> {
                pckg.setStat(info);
                socketClient.forward(channel, pckg);
            });
        });
        socketClient.addListener(SaveUserPackage.class, (channel, pckg) -> {
            System.out.println("Received SaveUserPackage from " + channel.getRealmName() + " for " + pckg.getUser().toString());
            mongoAdapter.save(pckg.getUserInfo());
        });
        socketClient.addListener(BulkSaveUserPackage.class, (channel,  pckg) -> {
            System.out.println("Received BulkSaveUserPackage from " + channel.getRealmName());
            mongoAdapter.save(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList()));
        });
        socketClient.addListener(TopPackage.class, ((channel, pckg) ->
                mongoAdapter.getTop(pckg.getTopType(), pckg.getLimit()).thenAccept(res -> {
                    pckg.setEntries(res);
                    socketClient.forward(channel, pckg);
                })));
        Thread consoleThread = new Thread(BridgeService::handleConsole);
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    private static void handleConsole() {
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        while (true) {
            String s = scanner.next();
            String[] args = s.split(" ");
            if (s.equals("stop")) {
                System.exit(0);
                return;
            }
            if (s.equals("players")) {
                try {
                    Map<UUID, Stat> uuidUserInfoMap = mongoAdapter.findAll().get(5, TimeUnit.SECONDS);
                    uuidUserInfoMap.forEach((k, v) -> System.out.println(k + ": " + GlobalSerializers.toJson(v)));
                    System.out.println(uuidUserInfoMap.size() + " players in total.");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (s.equals("vipe")) {
                val map = new HashMap<Bson, Bson>() {{
                    put(Filters.gt("money", 0), unset("money"));
                    put(Filters.gt("achievement", new ArrayList<>()), unset("achievement"));
                    put(Filters.gt("games", 0), unset("games"));
                    put(Filters.gt("kills", 0), unset("kills"));
                    put(Filters.gt("lootbox", 0), unset("lootbox"));
                    put(Filters.gt("lootboxOpenned", 0), unset("lootboxOpenned"));
                    put(Filters.gt("isBan", false), unset("isBan"));
                    put(Filters.gt("wins", 0), unset("wins"));
                    put(Filters.gt("realm", ""), unset("realm"));
                    put(Filters.gt("gameLockTime", 0), unset("gameLockTime"));
                    put(Filters.gt("gameExitTime", 0), unset("gameExitTime"));
                }};
                map.forEach((key, value) -> mongoAdapter.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
            if (s.equals("unbanall")) {
                val map = new HashMap<Bson, Bson>() {{
                    put(Filters.gt("realm", ""), unset("realm"));
                    put(Filters.gt("gameLockTime", 0), unset("gameLockTime"));
                    put(Filters.gt("gameExitTime", 0), unset("gameExitTime"));
                }};
                map.forEach((key, value) -> mongoAdapter.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
            if (s.equals("test")) {
                val map = new HashMap<Bson, Bson>() {{
                    put(Filters.gt("dailyClaimTimestamp", 0L), unset("dailyClaimTimestamp"));
                    put(Filters.gt("dailyTimestamp", 0L), unset("dailyTimestamp"));
                    put(Filters.gt("lastEnter", 0L), unset("lastEnter"));
                }};
                map.forEach((key, value) -> mongoAdapter.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
        }
    }
}
