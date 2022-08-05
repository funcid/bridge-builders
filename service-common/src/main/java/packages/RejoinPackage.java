package packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import user.Stat;

import java.util.UUID;

/**
 * @author : Рейдж
 * @project : BridgeBuilders
 **/

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class RejoinPackage extends BridgePackage {

    // request
    private final UUID uuid;
    private final Stat stat;

    // no response
}
