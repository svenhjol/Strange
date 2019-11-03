package svenhjol.strange.traveljournal.proxy;

import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import svenhjol.strange.traveljournal.Entry;

import java.util.function.Consumer;

public interface ITravelJournalProxy
{
    void openTravelJournal(Hand hand);

    void takeScreenshot(String id, Consumer<ITextComponent> onFinish);

    void handleAddEntry(Hand hand, Entry entry);

    void handleScreenshot(Hand hand, Entry entry);
}
