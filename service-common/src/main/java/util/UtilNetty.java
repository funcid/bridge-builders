package util;

import com.google.gson.Gson;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import lombok.val;
import packages.BridgePackage;
import packages.PackageWrapper;

/**
 * @author Рейдж 03.10.2021
 * @project ThePit
 */
public class UtilNetty {

    private static final Gson gson = new Gson();

    public static TextWebSocketFrame toFrame(BridgePackage thePitPackage) {
        return new TextWebSocketFrame(gson.toJson(new PackageWrapper(thePitPackage.getClass().getName(), gson.toJson(thePitPackage))));
    }

    @SneakyThrows
    public static BridgePackage readFrame(TextWebSocketFrame textFrame) {
        PackageWrapper wrapper = gson.fromJson(textFrame.text(), PackageWrapper.class);
        return (BridgePackage) gson.fromJson(wrapper.getObjectData(), Class.forName(wrapper.getClazz()));
    }

}
