package packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.network.CorePackage;
import tops.PlayerTopEntry;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class TopPackage extends CorePackage {

    // request
    private final TopType topType;
    private final int limit;

    // response
    private List<PlayerTopEntry<Object>> entries;

    public enum TopType {

        WINS,
        GAMES
        ;

    }

}