//Imports and packages
package com.exercise1
import com.exercise1.Global
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import scala.collection.mutable
import scala.util.parsing.json.JSONObject
import java.io._

//-------------------------------------------------------------------------------------------
//Batch layer
package object BatchLayer {

  //Loaded batch views
  val views = mutable.Map.empty[String, RDD[(String, Iterable[String])]]

  //Compute batch views
  //(May take some time)
  def compute(spark:SparkSession): Unit = {
    //Debug
    println("Computing batch views...")

    //
    if (Global.exists(s"src/resources/batchviews")) {
      println("  > (Operation was skipped, remove src/resources/batchviews to recompute)")
      return
    }

    //Retrieve data from JSON file (spells)
    val spells = spark.sqlContext.read
      .option("multiLine", true)
      .json("src/resources/JSON/spells.json")

    //Retrieve data from JSON file (monsters)
    val monsters = spark.sqlContext.read
      .option("multiLine", true)
      .json("src/resources/JSON/monsters.json")

    //Clean batchviews folder
    Global.clean("src/resources/batchviews")

    //Create a batch view (spell_name, [...monsters])
    monsters.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("spells")))
      .flatMap{case (monster, spells) => spells.map(spell => (spell, monster))}
      .groupByKey()
      .map{case (key, values) => (key, values.mkString("[", ";;", "]"))}
      .saveAsTextFile(s"src/resources/batchviews/spells/monsters")


    //Prepare monsters data for futures batch views (spell_name, [...monsters data])
    val spells_monsters = monsters.rdd
      .map(row => (row.getValuesMap[Any](row.schema.fieldNames), row.getAs[Seq[String]]("spells")))
      .flatMap{case (monster, spells) => spells.map(spell => (spell, monster))}
      .groupByKey()
      .map{case (key, values) => (key, values.map(value => value.map{case (k, v) => (k, if (v.isInstanceOf[mutable.WrappedArray[String]]) v.asInstanceOf[mutable.WrappedArray[String]].toArray.mkString("[", ",", "]") else v) }))}
      .map{case (key, values) => (key, values.map(value => JSONObject(value)))}
      .map{case (key, values) => (key, values.mkString("[", ",", "]"))}

    //Create spells batch view (spell_name, [spell_data])
    //This view store in its values spells raw json data
    for(i <-97 until 123) {
      spells.rdd
        .map(row => (row.getAs[String]("name"), row.getValuesMap[Any](row.schema.fieldNames)))
        .filter{case (key,value) => key.charAt(0) == i.asInstanceOf[Char]}
        .groupByKey()
        .leftOuterJoin(spells_monsters)
        .flatMap{case (spell, (left, right)) => left.map(data => (spell, data ++ Map("monsters" -> right.getOrElse("[]"))))}
        .map{case (key, value) => (key, value.map{case (k, v) => (k, if (v.isInstanceOf[mutable.WrappedArray[String]]) v.asInstanceOf[mutable.WrappedArray[String]].toArray.mkString("[", ",", "]") else v) })}
        .map{case (key, value) => (key, JSONObject(value.filter(_._2 != null)).toString())}
        .map{case (key, value) => (key, s"[${value}]")}
        .coalesce(1)
        .saveAsTextFile(s"src/resources/batchviews/spells/name/${i.asInstanceOf[Char]}")
    }

    //Create keywords batch views (keywords, [...spell_name])
    val spells_keywords = spells.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("keywords")))
      .flatMap{case (spell, keywords) => keywords.map(keyword => (keyword, spell))}
      .groupByKey()
      .map{case (keyword, spells) => (keyword, spells.mkString("[", ";;", "]"))}
      .saveAsTextFile(s"src/resources/batchviews/spells/keywords/all")

    //Create spells batch view which regroup all spells (spell_name, [spell_data])
    spells.rdd
        .map(row => (row.getAs[String]("name"), row.getValuesMap[Any](row.schema.fieldNames)))
        .groupByKey()
        .leftOuterJoin(spells_monsters)
        .flatMap{case (spell, (left, right)) => left.map(data => (spell, data ++ Map("monsters" -> right.getOrElse("[]"))))}
        .map{case (key, value) => (key, value.map{case (k, v) => (k, if (v.isInstanceOf[mutable.WrappedArray[String]]) v.asInstanceOf[mutable.WrappedArray[String]].toArray.mkString("[", ",", "]") else v) })}
        .map{case (key, value) => (key, JSONObject(value.filter(_._2 != null)).toString())}
        .map{case (key, value) => (key, s"[${value}]")}
        .coalesce(1)
        .saveAsTextFile(s"src/resources/batchviews/spells/name/all")

    //Prepare components batch views (component, [...spell_name])
    val spells_components = spells.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("components")))
      .flatMap{case (spell, components) => components.map(component => (component, spell))}
      .groupByKey()

    //Create components batch views (spell_name, [component])
    spells_components.collectAsMap().keys.foreach{case component => {
      spells_components
        .filter{case (key, values) => key == component}
        .flatMap{case (key, spells) => spells.map(spell => (spell, component))}
        .map{case (spell, component) => (spell, s"[${component}]")}
        .saveAsTextFile(s"src/resources/batchviews/spells/components/${component}")
    }}

    //Prepare schools batch views (school, [...spell_name])
    val spells_schools = spells.rdd
      .map(row => (row.getAs[String]("School").split(" ")(0).toLowerCase, row.getAs[String]("name")))
      .groupByKey()

    //Create schools batch views (spell_name, [school])
    spells_schools.collectAsMap().keys.foreach{case school => {
      spells_schools
        .filter{case (key, values) => key == school}
        .flatMap{case (key, spells) => spells.map(spell => (spell, school))}
        .map{case (spell, school) => (spell, s"[${school}]")}
        .saveAsTextFile(s"src/resources/batchviews/spells/schools/${school}")
    }}

    //Prepare classes and levels batch views (spell_name, [...spell_class_lvl]")
    val spells_cl_lvl = spells.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[String]("Level").toLowerCase.replaceAll("\\/", "-")))
      .groupByKey()
      .map{case (key, value) => (key, value.flatMap((v: String) => v.split(",").map(w => w.trim)))}

    //Prepare classes batch views (spell_class, [...(spell_name, spell_class_lvl]")
    val spells_classes = spells_cl_lvl
      .flatMap{case (spell, classes_lvl) => classes_lvl.map(class_lvl => (class_lvl, spell))}
      .filter{case (key, value) => key.matches("^[a-z-]+ [0-9]+")}
      .map{case (class_lvl, spell) => (class_lvl.split(" ")(0).toLowerCase, (spell, class_lvl.split(" ")(1)))}
      .groupByKey()

    //Create classes batch views (spell_name, spell_class) and (spell_name, spell_level)
    spells_classes.collectAsMap().keys.foreach{case kind => {

      //Filter by spells by class
      val spells_classes_all = spells_classes
        .filter { case (key, values) => key == kind }

      //Create batch view (spell_name, spell_class) (all levels)
      spells_classes_all
        .flatMap { case (key, spells) => spells.map(spell_lvl => (spell_lvl._1, kind)) }
        .map { case (spell, kind) => (spell, s"[${kind}]") }
        .saveAsTextFile(s"src/resources/batchviews/spells/classes/${kind}/all")

      //Prepare batch view (spell_level, [...spells])
      val spells_levels = spells_classes_all
        .flatMap { case (key, spells) => spells.map(spell_lvl => (spell_lvl._2, spell_lvl._1)) }
        .groupByKey()

      //Create batch view (spell_name, spell_level)
      spells_levels.collectAsMap().keys.foreach { case level => {
        spells_levels
          .filter { case (key, values) => key == level }
          .flatMap { case (key, spells) => spells.map(spell => (spell, s"[${key}]")) }
          .saveAsTextFile(s"src/resources/batchviews/spells/classes/${kind}/${level}")
      }}

    }}

    //Prepare levels batch views (spell_class, [...(spell_name, spell_class_lvl]")
      val spells_levels = spells_classes
          .flatMap{case (kind, spells_lvl) => spells_lvl.map(spell_lvl => (spell_lvl._2, spell_lvl._1)) }
          .groupByKey()

      //Create levels batch views (spell_name, spell_level)
      spells_levels.collectAsMap().keys.foreach{case level => {
        spells_levels
          .filter { case (key, values) => key == level }
          .flatMap { case (key, spells) => spells.map(spell => (spell, s"[${key}]")) }
          .saveAsTextFile(s"src/resources/batchviews/spells/levels/${level}")
      }}
    

  }

  //Read batch views back from text file
  def load(spark:SparkSession, file:File = new File("src/resources/batchviews"), first:Boolean = true): Unit = {
      if (first)
        println("Loading batch views...")
      if (file.isDirectory){
          file.listFiles.foreach(subfile => 
              if(subfile.isDirectory) {
                  load(spark, subfile, false)
              } else if (subfile.getName == "part-00000") {
                    BatchLayer.views(file.toString.replaceAll("\\\\", "/")) = spark.sparkContext.textFile(s"${file.toString}")
                    .map(row => {
                        val matches = "^\\((.+),\\[(.+)\\]\\)$".r.findFirstMatchIn(row).get.subgroups
                        (matches(0), matches(1).split(";;"))
                    })
              }
            )
        }
    }

}