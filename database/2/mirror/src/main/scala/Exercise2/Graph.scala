package com.exercise2
import com.exercise2.monsters.Monster
import com.exercise2.skills.Skill
import org.apache.spark.sql.{Dataset, RelationalGroupedDataset, SparkSession}
import org.apache.spark.sql.functions.{array, collect_list, collect_set, concat_ws, sum}

import scala.reflect.ClassTag
import scala.collection.JavaConverters._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe._
import io.circe.generic.semiauto._
import com.exercise2.WebServices
import org.apache.spark.broadcast.Broadcast

import scala.math._
import com.exercise2.ai.AI

import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets


//-------------------------------------------------------------------------------------------
//Graph representing battle

class BattleGraph() extends Serializable {

  val debug = false

  //Spark session
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._

  //Graph
  var vertices = Seq[(Int, Monster)]().toDS()
  var edges = Seq[(Int, Seq[(Int, Int, Int, Int, Int)])]().toDS()

  //Battle data
  var turn:Int = 0
  var cid:Int = 0
  var ended:Boolean = false

  //Add a new monster to battle
  def add(v:Monster):Unit = {
    v.set("id", cid)
    val vertex = Seq[(Int, Monster)]((cid, v)).toDS()
    vertices = vertices.union(vertex)
    cid += 1
  }

  //Compute next turn
  def next():Unit = {
    turn += 1
    if (ended) return
    println(s"== Turn ${"%4d".format(turn)} ========================================")
    val monsters = spark.sparkContext.broadcast(vertices.collect().toMap)

    //Compute edges
    val ids = vertices.map{case (id, monster) => (id)}.collect()
    var links = Seq[(Int, Int, Int, Int, Int)]()
    ids.foreach(ida => {
      ids.foreach(idb => {
        val a = monsters.value(ida)
        val b = monsters.value(idb)
        val team = if (a.get("team") == b.get("team")) 0 else 1
        val distance = sqrt(pow(b.get("x") - a.get("x"), 2) + pow(b.get("y") - a.get("y"), 2) + pow(b.get("z") - a.get("z"), 2))    
        //Ennemies doesn't need to be connected each other
        if (!((team == 0)&&(b.get("team") > 1))) {
          val hp = (100*b.get("hp")/b.get("hpm")).toInt
          //If opponent, can move towards
          if ((team == 1)&&(ida != idb))
            links = links ++ Seq((ida, idb, team, 0, hp))
          //Melee
          if ((distance < 10)&&(ida != idb))
            links = links ++ Seq((ida, idb, team, 1, hp))
          //Ranged
          if ((distance < 100)&&(ida != idb))
            links = links ++ Seq((ida, idb, team, 2, hp))
        }
      })
    })
    edges = links.toDS()
      .groupBy("_1")
      .agg(collect_list(array("_1", "_2", "_3", "_4", "_5")))
      .as[(Int, Seq[Seq[Int]])]
      .map{case (id, edge) => (id, edge.map(e => (e(0), e(1), e(2), e(3), e(4))))}   

    //Compute vertices
    val neighbors = spark.sparkContext.broadcast(edges.collect().toMap)
    vertices = vertices
      //Compute actions decided by each monster
      .map{case (id, monster) => {
        val m = monster
        m.actions = AI.compute(m, neighbors.value(id))
        (id, m)
      }}
      //Compute differences depending on each individual monster's actions
      .flatMap{case (id, monster) => {
        val computed = Seq((id, "hp", monster.get("regen"))) ++ monster.actions.flatMap{case (target, skill) => Skill.execute(id, monster, skill, target, monsters.value(target))}
        computed
      }}
      //Merge differences
      .groupBy("_1", "_2")
      .agg(sum("_3").alias("_3"))
      .groupBy("_1")
      .agg(collect_list(array("_2", "_3")).alias("_d"))
      .as[(Int, Seq[Seq[String]])]
      //Apply differences
      .map{case (id, diffs) => {
        val m = monsters.value(id)
        m.actions = Seq()
        diffs
          .map(diff => { (diff(0), diff(1).toInt)})
          .foreach{case (k, v) => m.set(k, m.get(k) + v)}
        m.set("hp", min(m.get("hp"), m.get("hpm")))
        (id, m)
      }}
      //Filter monsters by hp
      .filter(m => {
        if ((debug)&&(m._2.get("hp") <= 0)) println(s"${m._2.name} (${m._1}) is ko")
        m._2.get("hp") > 0
      })
      .localCheckpoint()
      .cache()

      val teams = Set(vertices.map(m => m._2.get("team")).collect():_*)
      if (teams.size <= 1) {
        println(s"Team ${teams.head} has won the battle")
        ended = true
      }

  }
  
  //Print current state
  def print():Unit = {
    vertices.show(false)
  }

  //Send to websockets
  def send():Unit = {
    import spark.implicits._
    val json = vertices.map(r => r._2).collectAsList().asScala.toList.asJson.noSpaces
    WebServices.send(json)
    println(s"sent data from turn ${turn}")
  }

  //Save file
  def save(file:String):Unit = {
    import spark.implicits._
    val json = vertices.map(r => r._2).collectAsList().asScala.toList.asJson.noSpaces
    val path = Paths.get(s"src/resources/www/Exercice2/battles/${file}")
    try {
      Files.write(path, s"${json}\n".getBytes(StandardCharsets.UTF_8), if (Files.exists(path)) StandardOpenOption.APPEND else StandardOpenOption.CREATE)
      println(s"saved data from turn ${turn}")
    } catch {
      case _: Throwable => println(s"failed to save data from turn ${turn}")
    }  
  }

}

