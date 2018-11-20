package com.hdigiorgi.showPhoto.application

import com.hdigiorgi.showPhoto.model.post.RandomPost
import play.api.Play

case class Arguments(args: Array[String]) {
  def apply(index: Integer, name: String): String = get(index, name)
  def getOpt(index: Integer, name: String): Option[String] = {
    if(index >= args.length) None else Some(args(index))
  }
  def get(index: Integer, name: String): String = getOpt(index, name) match {
    case None => throw new RuntimeException(f"argument '$name' wasn't found")
    case Some(value) => value
  }
  def drop(count: Integer): Arguments = {
    if(count > args.length) {
      throw new RuntimeException(f"invalid amount of arguments")
    } else {
      Arguments(args.drop(count))
    }
  }
}

object Main {
  def main(inputArgs: Array[String]): Unit = {
    val args = Arguments(inputArgs)
    val env = Environment.fromString(args(0, "environment"))
    val action = args(1, "action")
    val extraArgs = args.drop(2)
    callAction(env, action, extraArgs)
  }

  private def callAction(env: Environment, action: String, args: Arguments): Unit = action match {
    case "populate" => populate(env, args)
    case "run" => run(env, args)
    case _ =>
      throw new Exception(f"action found: $action")
  }

  private def populate(env: Environment, args: Arguments): Unit = {
    val count = args.get(0, "amount").toInt
    val imageFolder = args.get(1, "default image folder")
    val attachmentFolder = args.get(2, "attachment files folder")
    RandomPost.genAndSave(env.configuration, imageFolder, attachmentFolder, count)
  }

  private def run(env: Environment, args: Arguments): Unit = {
    Play.start(env.application)
  }

}