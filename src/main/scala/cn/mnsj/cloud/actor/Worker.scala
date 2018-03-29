package cn.mnsj.cloud.actor

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * master与worker之间进行通信
  * 先启动Master
  *
  *
  */
class Worker extends Actor{
  println("woker actor invoked")

  val worker: ActorSelection = context.actorSelection("akka.tcp://masterActorSystem@192.168.92.30:8888/user/masterActor")
  worker ! "connect"

  override def receive: Receive = {
    case "conntect"=>{
        println("a clinet connected")
    }

    case "success" =>{
      println("注册成功")
    }
  }
}

object Worker{
  def main(args: Array[String]): Unit = {
    // 定义worker的IP地址
    val host="192.168.92.30"
    //定义worker的端口
    val port="6666"

    // 准备配置文件信息
    val configStr=
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
         |
      """.stripMargin

    // 利用ConfigFactory解析配置文件，获取配置信息
    val config = ConfigFactory.parseString(configStr)

    // 1. 创建ActorSystem 他是整个进程中老大 它负责创建和监督actor 单例
    val masterActorSystem = ActorSystem("workerActorSystem",config)
    // 2. 通过ActorSystem创建master actor
    val masterActor:ActorRef = masterActorSystem.actorOf(Props(new Worker),"workerActor")

    masterActor ! "connect"
  }
}
