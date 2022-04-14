package bridge.handlers;

import io.netty.channel.Channel;
import packages.BridgePackage;

@FunctionalInterface
public interface PackageHandler<T extends BridgePackage> {

	void handle(Channel channel, String serverName, T thePitPackage);

}
