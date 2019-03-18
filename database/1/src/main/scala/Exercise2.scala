//Imports
  import org.apache.spark.sql.SparkSession

//Classes
  case class Page(url:String = "", links:Array[String] = Array(), var rank:Double = 1.0)

//Source
  object Exercise2 extends App {

    //Apache Spark
      val spark = SparkSession.builder
        .master("local")
        .appName("Exercise 2")
        .getOrCreate()
      spark.sparkContext.setLogLevel("ERROR")

    //Pages RDD
      var pages = spark.sparkContext.makeRDD(Array(
        ("A", new Page("A", Array("B", "C"))),
        ("B", new Page("B", Array("C"))),
        ("C", new Page("C", Array("A"))),
        ("D", new Page("D", Array("C")))
      )).cache()

    //Parameters (damping factor and number of iterations)
      val d = 0.85
      val n = 20

    //Page rank algorithm
      for (i <- 1 to n) {

        //Compute ranks values based on each contribution
          val ranks = pages
            .flatMap{case (_, page) => page.links.map(link => (link, page.rank / page.links.size))}
            .reduceByKey((a, b) => a + b)

        //Update Pages RDD
          pages = pages
            .leftOuterJoin(ranks)
            .sortByKey()
            .mapValues{case (page, rank) => {
              page.rank = (1 - d) + d * rank.getOrElse(0.0)
              page
            }}

        //Debug
          println(s"\n//// Iteration ${i} ////////////////////////////////////////////////")
          pages.foreach(v => println(s"${v._1} : ${v._2.rank}"))

      }

  }
