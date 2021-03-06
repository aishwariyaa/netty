/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2.draft10.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.draft10.Http2Exception;

import java.util.List;

public interface Http2Connection {

    /**
     * A view of the connection from one endpoint (local or remote).
     */
    interface Endpoint {

        /**
         * Creates a stream initiated by this endpoint and notifies all listeners. This could fail for
         * the following reasons:
         * <p/>
         * - The requested stream ID is not the next sequential ID for this endpoint. <br>
         * - The stream already exists. <br>
         * - The number of concurrent streams is above the allowed threshold for this endpoint. <br>
         * - The connection is marked as going away}. <br>
         * - The provided priority is < 0.
         *
         * @param streamId   The ID of the stream
         * @param priority   the priority of the stream
         * @param halfClosed if true, the stream is created in the half-closed state with respect to
         *                   this endpoint. Otherwise it's created in the open state.
         */
        Http2Stream createStream(int streamId, int priority, boolean halfClosed) throws Http2Exception;

        /**
         * Creates a push stream in the reserved state for this endpoint and notifies all listeners.
         * This could fail for the following reasons:
         * <p/>
         * - Server push is not allowed to the opposite endpoint. <br>
         * - The requested stream ID is not the next sequential stream ID for this endpoint. <br>
         * - The number of concurrent streams is above the allowed threshold for this endpoint. <br>
         * - The connection is marked as going away. <br>
         * - The parent stream ID does not exist or is not open from the side sending the push promise.
         * <br>
         * - Could not set a valid priority for the new stream.
         *
         * @param streamId the ID of the push stream
         * @param parent   the parent stream used to initiate the push stream.
         */
        Http2Stream reservePushStream(int streamId, Http2Stream parent) throws Http2Exception;

        /**
         * Sets whether server push is allowed to this endpoint.
         */
        void setPushToAllowed(boolean allow);

        /**
         * Gets whether or not server push is allowed to this endpoint.
         */
        boolean isPushToAllowed();

        /**
         * Gets the maximum number of concurrent streams allowed by this endpoint.
         */
        int getMaxStreams();

        /**
         * Sets the maximum number of concurrent streams allowed by this endpoint.
         */
        void setMaxStreams(int maxStreams);

        /**
         * Gets the ID of the stream last successfully created by this endpoint.
         */
        int getLastStreamCreated();

        /**
         * Gets the {@link Endpoint} opposite this one.
         */
        Endpoint opposite();
    }

    /**
     * A listener of the connection for stream events.
     */
    interface Listener {
        /**
         * Called when a new stream with the given ID is created.
         */
        void streamCreated(int streamId);

        /**
         * Called when the stream with the given ID is closed.
         */
        void streamClosed(int streamId);
    }

    /**
     * Adds a listener of this connection.
     */
    void addListener(Listener listener);

    /**
     * Removes a listener of this connection.
     */
    void removeListener(Listener listener);

    /**
     * Attempts to get the stream for the given ID. If it doesn't exist, throws.
     */
    Http2Stream getStreamOrFail(int streamId) throws Http2Exception;

    /**
     * Gets the stream if it exists. If not, returns {@code null}.
     */
    Http2Stream getStream(int streamId);

    /**
     * Gets all streams that are currently either open or half-closed. The returned collection is
     * sorted by priority.
     */
    List<Http2Stream> getActiveStreams();

    /**
     * Gets a view of this connection from the local {@link Endpoint}.
     */
    Endpoint local();

    /**
     * Gets a view of this connection from the remote {@link Endpoint}.
     */
    Endpoint remote();

    /**
     * Marks that a GoAway frame has been sent on this connection. After calling this, both
     * {@link #isGoAwaySent()} and {@link #isGoAway()} will be {@code true}.
     */
    void sendGoAway(ChannelHandlerContext ctx, ChannelPromise promise, Http2Exception cause);

    /**
     * Marks that a GoAway frame has been received on this connection. After calling this, both
     * {@link #isGoAwayReceived()} and {@link #isGoAway()} will be {@code true}.
     */
    void goAwayReceived();

    /**
     * Indicates that this connection received a GoAway message.
     */
    boolean isGoAwaySent();

    /**
     * Indicates that this connection send a GoAway message.
     */
    boolean isGoAwayReceived();

    /**
     * Indicates whether or not this endpoint is going away. This is a short form for
     * {@link #isGoAwaySent()} || {@link #isGoAwayReceived()}.
     */
    boolean isGoAway();
}
