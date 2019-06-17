import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.channel.*
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets

class ServerHandler : SimpleChannelHandler() {

    private val logger = LoggerFactory.getLogger(ServerHandler::class.java)
    
    override fun messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
        val message = getStringFromBuffer(e.message as ChannelBuffer)
        logger.info("Получено сообщение: $message")

        val parsedMessage = message.split("/d/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (string in parsedMessage) {
            if (string != "");
            ServerMessage.read(string, e.channel)
        }
    }

    private fun getStringFromBuffer(buffer: ChannelBuffer): String {
        val bufSize = buffer.readableBytes()
        val byteBuffer = ByteArray(bufSize)
        buffer.readBytes(byteBuffer)
        return String(byteBuffer, StandardCharsets.UTF_8)
    }

    @Throws(Exception::class)
    override fun channelClosed(ctx: ChannelHandlerContext, e: ChannelStateEvent) {
        super.channelClosed(ctx, e)
        logger.info("Канал закрылся.")
        ServerMethods.disconnectReceived(e.channel)
    }

    @Throws(Exception::class)
    override fun writeRequested(ctx: ChannelHandlerContext, e: MessageEvent) {
        // /d/ выступает в качестве разделителя между сообщениями
        val message = e.message.toString() + "/d/"
        logger.info("Отправляю сообщение: $message")
        Channels.write(
            ctx,
            e.future,
            ChannelBuffers.wrappedBuffer(message.toByteArray(StandardCharsets.UTF_8)),
            e.remoteAddress
        )
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
        logger.info("Cловил Exception.")
        e.cause.printStackTrace()
        //        ServerMethods.disconnectReceived(e.getChannel());
        e.channel.close()
    }
}
