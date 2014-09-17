package com.stratio.connector

import akka.actor.{ActorRef, ActorSystem}
import akka.routing.RoundRobinRouter
import com.stratio.connectors.config.ConnectConfig
import com.stratio.meta.common.connector.IConnector
import com.typesafe.config.ConfigFactory
import org.apache.log4j.BasicConfigurator

//import com.stratio.connector.cassandra.CassandraConnector

object ConnectorApp extends ConnectorApp{
  def main(args: Array[String]): Unit = {
    BasicConfigurator.configure()
    //if (args.length == 0) println(usage)
    val options = nextOption(Map(),args.toList)
    var connectortype:Option[String]=options.get( Symbol("connectortype"))
    var port:Option[String]=options.get(Symbol("port"))
    if(port==None)port=Some("2551")
    val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())
    if(connectortype==None)connectortype=Some("cassandra")
    val c=getConnector(connectortype.get.asInstanceOf[String])
    startup(c,Seq(port.get.asInstanceOf[String]),config)
  }
}

class ConnectorApp  extends ConnectConfig {

    val usage = """Usage: 
      connectorApp [--port <port number>] [--connectortype <connector type name>] 
    """
    lazy val system = ActorSystem(clusterName, config)

  def getConnector(connectortype:String):IConnector={
    connectortype match {
      //case "cassandra" => new CassandraConnector
      case _ => null //new CassandraConnector
    }
  }

  type OptionMap = Map[Symbol, String]
  def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
       def isSwitch(s : String) = (s(0) == '-')
       list match {
        case Nil => map
        case "--port" :: value :: tail =>
                               nextOption(map ++ Map('port -> value), tail)
        case "--connector-type" :: value :: tail =>
                               nextOption(map ++ Map('connectortype -> value), tail)
        case option :: tail => 
            println("Unknown option "+option) 
        	println(usage)
            exit(1) 
       }
  }

  def startup(connector:IConnector,port:String,config:com.typesafe.config.Config): ActorRef= {
    startup(connector,Array(port),config)
  }

  def startup(connector:IConnector,ports:Array[String],config:com.typesafe.config.Config): ActorRef= {
    startup(connector,ports.toList,config)
  }

  def startup(connector:IConnector,ports: Seq[String],config:com.typesafe.config.Config): ActorRef= {
    var actorClusterNode:ActorRef=null
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())


      // Create an Akka system

      // Create an actor that handles cluster domain events
      //actorClusterNode=system.actorOf(Props[ClusterListener], name = actorName)
      actorClusterNode=system.actorOf(ClusterListener.props(connector.getConnectorName).withRouter(RoundRobinRouter(nrOfInstances=num_connector_actor)),"CoordinatorActor")


        actorClusterNode ! "I'm in!!!"
    }
    actorClusterNode
  }
  def startup(connector:IConnector):ActorRef={
      // Create an Akka system

      // Create an actor that handles cluster domain events
   val  actorClusterNode=system.actorOf(ClusterListener.props(connector.getConnectorName).withRouter(RoundRobinRouter(nrOfInstances=num_connector_actor)),"CoordinatorActor")
    actorClusterNode ! "I'm in!!!"
     actorClusterNode

  }

}