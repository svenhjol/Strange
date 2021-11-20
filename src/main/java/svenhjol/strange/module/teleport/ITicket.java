package svenhjol.strange.module.teleport;

public interface ITicket {
    void tick();

    boolean isValid();

    boolean isSuccess();

    void onSuccess();

    void onFail();
}
