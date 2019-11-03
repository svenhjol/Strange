package svenhjol.strange.traveljournal.proxy;

import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import svenhjol.strange.traveljournal.Entry;

import java.util.function.Consumer;

public class TravelJournalServerProxy implements ITravelJournalProxy
{

    @Override
    public void openTravelJournal(Hand hand)
    {

    }

    @Override
    public void takeScreenshot(String id, Consumer<ITextComponent> onFinish)
    {

    }

    @Override
    public void handleAddEntry(Hand hand, Entry entry)
    {

    }

    @Override
    public void handleScreenshot(Hand hand, Entry entry)
    {

    }
}
