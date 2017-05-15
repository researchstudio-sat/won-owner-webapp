/*
 * Copyright 2017  Research Studios Austria Forschungsges.m.b.H.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package won.bot.framework.eventbot.behaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.filter.EventFilter;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.eventbot.listener.impl.ActionOnEventListener;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BotBehaviour {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicBoolean active = new AtomicBoolean(false);
    protected final String name;
    protected final EventListenerContext context;
    protected Set<EventListener> activeListeners;
    protected Set<CoordinationBehaviour> coordinationBehaviours;

    public BotBehaviour(EventListenerContext context) {
        this.context = context;
        this.name = this.getClass().getName();
        this.activeListeners = Collections.synchronizedSet(new HashSet<>());
        this.coordinationBehaviours = Collections.synchronizedSet(new HashSet<>());
    }

    public BotBehaviour(EventListenerContext context, String name) {
        this.name = name;
        this.context = context;
        this.activeListeners = Collections.synchronizedSet(new HashSet<>());
        this.coordinationBehaviours = Collections.synchronizedSet(new HashSet<>());
    }

    public void activateAfterDeactivate(BotBehaviour... nextBehaviours) {
        Arrays.stream(nextBehaviours).forEach(botBehaviour -> {
            CoordinationBehaviour c = CoordinationBehaviour.connectDeactivateActivate(context, this, botBehaviour);
            this.coordinationBehaviours.add(c);
        });
    }

    public void activateWithActivate(BotBehaviour... nextBehaviours) {
        Arrays.stream(nextBehaviours).forEach(botBehaviour -> {
            CoordinationBehaviour c = CoordinationBehaviour.connectActivateActivate(context, this, botBehaviour);
            this.coordinationBehaviours.add(c);
        });
    }

    public void deactivateAfterDeactivate(BotBehaviour... nextBehaviours) {
        Arrays.stream(nextBehaviours).forEach(botBehaviour -> {
            CoordinationBehaviour c = CoordinationBehaviour.connectDeactivateDeactivate(context, this, botBehaviour);
            this.coordinationBehaviours.add(c);
        });
    }

    public void deactivateAfterActivate(BotBehaviour... nextBehaviours) {
        Arrays.stream(nextBehaviours).forEach(botBehaviour -> {
            CoordinationBehaviour c = CoordinationBehaviour.connectDeactivateActivate(context, this, botBehaviour);
            this.coordinationBehaviours.add(c);
        });
    }

    /**
     * Activates the behaviour by registering listeners. If the onActivate method of the subclass throws an Exception,
     * the deactivate method is called.
     */
    public final synchronized void activate() {
        coordinationBehaviours.stream().forEach(b -> b.activate());
        if (active.get()) {
            cleanup();
        }
        ;
        try {
            onActivate();
            active.set(true);
            context.getEventBus().publish(new BotBehaviourActivatedEvent(this));
        } catch (Exception e) {
            logger.warn("could not activate {}, caught Exception", name, e);
            logger.debug("deactivating {}", name);
            cleanup();
            context.getEventBus().publish(new BotBehaviourFailedEvent(this, e));
        }

    }

    /**
     * Deactivates the behaviour. Automatically unsubscribes every EventListener Instance that has been created.
     * No traces of it must be left in the Event Bus after this method has finished.
     */
    public final synchronized void deactivate() {
        try {
            cleanup();
            context.getEventBus().publish(new BotBehaviourDeactivatedEvent(this));
        } catch (Exception e) {
            logger.warn("could not deactivate {}, caught Exception", name, e);
        }
    }

    private final synchronized void cleanup() {
        onCleanup();
        for (EventListener eventListener : activeListeners) {
            context.getEventBus().unsubscribe(eventListener);
        }
        activeListeners.clear();
        active.set(false);
    }

    /**
     * Cleans up the coordination behaviours, to be used in case of an error in the behaviour (because it won't terminate correctly).
     */
    private final synchronized void cleanupCoordiationBehaviours(){
        coordinationBehaviours.stream().forEach(b -> b.deactivate());
    }

    /**
     * Subscribes an Event/EventListener Pair, that will be automatically cleaned within the deactivate process of this
     * behaviour.
     */
    protected <T extends Event> void subscribeWithAutoCleanup(Class<T> eventClazz, EventListener listener) {
        activeListeners.add(listener);
        context.getEventBus().subscribe(eventClazz, listener);
    }

    /**
     * Deactivates the behaviour. No traces of it must be left in the Event Bus after this method has finished.
     * No Implementation required if EventListeners are added with the subscribeWithAutoCleanup method.
     */
    protected void onCleanup() {

    }

    ;

    /**
     * Activates the behaviour by registering listeners, if you addListeners with the subscribeWithAutoCleanup method, you
     * do not have to clean them within the onCleanup method.
     */
    protected abstract void onActivate();

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active.get();
    }
}
