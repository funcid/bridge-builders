package bridge;

import com.mongodb.async.client.MongoClient;
import lombok.val;
import packages.TopPackage;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.BulkGroupsPackage;
import ru.cristalix.core.network.packages.GroupData;
import tops.PlayerTopEntry;
import tops.TopEntry;
import user.Stat;
import util.UtilCristalix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserDataMongoAdapter extends MongoAdapter<Stat> {

    public UserDataMongoAdapter(MongoClient client, String dbName) {
        super(client, dbName, "data", Stat.class);
    }

    public CompletableFuture<List<PlayerTopEntry<Object>>> getTop(TopPackage.TopType topType, int limit) {
        return makeRatingByField(topType.name().toLowerCase(), limit).thenApplyAsync(entries -> {
            entries.forEach(en -> System.out.println(en.getKey()));
            List<PlayerTopEntry<Object>> playerEntries = new ArrayList<>();
            for (val userInfoObjectTopEntry : entries) {
                PlayerTopEntry<Object> objectPlayerTopEntry = new PlayerTopEntry<>(userInfoObjectTopEntry.getKey(), userInfoObjectTopEntry.getValue());
                playerEntries.add(objectPlayerTopEntry);
            }
            try {
                List<UUID> uuids = new ArrayList<>();
                for (TopEntry<Stat, Object> entry : entries) {
                    UUID uuid = entry.getKey().getUuid();
                    uuids.add(uuid);
                }
                List<GroupData> groups = ISocketClient.get()
                        .<BulkGroupsPackage>writeAndAwaitResponse(new BulkGroupsPackage(uuids))
                        .get(5L, TimeUnit.SECONDS)
                        .getGroups();
                Map<UUID, GroupData> map = groups.stream()
                        .collect(Collectors.toMap(GroupData::getUuid, Function.identity()));
                for (PlayerTopEntry<Object> playerEntry : playerEntries) {
                    GroupData data = map.get(playerEntry.getKey().getUuid());
                    System.out.println("data " + data);
                    System.out.println("name " + data.getUsername());
                    playerEntry.setUserName(data.getUsername());
                    playerEntry.setDisplayName(UtilCristalix.createDisplayName(data));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                // Oh shit
                playerEntries.forEach(entry -> {
                    entry.setUserName("ERROR");
                    entry.setDisplayName("ERROR");
                });
            }
            return playerEntries;
        });
    }

}
