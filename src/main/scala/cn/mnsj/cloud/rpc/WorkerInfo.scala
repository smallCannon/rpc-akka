package cn.mnsj.cloud.rpc

class WorkerInfo(val wokerId:String,val memery:String,val Cores:String) {
  // 下划线表示初始化
    var lastHeartBeatTime:Long=_

  override def toString: String = {
      s"workerId:$wokerId memory:$memery Cores:$Cores"
  }
}
