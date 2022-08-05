package bridge;

import bridge.handlers.PackageHandler;
import bridge.realm.RealmsController;
import bridge.socket.ServerSocket;
import bridge.socket.ServerSocketHandler;
import com.google.common.collect.Maps;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.client.model.Filters;
import io.javalin.Javalin;
import io.netty.channel.Channel;
import lombok.val;
import org.bson.conversions.Bson;
import packages.*;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.TransferPlayerPackage;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.permissions.PermissionService;
import ru.cristalix.core.realm.RealmInfo;
import user.Stat;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Updates.unset;

/**
 * @author Рейдж 03.10.2021
 * @project BridgeBuilders
 */
public class BridgeService {

    public static String PASSWORD;
    @SuppressWarnings("rawtypes")
    public static final Map<Class<? extends BridgePackage>, PackageHandler> HANDLER_MAP = new HashMap<>();
    public static final Map<String, ThePitMetricsPackage> METRICS = Maps.newConcurrentMap();

    public static UserDataMongoAdapter userData;

    private static RealmsController realmsController;

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("cristalix.core.net-context-limit", "655360");
        int thepitServicePort;
        try {
            thepitServicePort = Integer.parseInt(System.getenv("BRIDGE_SERVICE_PORT"));
        } catch (NumberFormatException | NullPointerException exception) {
            System.out.println("No BRIDGE_SERVICE_PORT environment variable specified!");
            Thread.sleep(1000);
            return;
        }

        PASSWORD = System.getenv("BRIDGE_SERVICE_PASSWORD");
        if (PASSWORD == null) {
            System.out.println("No BRIDGE_SERVICE_PASSWORD environment variable specified!");
            Thread.sleep(1000);
            return;
        }

        MicroserviceBootstrap.bootstrap(new MicroServicePlatform(2));
        CoreApi.get().registerService(IPermissionService.class, new PermissionService(ISocketClient.get()));

        ServerSocket serverSocket = new ServerSocket(thepitServicePort);
        serverSocket.start();

        String dbUrl = System.getenv("db_url");
        String dbName = System.getenv("db_data");
        MongoClient client = MongoClients.create(dbUrl);
        userData = new UserDataMongoAdapter(client, dbName);

        realmsController = new RealmsController();

        registerHandler(ThePitMetricsPackage.class, (channel, source, pckg) -> {
            System.out.println("Received metrics.");
            METRICS.put(pckg.getServerName(), pckg);
        });
        registerHandler(StatPackage.class, (channel, source, pckg) -> {
            System.out.println("Received UserInfoPackage from " + source + " for " + pckg.getUuid().toString());

            userData.find(pckg.getUuid()).thenAccept(info -> {
                pckg.setStat(info);
                answer(channel, pckg);
            });
        });
        registerHandler(SaveUserPackage.class, (channel, source, pckg) -> {
            System.out.println("Received SaveUserPackage from " + source + " for " + pckg.getUser().toString());
            userData.save(pckg.getUserInfo());
        });
        registerHandler(BulkSaveUserPackage.class, (channel, source, pckg) -> {
            System.out.println("Received BulkSaveUserPackage from " + source);
            userData.save(pckg.getPackages().stream().map(SaveUserPackage::getUserInfo).collect(Collectors.toList()));
        });
        registerHandler(TopPackage.class, ((channel, serverName, pitPackage) ->
                userData.getTop(pitPackage.getTopType(), pitPackage.getLimit()).thenAccept(res -> {
                    pitPackage.setEntries(res);
                    answer(channel, pitPackage);
                })));
        registerHandler(ResetRejoin.class, ((channel, serverName, pckg) -> userData.find(pckg.getUuid()).thenAccept(stat -> {
            stat.setRealm("");
            pckg.setStat(stat);
            userData.save(stat);
        })));
        registerHandler(UserRequestJoinPackage.class, ((channel, serverName, thePitPackage) -> {
            Optional<RealmInfo> realm = realmsController.bestRealm();
            boolean passed = false;
            if (realm.isPresent()) {
                passed = true;
                RealmInfo realmInfo = realm.get();
                realmInfo.setCurrentPlayers(realmInfo.getCurrentPlayers() + 1);
                ISocketClient.get().write(new TransferPlayerPackage(thePitPackage.getUser(), realmInfo.getRealmId(), Collections.emptyMap()));
            }
            thePitPackage.setPassed(passed);
            answer(channel, thePitPackage);
        }));

        try {
            Javalin.create().get("/", ctx -> ctx.result(createMetrics())).start(Integer.parseInt(System.getenv("METRICS_PORT")));
        } catch (NumberFormatException | NullPointerException ignored) {
        }

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
                    Map<UUID, Stat> uuidUserInfoMap = userData.findAll().get(5, TimeUnit.SECONDS);
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
                map.forEach((key, value) -> userData.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
            if (s.equals("unbanall")) {
                val map = new HashMap<Bson, Bson>() {{
                    put(Filters.gt("realm", ""), unset("realm"));
                    put(Filters.gt("gameLockTime", 0), unset("gameLockTime"));
                    put(Filters.gt("gameExitTime", 0), unset("gameExitTime"));
                }};
                map.forEach((key, value) -> userData.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
            if (s.equals("test")) {
                val map = new HashMap<Bson, Bson>() {{
                    put(Filters.gt("dailyClaimTimestamp", 0L), unset("dailyClaimTimestamp"));
                    put(Filters.gt("dailyTimestamp", 0L), unset("dailyTimestamp"));
                    put(Filters.gt("lastEnter", 0L), unset("lastEnter"));
                }};
                map.forEach((key, value) -> userData.getData().updateMany(key, value, (result, t) -> t.printStackTrace()));
            }
            if (args[0].equals("delete")) {
                if (args.length < 2) System.out.println("Usage: delete [uuid]");
                else {
                    if (args[1].equals("all")) {
                        userData.findAll().thenAccept(map -> map.forEach(((uuid, userInfo) -> {
                            userData.clear(uuid);
                            System.out.println("Removed " + uuid);
                        })));
                    } else {
                        UUID uuid = UUID.fromString(args[1]);
                        userData.clear(uuid);
                        System.out.println("Removed " + uuid + "'s data from db...");
                    }
                }
            }
        }

    }

    private static String createMetrics() {
        Map<String, ThePitMetricsPackage.PacketMetric> metrics = Maps.newHashMap();
        StringBuilder builder = new StringBuilder();
        METRICS.forEach((source, data) -> {
            builder.append("online{bridge.realm=\"").append(source).append("\"} ").append(data.getOnline()).append("\n");
            builder.append("tps{bridge.realm=\"").append(source).append("\"} ").append(data.getTps()).append("\n");
            builder.append("free_memory{bridge.realm=\"").append(source).append("\"} ").append(data.getFreeMemory()).append("\n");
            builder.append("allocated_memory{bridge.realm=\"").append(source).append("\"} ").append(data.getAllocatedMemory()).append("\n");
            builder.append("total_memory{bridge.realm=\"").append(source).append("\"} ").append(data.getTotalMemory()).append("\n");

            data.getMetrics().forEach((key, value) -> {
                metrics.compute(key, (__, old) -> {
                    if (old != null) {
                        old.setCompressedBytes(old.getCompressedBytes() + value.getCompressedBytes());
                        old.setDecompressedBytes(old.getDecompressedBytes() + value.getDecompressedBytes());
                        old.setReceived(old.getReceived() + value.getReceived());
                        old.setReceivedBytes(old.getReceivedBytes() + value.getReceivedBytes());
                        old.setSent(old.getSent() + value.getSent());
                        old.setSentBytes(old.getSentBytes() + value.getSentBytes());
                        return old;
                    }
                    return value.clone();
                });
            });
        });
        metrics.forEach((key, value) -> {
            builder.append("compressed_bytes{packet=\"").append(key).append("\"} ").append(value.getCompressedBytes()).append("\n");
            builder.append("decompressed_bytes{packet=\"").append(key).append("\"} ").append(value.getDecompressedBytes()).append("\n");
            builder.append("received{packet=\"").append(key).append("\"} ").append(value.getReceived()).append("\n");
            builder.append("received_bytes{packet=\"").append(key).append("\"} ").append(value.getReceivedBytes()).append("\n");
            builder.append("sent{packet=\"").append(key).append("\"} ").append(value.getSent()).append("\n");
            builder.append("sent_bytes{packet=\"").append(key).append("\"} ").append(value.getSentBytes()).append("\n");
        });
        return builder.toString();
    }

    /**
     * Send package to bridge.socket
     *
     * @param pckg package
     */
    private static void answer(Channel channel, BridgePackage pckg) {
        ServerSocketHandler.send(channel, pckg);
    }

    /**
     * Register handler to package type
     *
     * @param clazz   class of package
     * @param handler handler
     * @param <T>     package type
     */
    private static <T extends BridgePackage> void registerHandler(Class<T> clazz, PackageHandler<T> handler) {
        HANDLER_MAP.put(clazz, handler);
    }
}
