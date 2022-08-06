package bridge;

import me.reidj.bridgebuilders.packages.*;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.microservice.MicroServicePlatform;
import ru.cristalix.core.microservice.MicroserviceBootstrap;
import ru.cristalix.core.network.Capability;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.permissions.PermissionService;

import java.util.stream.Collectors;

/**
 * @author Рейдж 03.10.2021
 * @project BridgeBuilders
 */
public class BridgeService {

    public static void main(String[] args) {

        MicroserviceBootstrap.bootstrap(new MicroServicePlatform(4));

        MongoAdapter mongoAdapter = new MongoAdapter(System.getenv("db_url"), System.getenv("db_data"), "me/reidj/bridgebuilders/data");

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
                        .build(),
                Capability.builder()
                        .className(ResetRejoin.class.getName())
                        .notification(true)
                        .build(),
                Capability.builder()
                        .className(RejoinPackage.class.getName())
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
        socketClient.addListener(ResetRejoin.class, ((channel, pckg) -> mongoAdapter.find(pckg.getUuid()).thenAccept(stat -> {
            stat.setRealm("");
            pckg.setStat(stat);
            mongoAdapter.save(stat);
        })));
        //socketClient.addListener(RejoinPackage.class, ((channel, pckg) -> broadcast(pckg)));
    }
}
