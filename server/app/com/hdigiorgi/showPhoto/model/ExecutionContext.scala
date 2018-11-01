package com.hdigiorgi.showPhoto.model

package object ExecutionContext {
  implicit val executionContext =
    scala.concurrent.ExecutionContext.fromExecutor(java.util.concurrent.Executors.newCachedThreadPool())
}



