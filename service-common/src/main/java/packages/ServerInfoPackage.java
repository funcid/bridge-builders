package packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import user.Stat;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class ServerInfoPackage extends BridgePackage {

    // request
    private final UUID uuid;

    // response
    private Stat stat;
}
