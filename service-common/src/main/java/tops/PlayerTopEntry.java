package tops;

import lombok.Getter;
import lombok.Setter;
import user.Stat;

@Setter
@Getter
public class PlayerTopEntry<V> extends TopEntry<Stat, V> {

    private String userName;
    private String displayName;

    public PlayerTopEntry(Stat stat, V value) {
        super(stat, value);
    }

}
