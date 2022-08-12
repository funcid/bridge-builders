package bridge;

import com.mongodb.ClientSessionOptions;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.session.ClientSession;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import me.reidj.bridgebuilders.packages.TopPackage;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.BulkGroupsPackage;
import ru.cristalix.core.network.packages.GroupData;
import me.reidj.bridgebuilders.tops.PlayerTopEntry;
import me.reidj.bridgebuilders.tops.TopEntry;
import me.reidj.bridgebuilders.user.Stat;
import me.reidj.bridgebuilders.util.UtilCristalix;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MongoAdapter {

	private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

	@Getter
	private final MongoCollection<Document> data;
	private final ClientSession session;

	public MongoAdapter(String dbUrl, String database, String collection) {
		CompletableFuture<ClientSession> future = new CompletableFuture<>();
		MongoClient client = MongoClients.create(dbUrl);
		client.startSession(ClientSessionOptions.builder().causallyConsistent(true).build(), (s, throwable) -> {
			if (throwable != null) future.completeExceptionally(throwable);
			else future.complete(s);
		});
		data = client.getDatabase(database).getCollection(collection);
		try {
			session = future.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<Stat> find(UUID uuid) {
		CompletableFuture<Stat> future = new CompletableFuture<>();
		data.find(session, Filters.eq("uuid", uuid.toString()))
				.first((result, throwable) -> {
					try {
						future.complete(readDocument(result));
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		return future;
	}

	public CompletableFuture<Map<UUID, Stat>> findAll() {
		CompletableFuture<Map<UUID, Stat>> future = new CompletableFuture<>();
		FindIterable<Document> documentFindIterable = data.find();
		Map<UUID, Stat> map = new ConcurrentHashMap<>();
		documentFindIterable.forEach(document -> {
			Stat object = readDocument(document);
			map.put(object.getUuid(), object);
		}, (v, error) -> future.complete(map));
		return future;
	}

	private Stat readDocument(Document document) {
		return document == null ? null : GlobalSerializers.fromJson(document.toJson(), Stat.class);
	}

	public void save(Stat info) {
		save(Collections.singletonList(info));
	}

	public void save(List<Stat> uniques) {
		List<WriteModel<Document>> models = new ArrayList<>();
		for (Stat unique : uniques) {
			WriteModel<Document> model = new UpdateOneModel<>(
					Filters.eq("uuid", unique.getUuid().toString()),
					new Document("$set", Document.parse(GlobalSerializers.toJson(unique))),
					UPSERT
			);
			models.add(model);
		}

		if (!models.isEmpty())
			data.bulkWrite(session, models, this::handle);
	}

	public <V> CompletableFuture<List<TopEntry<Stat, V>>> makeRatingByField(String fieldName, int limit) {
		List<Bson> operations = Arrays.asList(
				Aggregates.project(Projections.fields(
						Projections.include(fieldName),
						Projections.include("prefix"),
						Projections.include("uuid"),
						Projections.exclude("_id")
				)), Aggregates.sort(Sorts.descending(fieldName)),
				Aggregates.limit(limit)
		);
		List<TopEntry<Stat, V>> entries = new ArrayList<>();
		CompletableFuture<List<TopEntry<Stat, V>>> future = new CompletableFuture<>();
		data.aggregate(operations).forEach(document -> {
			Stat key = readDocument(document);
			entries.add(new TopEntry<>(key, (V) document.get(fieldName)));
		}, (__, throwable) -> {
			if (throwable != null) {
				future.completeExceptionally(throwable);
				return;
			}
			future.complete(entries);
		});
		return future;
	}

	private void handle(Object result, Throwable throwable) {
		if (throwable != null)
			throwable.printStackTrace();
	}

	public CompletableFuture<List<PlayerTopEntry<Object>>> getTop(TopPackage.TopType topType, int limit) {
		return makeRatingByField(topType.name().toLowerCase(), limit).thenApplyAsync(entries -> {
			List<PlayerTopEntry<Object>> playerEntries = new ArrayList<>();
			for (TopEntry<Stat, Object> userInfoObjectTopEntry : entries) {
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
