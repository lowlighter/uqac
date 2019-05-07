package com.exercise2.skills
import com.exercise2.monsters.Monster
import scala.math._

//-------------------------------------------------------------------------------------------
//Skills

package object Skill {

  val rand = scala.util.Random

  val debug = true

  //Execute skill effect
  def execute(ida:Int, a:Monster, skill:String, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return Skill.list(skill)(ida, a, idb, b)
  }
  

  val list = Map[String, (Int, Monster, Int, Monster) => Seq[(Int, String, Int)]](
    "move" -> Skill.move, 
    "dancing_greatsword" -> Skill.dancing_greatsword,
    "composite_longbow" -> Skill.composite_longbow,
    "orc_double_axe" -> Skill.orc_double_axe,
    "mwk_composite_longbow" -> Skill.mwk_composite_longbow,
    "mwk_battleaxe" -> Skill.mwk_battleaxe,
    "shortbow" -> Skill.shortbow,
    "vicious_flail" -> Skill.vicious_flail,
    "mwk_throwing_axe" -> Skill.mwk_throwing_axe
  )

  //Move skill
  def move(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    val xa = a.get("x")
    val ya = a.get("y")
    val za = a.get("z")
    val xb = b.get("x")
    val yb = b.get("y")
    val zb = b.get("z")
    var limit = if (a.get("flying") == 1) a.get("fly") else a.get("speed")
    
    val theta = Math.atan2(yb-ya, xb-xa)
    val x = min(abs(xb-xa),limit) * Math.cos(theta)
    val y = min(abs(yb-ya),limit) * Math.sin(theta)
    val z = if (a.get("flying") == 1) min(zb-za,limit) else 0
    if (debug) println(s"${a.name} (${ida}) moves towards ${b.name} (${idb}) | ${xa};${ya};${za} -> ${xa+x.toInt};${ya+y.toInt};${za+z.toInt}")

    return Seq((ida, "x", x.toInt), (ida, "y", y.toInt), (ida, "z", z.toInt))
  }


  def attack(ida:Int, a:Monster, idb:Int, b:Monster, attacks:Seq[Int], rolls:Int, dice:Int, base_damage:Int, melee:Boolean = true):Seq[(Int, String, Int)] = {
    //If melee attack, but monster B is flying while A isn't, miss atttack
    if (melee && (a.get("flying") < b.get("flying")))
      return Seq()
      
    //Rolls
    var debug_msg = if (debug) s"${a.name} (${ida}) attacks ${b.name} (${idb}) " else ""
    var diffs = Seq[(Int, String, Int)]()
    attacks.map(prec => {
      //Precision roll
      var d20 = 1 + rand.nextInt(20)
      if ((d20 == 20)||(prec + d20 >= b.get("armor"))) {
        //Damages roll
        var damage = base_damage
        for (i <- 0 until rolls)
          damage += 1 + rand.nextInt(dice)
        diffs = diffs ++ Seq((idb, "hp", -damage))
        if (debug) debug_msg += s"| ${damage} damage ${if (d20 == 20) "(20!)" else ""}"
      } else {
        if (debug) debug_msg += s"| miss (${prec + d20} < ${b.get("armor")})"
      }
    })
    if (debug) println(debug_msg)
    return diffs
  }

  //Solar skills
  def dancing_greatsword(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(35, 30, 25, 20), 3, 6, 18)
  }
  
  def composite_longbow(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(31, 26, 21, 16), 2, 6, 14, false)
  }

  //Orc barbarian skills
  def orc_double_axe(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(19, 14, 9), 1, 8, 10)
  }

  def mwk_composite_longbow(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(16, 11, 10), 1, 8, 6, false)
  }

  //Orc rider skills
  def mwk_battleaxe(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(6), 1, 8, 2)
  }

  def shortbow(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(4), 1, 6, 0, false)
  }

  //Warlord skills
  def vicious_flail(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(20, 15, 10), 1, 8, 10)
  }

  def mwk_throwing_axe(ida:Int, a:Monster, idb:Int, b:Monster):Seq[(Int, String, Int)] = {
    return attack(ida, a, idb, b, Seq(19), 1, 6, 5, false)
  }

}