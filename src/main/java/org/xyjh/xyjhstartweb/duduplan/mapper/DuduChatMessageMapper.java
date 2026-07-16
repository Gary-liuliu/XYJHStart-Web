package org.xyjh.xyjhstartweb.duduplan.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xyjh.xyjhstartweb.duduplan.entity.DuduChatMessage;

import java.util.List;

@Mapper
public interface DuduChatMessageMapper {
    @Insert("""
            INSERT INTO dudu_chat_message
              (message_id, sender_role, receiver_role, message_type, content, reply_to_message_id,
               client_created_at, server_created_at, delivered_at, read_at, recalled_at)
            VALUES
              (#{messageId}, #{senderRole}, #{receiverRole}, #{messageType}, #{content}, #{replyToMessageId},
               #{clientCreatedAt}, #{serverCreatedAt}, #{deliveredAt}, #{readAt}, #{recalledAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DuduChatMessage message);

    @Select("SELECT * FROM dudu_chat_message WHERE message_id = #{messageId} LIMIT 1")
    DuduChatMessage findByMessageId(String messageId);

    @Select("""
            <script>
            SELECT * FROM dudu_chat_message
            WHERE (sender_role = #{role} OR receiver_role = #{role})
            <if test='beforeId != null'>AND id &lt; #{beforeId}</if>
            ORDER BY id DESC
            LIMIT #{limit}
            </script>
            """)
    List<DuduChatMessage> findHistory(@Param("role") String role, @Param("beforeId") Long beforeId,
                                      @Param("limit") int limit);

    @Select("""
            SELECT COUNT(*) FROM dudu_chat_message
            WHERE receiver_role = #{role} AND read_at IS NULL AND recalled_at IS NULL
            """)
    long countUnread(String role);

    @Update("""
            <script>
            UPDATE dudu_chat_message SET delivered_at = COALESCE(delivered_at, #{deliveredAt})
            WHERE receiver_role = #{receiverRole} AND recalled_at IS NULL
              AND message_id IN
              <foreach collection='messageIds' item='messageId' open='(' separator=',' close=')'>
                #{messageId}
              </foreach>
            </script>
            """)
    int markDelivered(@Param("receiverRole") String receiverRole, @Param("messageIds") List<String> messageIds,
                      @Param("deliveredAt") long deliveredAt);

    @Select("""
            <script>
            SELECT * FROM dudu_chat_message WHERE message_id IN
            <foreach collection='messageIds' item='messageId' open='(' separator=',' close=')'>
              #{messageId}
            </foreach>
            ORDER BY id ASC
            </script>
            """)
    List<DuduChatMessage> findByMessageIds(@Param("messageIds") List<String> messageIds);

    @Update("""
            UPDATE dudu_chat_message
            SET delivered_at = COALESCE(delivered_at, #{readAt}), read_at = COALESCE(read_at, #{readAt})
            WHERE receiver_role = #{receiverRole} AND id &lt;= #{upToId} AND recalled_at IS NULL
            """)
    int markRead(@Param("receiverRole") String receiverRole, @Param("upToId") long upToId,
                 @Param("readAt") long readAt);

    @Update("""
            UPDATE dudu_chat_message SET recalled_at = #{recalledAt}
            WHERE message_id = #{messageId} AND sender_role = #{senderRole}
              AND recalled_at IS NULL AND server_created_at &gt;= #{earliestServerCreatedAt}
            """)
    int recall(@Param("messageId") String messageId, @Param("senderRole") String senderRole,
               @Param("recalledAt") long recalledAt, @Param("earliestServerCreatedAt") long earliestServerCreatedAt);
}
