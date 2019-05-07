//Imports and packages
package com.exercise1
import java.io._
import java.nio.file.Paths
import java.nio.file.Files

//-------------------------------------------------------------------------------------------
//Global name space
package object Global {
  //List all files in a directories
  def directory(path:String): List[String] = (new File(path)).listFiles.filter(_.isDirectory).map(_.getName).toList

  //Clean directories of its content
  def clean(path:String): Unit = clean(new File(path))
  def clean(file:File, delete:Boolean = false):Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(clean(_, true))
    if (delete && file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  //Test if file exists
  def exists(path:String):Boolean = Files.exists(Paths.get(path))

}