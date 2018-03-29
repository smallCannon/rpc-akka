package cn.mnsj.cloud.rpc

trait RemoteMessage extends Serializable

//注册信息
case class RegisterMessage(val workerId:String, val memory:String, val cores:String) extends RemoteMessage

// 注册成功返回信息
case class RegistedMessage(val msg:String) extends RemoteMessage

// 发送心跳
case class SendHeartBeat(val workerId:String) extends RemoteMessage

case class HeartBeat()

// 心跳检查时间
case class CheckOutTime()
