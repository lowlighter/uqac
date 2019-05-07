//Imports and packages
package com.exercise1
import com.exercise1.Global
import com.exercise1.BatchLayer
import io.circe.HCursor
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

//-------------------------------------------------------------------------------------------
//Batch layer
package object ServingLayer {

  //Spark session
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .getOrCreate()

  def process(query:HCursor):Response = {
    //Read query parameters
    val name = query.get[String]("name").getOrElse("")
    val components = query.get[Seq[String]]("components").getOrElse(List())
    val schools = query.get[Seq[String]]("schools").getOrElse(List())
    var levels = query.get[Seq[String]]("levels").getOrElse(List())
    val classes = query.get[Seq[String]]("classes").getOrElse(List())
    val init = query.get[Boolean]("init").getOrElse(false)
    val misc = query.get[Seq[String]]("misc").getOrElse(List())
    val limit = query.get[Int]("limit").getOrElse(20)

    //Init filters
    if (init) {
      return init_filters()
    } 

    //Search by name
    var name_view = BatchLayer.views("src/resources/batchviews/spells/name/all")
    if ((name.length > 0)&&(name.toLowerCase.charAt(0).toString.matches("[a-z]"))) {
        name_view = BatchLayer.views(s"src/resources/batchviews/spells/name/${name.toLowerCase.charAt(0)}")
    }
    if (name.length > 1) {
      name_view = name_view.filter{case (key, value) => key.contains(name)}
    }

    //Search by classes and possibly by level 
    var classes_view = spark.sparkContext.emptyRDD[(String, Iterable[String])]
    if(classes.length > 0) {
        classes.foreach(kind => {
          //If classes and levels are defined, search in class/level
          if (levels.length > 0) {
            levels.foreach(level => {
              if (BatchLayer.views.contains(s"src/resources/batchviews/spells/classes/${kind}/${level}")) {
                var kind_level_view = BatchLayer.views(s"src/resources/batchviews/spells/classes/${kind}/${level}")
                classes_view = classes_view.fullOuterJoin(kind_level_view).map{case (spell, (left, right)) => (spell, Iterable[String]("1"))}
              }
            })
          } 
          //If only classes are defined, search in class/all
          else {
            if (BatchLayer.views.contains(s"src/resources/batchviews/spells/classes/${kind}/all")) {
              var kind_view = BatchLayer.views(s"src/resources/batchviews/spells/classes/${kind}/all")
              classes_view = classes_view.fullOuterJoin(kind_view).map{case (spell, (left, right)) => (spell, Iterable[String]("1"))}
            }
          }
        })

        //If levels were treated in this section, skip it in the next section
        if (levels.length > 0)
          levels = List()
    }

    //Search by level only (if it was treated precendtly, skip this section)
    var levels_view = spark.sparkContext.emptyRDD[(String, Iterable[String])]
    if (levels.length > 0) {
      levels.foreach(level => {
        if (BatchLayer.views.contains(s"src/resources/batchviews/spells/levels/${level}")) {
          var level_view = BatchLayer.views(s"src/resources/batchviews/spells/levels/${level}")
          levels_view = levels_view.fullOuterJoin(level_view).map{case (spell, (left, right)) => (spell, Iterable[String]("1"))}
        }
      })
    }

    //Search by schools 
    var schools_view = spark.sparkContext.emptyRDD[(String, Iterable[String])]
    if (schools.length > 0) {
      schools.foreach(school => {
        if (BatchLayer.views.contains(s"src/resources/batchviews/spells/schools/${school}")) {
          var school_view = BatchLayer.views(s"src/resources/batchviews/spells/schools/${school}")
          schools_view = schools_view.fullOuterJoin(school_view).map{case (spell, (left, right)) => (spell, Iterable[String]("1"))}
        }
      })
    }

    //Search by components
    var components_view = spark.sparkContext.emptyRDD[(String, Iterable[String])]
    if (components.length > 0) {
      components.foreach(component => {
        if (BatchLayer.views.contains(s"src/resources/batchviews/spells/components/${component}")) {
          var component_view = BatchLayer.views(s"src/resources/batchviews/spells/components/${component}")
          components_view = components_view.fullOuterJoin(component_view).map{case (spell, (left, right)) => (spell, Iterable[String]("1"))}
        }
      })
    }

    //Search by description 
    var keywords_view  = spark.sparkContext.emptyRDD[(String, Int)]
    if (misc.length > 0) {
      keywords_view = BatchLayer.views("src/resources/batchviews/spells/keywords/all")
        .filter{case (keyword, spells) => misc.contains(keyword)}
        .flatMap{case (keyword, spells) => spells.map(spell => (spell, 1))}
        .reduceByKey((a, b) => a + b)
    }

    //Merge requests
    var view = name_view
    if (classes_view.count() > 0) view = view.join(classes_view).map{case (spell, (left, right)) => (spell, left)}
    if (levels_view.count() > 0) view = view.join(levels_view).map{case (spell, (left, right)) => (spell, left)}
    if (schools_view.count() > 0) view = view.join(schools_view).map{case (spell, (left, right)) => (spell, left)}
    if (components_view.count() > 0) view = view.join(components_view).map{case (spell, (left, right)) => (spell, left)}

    //Remove possibles duplicates
    view = view.groupByKey().map{case (spell, duplicates) => (spell, duplicates.toList(0))}

    //Merge requests (2)
    if (keywords_view.count() > 0) {
      view = view
        .join(keywords_view).map{case (spell, (left, right)) => (spell, left, right)}
        .sortBy(_._3, false)
        .map{case (spell, left, right) => (spell, left)}
    } else if (misc.length > 0) { view = spark.sparkContext.emptyRDD[(String, Iterable[String])] }

    
    val results =  view.take(limit+1).map{case (spell, spell_data) => spell_data.toList(0).asJson}

    return Response("", results, view.count())
  }

  def init_filters():Response = {
     val components_filter = Global.directory("src/resources/batchviews/spells/components")
     val schools_filter = Global.directory("src/resources/batchviews/spells/schools")
     val levels_filter = Global.directory("src/resources/batchviews/spells/levels")
     val classes_filter = Global.directory("src/resources/batchviews/spells/classes")

    val results = List(Map("type" -> Seq("init"), "components" -> components_filter, "schools" -> schools_filter, "levels" -> levels_filter, "classes" -> classes_filter).asJson).toArray
    return Response("", results)
  }

}

