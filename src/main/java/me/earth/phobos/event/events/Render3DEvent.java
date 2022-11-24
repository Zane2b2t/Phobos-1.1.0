package me.earth.phobos.event.events;

import me.earth.phobos.event.EventStage;

public class Render3DEvent extends EventStage {

    private float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
