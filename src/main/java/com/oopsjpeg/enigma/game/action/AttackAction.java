package com.oopsjpeg.enigma.game.action;

import com.oopsjpeg.enigma.game.GameAction;
import com.oopsjpeg.enigma.game.GameMember;
import com.oopsjpeg.enigma.util.Emote;

public class AttackAction implements GameAction {
    private final GameMember target;

    public AttackAction(GameMember target) {
        this.target = target;
    }

    public GameMember getTarget() {
        return target;
    }

    @Override
    public String act(GameMember actor) {
        return actor.damage(actor.attack(target), Emote.ATTACK);
    }

    @Override
    public int getEnergy() {
        return 50;
    }
}