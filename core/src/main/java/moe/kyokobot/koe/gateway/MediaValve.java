package moe.kyokobot.koe.gateway;

import moe.kyokobot.koe.internal.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
class MediaValve {
    private static final Logger LOG = LoggerFactory.getLogger(MediaValve.class.getName());

    private final AbstractMediaGatewayConnection gatewayConnection;

    /**
     * A map of `user_id->streams[*].ssrc`
     */
    private final Map<String, int[]> unwantedSsrcs = new HashMap<>();

    MediaValve(AbstractMediaGatewayConnection gatewayConnection) {
        this.gatewayConnection = gatewayConnection;
    }

    /**
     * Send a {@link Op#MEDIA_SINK_WANTS} payload to the gateway.
     */
    public void send() {
        JsonObject d = new JsonObject();

        // disable all incoming audio streams.
        d.add("any", 0);

        // add any unwanted SSRCs.
        for (int[] streams : unwantedSsrcs.values()) {
            for (int ssrc : streams) {
                d.add(Integer.toString(ssrc), 0);
            }
        }

        this.gatewayConnection.sendInternalPayload(Op.MEDIA_SINK_WANTS, d);
    }

    /**
     * Handle a voice gateway message.
     */
    public void handle(JsonObject obj) {
        int op = obj.getInt("op");
        JsonObject d = obj.getObject("d");
        String userId = d.getString("user_id");

        if (op == Op.CLIENT_CONNECT) {
            // if `video_ssrc` is 0, it indicates that the user is not showing their camera.
            if (d.getInt("video_ssrc") == 0) {
                this.removeUser(userId);
                return;
            }

            // we can skip `audio_ssrc` since "any":0 covers audio.
            // instead, all ssrcs listed in "streams".
            int[] ssrcs = d.getArray("streams").stream()
                    .filter(s -> s instanceof JsonObject)
                    .mapToInt((stream) -> ((JsonObject) stream).getInt("ssrc"))
                    .toArray();

            LOG.debug("Received streams for user {}: {}", d, Arrays.toString(ssrcs));

            this.unwantedSsrcs.put(userId, ssrcs);
        } else if (op == Op.CLIENT_DISCONNECT) {
            this.removeUser(userId);
        }
    }

    void removeUser(String userId) {
        LOG.debug("Removing streams for user {}", userId);
        this.unwantedSsrcs.remove(userId);
    }
}
