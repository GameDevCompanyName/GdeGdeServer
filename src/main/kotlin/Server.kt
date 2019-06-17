import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.Channels
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.slf4j.LoggerFactory

import java.net.InetSocketAddress
import java.util.concurrent.Executors

object Server {
    private val logger = LoggerFactory.getLogger(Server::class.java)

    private val PORT  = 16261
    
    @JvmStatic
    fun main(args: Array<String>) {        
        logger.info("Сервер запускается")
        logger.info("Инициализация DBConnector...")
        DBConnector.initDBConnector()
        logger.info("DBConnector DONE")

        logger.info("ChannelFactory...")
        val factory = NioServerSocketChannelFactory(
            Executors.newFixedThreadPool(1),
            Executors.newFixedThreadPool(4)
        )
        logger.info("ChannelFactory DONE")

        logger.info("Bootstrap...")
        val bootstrap = ServerBootstrap(factory)
        bootstrap.setPipelineFactory { Channels.pipeline(ServerHandler()) }
        logger.info("Bootstrap DONE")

        logger.info("Binding Channel...")
        val channel = bootstrap.bind(InetSocketAddress(PORT))
        logger.info("Binding Channel DONE")

        CommandLine().run()
    }
}