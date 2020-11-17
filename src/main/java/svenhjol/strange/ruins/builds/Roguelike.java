package svenhjol.strange.ruins.builds;

import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class Roguelike extends BaseStructure {
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> SIDES = new HashMap<>();
    private final Map<String, Integer> TEST = new HashMap<>();

    public Roguelike() {
        super(Strange.MOD_ID, "ruins", "roguelike");

        addStart("start1", 1);

        registerPool("ends", ENDS);
    }
}
