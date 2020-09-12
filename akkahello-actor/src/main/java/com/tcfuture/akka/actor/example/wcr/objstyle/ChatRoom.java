package com.tcfuture.akka.actor.example.wcr.objstyle;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liulv
 */
public class ChatRoom {

    /**
     * 聊天命令
     */
    static interface RoomCommand {}

    /**
     * 获取会话: 屏幕名称-客户端ActorRef<SessionEvent>
     */
    public static final class GetSession implements RoomCommand {
        public final String screenName;
        public final ActorRef<SessionEvent> replyTo;

        public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
            this.screenName = screenName;
            this.replyTo = replyTo;
        }
    }

    /**
     * 会话事件
     */
    static interface SessionEvent {}

    /**
     * 会话协议
     */
    public static final class SessionGranted implements SessionEvent {
        public final ActorRef<PostMessage> handle;

        public SessionGranted(ActorRef<PostMessage> handle) {
            this.handle = handle;
        }
    }

    /**
     * 会话拒绝：reason理由
     */
    public static final class SessionDenied implements SessionEvent {
        public final String reason;

        public SessionDenied(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 发帖消息
     */
    public static final class MessagePosted implements SessionEvent {
        public final String screenName;
        public final String message;

        public MessagePosted(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    static interface SessionCommand {}

    /**
     * 帖子消息
     */
    public static final class PostMessage implements SessionCommand {
        public final String message;

        public PostMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 通告客户端
     */
    private static final class NotifyClient implements SessionCommand {
        final MessagePosted message;

        NotifyClient(MessagePosted message) {
            this.message = message;
        }
    }

    /**
     * 发布会话消息
     */
    private static final class PublishSessionMessage implements RoomCommand {
        public final String screenName;
        public final String message;

        public PublishSessionMessage(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    public static Behavior<RoomCommand> create() {
        return Behaviors.setup(ChatRoomBehavior::new);
    }

    /**
     * 聊天室行为（聊天室Actor）
     */
    public static class ChatRoomBehavior extends AbstractBehavior<RoomCommand> {
        final List<ActorRef<SessionCommand>> sessions = new ArrayList<>();

        private ChatRoomBehavior(ActorContext<RoomCommand> context) {
            super(context);
        }

        @Override
        public Receive<RoomCommand> createReceive() {
            ReceiveBuilder<RoomCommand> builder = newReceiveBuilder();

            builder.onMessage(GetSession.class, this::onGetSession);
            builder.onMessage(PublishSessionMessage.class, this::onPublishSessionMessage);

            return builder.build();
        }

        /**
         * 第二步：收到GetSession消息后回复
         * 1. 获取消息中的客户端Gabbler Actor
         * 2. 创建一个SessionBehavior Actor,包含room:信息房间为getContext().getSelf(), 聊天屏名， 客户端
         * 3. 发送SessionGranted<第二个步缩小版的Actor>消息给Gabbler（客户端），并且把当前回话添加到
         * List<ActorRef<SessionCommand>>
         *
         * @param getSession GetSession 接收到的消息
         * @return Behavior<RoomCommand>
         * @throws UnsupportedEncodingException
         */
        private Behavior<RoomCommand> onGetSession(GetSession getSession)
                throws UnsupportedEncodingException {
            ActorRef<SessionEvent> client = getSession.replyTo;
            ActorRef<SessionCommand> ses = getContext().spawn(
 SessionBehavior.create(getContext().getSelf(), getSession.screenName, client),
 URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
            // narrow to only expose PostMessage
            client.tell(new SessionGranted(ses.narrow()));
            sessions.add(ses);
            return this;
        }

        /**
         * 第五步：接收到PublishSessionMessage消息后行为
         *  1. 根据创建MessagePosted对象实例创建NotifyClient
         *  2. 循环sessions消息ActorRef<SessionCommand>，发布NotifyClient给多个
         *  客户端ActorRef<SessionCommand>
         *
         * @param pub
         * @return
         */
        private Behavior<RoomCommand> onPublishSessionMessage(PublishSessionMessage pub) {
            NotifyClient notification =
                    new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
            sessions.forEach(s -> s.tell(notification));
            return this;
        }
    }

    /**
     * 会话行为（Session Actor）
     */
    static class SessionBehavior extends AbstractBehavior<ChatRoom.SessionCommand> {
        private final ActorRef<RoomCommand> room;
        private final String screenName;
        private final ActorRef<SessionEvent> client;

        public static Behavior<ChatRoom.SessionCommand> create(
                ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
            return Behaviors.setup(context -> new SessionBehavior(context, room, screenName, client));
        }

        private SessionBehavior(
                ActorContext<ChatRoom.SessionCommand> context,
                ActorRef<RoomCommand> room,
                String screenName,
                ActorRef<SessionEvent> client) {
            super(context);
            this.room = room;
            this.screenName = screenName;
            this.client = client;
        }

        @Override
        public Receive<SessionCommand> createReceive() {
            return newReceiveBuilder()
                    .onMessage(PostMessage.class, this::onPostMessage)
                    .onMessage(NotifyClient.class, this::onNotifyClient)
                    .build();
        }

        /**
         * 第四步：接收到客户端发送的PostMessage消息后回复
         * 1. 给当前房间room发送消息PublishSessionMessage，包含屏名字，和接收到的消息字符
         *
         * @param post PostMessage
         * @return
         */
        private Behavior<SessionCommand> onPostMessage(PostMessage post) {
            // from client, publish to others via the room
            room.tell(new PublishSessionMessage(screenName, post.message));
            return Behaviors.same();
        }

        /**
         * 第六步：接收到Session行为发送的NotifyClient消息后，回复
         * 发送notification.message消息内容MessagePosted给gabble
         *
         * @param notification NotifyClient消息
         * @return
         */
        private Behavior<SessionCommand> onNotifyClient(NotifyClient notification) {
            // published from the room
            client.tell(notification.message);
            return Behaviors.same();
        }
    }
}
