package packages;

import lombok.Getter;

import java.util.UUID;

/**
 * @author Рейдж 03.10.2021
 * @project ThePit
 */

@Getter
public abstract class BridgePackage {

    private final String id = UUID.randomUUID().toString();
}
