package pw.aru.libs.andeclient.events;

import pw.aru.libs.andeclient.events.node.NewNodeEvent;
import pw.aru.libs.andeclient.events.node.NodeConnectedEvent;
import pw.aru.libs.andeclient.events.node.NodeRemovedEvent;
import pw.aru.libs.andeclient.events.player.*;
import pw.aru.libs.andeclient.events.track.TrackEndEvent;
import pw.aru.libs.andeclient.events.track.TrackExceptionEvent;
import pw.aru.libs.andeclient.events.track.TrackStartEvent;
import pw.aru.libs.andeclient.events.track.TrackStuckEvent;

@SuppressWarnings({"unused"})
public class EventType<T extends AndeClientEvent> {

    public static final EventType<PlayerRemovedEvent> PLAYER_REMOVED_EVENT = new EventType<>();
    public static final EventType<NewNodeEvent> NEW_NODE_EVENT = new EventType<>();
    public static final EventType<NodeConnectedEvent> NODE_CONNECTED_EVENT = new EventType<>();
    public static final EventType<NodeRemovedEvent> NODE_REMOVED_EVENT = new EventType<>();
    public static final EventType<NewPlayerEvent> NEW_PLAYER_EVENT = new EventType<>();
    public static final EventType<TrackStartEvent> TRACK_START_EVENT = new EventType<>();
    public static final EventType<PlayerPauseEvent> PLAYER_PAUSE_EVENT = new EventType<>();
    public static final EventType<PlayerResumeEvent> PLAYER_RESUME_EVENT = new EventType<>();
    public static final EventType<TrackEndEvent> TRACK_END_EVENT = new EventType<>();
    public static final EventType<TrackStuckEvent> TRACK_STUCK_EVENT = new EventType<>();
    public static final EventType<TrackExceptionEvent> TRACK_EXCEPTION_EVENT = new EventType<>();
    public static final EventType<PlayerUpdateEvent> PLAYER_UPDATE_EVENT = new EventType<>();
    public static final EventType<WebSocketClosedEvent> WEB_SOCKET_CLOSED_EVENT = new EventType<>();

    private EventType() {}
}
