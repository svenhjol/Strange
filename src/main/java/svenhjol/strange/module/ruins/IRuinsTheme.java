package svenhjol.strange.module.ruins;

import svenhjol.strange.module.structures.BaseStructure;

import java.util.List;

public interface IRuinsTheme {
    void register();

    void runWhenEnabled();

    List<BaseStructure> getBuilds();
}
